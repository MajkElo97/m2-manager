package pl.m2manager.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.m2manager.organization.entity.Organization;
import pl.m2manager.organization.repository.OrganizationRepository;
import pl.m2manager.security.auth.dto.AuthenticationResult;
import pl.m2manager.user.entity.User;
import pl.m2manager.user.repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("jwt-it")
@Testcontainers(disabledWithoutDocker = true)
class JwtSecurityIntegrationTest {

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
			.withDatabaseName("m2manager_test")
			.withUsername("m2manager")
			.withPassword("m2manager_test");

	@DynamicPropertySource
	static void configureDataSource(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private Organization organizationA;
	private Organization organizationB;
	private User userA;
	private String slugA;
	private String slugB;

	@BeforeEach
	void setUp() {
		slugA = "org-a-" + UUID.randomUUID().toString().substring(0, 8);
		slugB = "org-b-" + UUID.randomUUID().toString().substring(0, 8);
		organizationA = saveOrganization("Organization A", slugA);
		organizationB = saveOrganization("Organization B", slugB);
		userA = saveUser(organizationA, "john@example.com", "passwordA");
		saveUser(organizationB, "john@example.com", "passwordB");
	}

	@Test
	void login_isAccessibleWithoutAuthorizationHeader() throws Exception {
		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(loginRequest(slugA, "john@example.com", "passwordA")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isNotEmpty());
	}

	@Test
	void getOrganization_withoutJwt_returns401() throws Exception {
		mockMvc.perform(get("/api/organization"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void getOrganization_withValidJwt_returns200() throws Exception {
		String token = loginAndExtractToken(slugA, "john@example.com", "passwordA");

		mockMvc.perform(get("/api/organization")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(organizationA.getId().toString()))
				.andExpect(jsonPath("$.slug").value(slugA));
	}

	@Test
	void getOrganization_withExpiredJwt_returns401() throws Exception {
		Clock pastClock = Clock.fixed(Instant.parse("2020-01-01T00:00:00Z"), ZoneOffset.UTC);
		JwtService pastJwtService = new JwtService(
				new JwtProperties("jwt-it-secret-not-for-production-min-32-chars!!", Duration.ofSeconds(60)),
				pastClock
		);
		org.springframework.test.util.ReflectionTestUtils.invokeMethod(pastJwtService, "initSigningKey");

		String expiredToken = pastJwtService.generateAccessToken(new AuthenticationResult(
				userA.getId(),
				organizationA.getId(),
				userA.getEmail()
		));

		mockMvc.perform(get("/api/organization")
						.header("Authorization", "Bearer " + expiredToken))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void getOrganization_withInvalidJwt_returns401() throws Exception {
		mockMvc.perform(get("/api/organization")
						.header("Authorization", "Bearer not-a-valid-jwt"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void getOrganization_authenticatedUserAResolvesOrganizationA() throws Exception {
		String token = loginAndExtractToken(slugA, "john@example.com", "passwordA");

		mockMvc.perform(get("/api/organization")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(organizationA.getId().toString()))
				.andExpect(jsonPath("$.slug").value(slugA));

		mockMvc.perform(get("/api/organization")
						.header("Authorization", "Bearer " + token)
						.queryParam("organizationId", organizationB.getId().toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(organizationA.getId().toString()))
				.andExpect(jsonPath("$.slug").value(slugA));
	}

	@Test
	void getOrganization_tamperedOrganizationIdClaim_returns401() throws Exception {
		String token = loginAndExtractToken(slugA, "john@example.com", "passwordA");
		String tamperedToken = tamperOrganizationIdClaim(token, organizationB.getId());

		mockMvc.perform(get("/api/organization")
						.header("Authorization", "Bearer " + tamperedToken))
				.andExpect(status().isUnauthorized());
	}

	private String loginAndExtractToken(String organizationSlug, String email, String password) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(loginRequest(organizationSlug, email, password)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andReturn();

		String responseBody = result.getResponse().getContentAsString();
		int tokenStart = responseBody.indexOf("\"accessToken\":\"") + 15;
		int tokenEnd = responseBody.indexOf('"', tokenStart);
		return responseBody.substring(tokenStart, tokenEnd);
	}

	private String loginRequest(String organizationSlug, String email, String password) {
		return """
				{
				  "organizationSlug": "%s",
				  "email": "%s",
				  "password": "%s"
				}
				""".formatted(organizationSlug, email, password);
	}

	private String tamperOrganizationIdClaim(String token, UUID newOrganizationId) {
		String[] parts = token.split("\\.");
		assertThat(parts).hasSize(3);

		String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
		String tamperedPayload = payloadJson.replaceFirst(
				"\"organization_id\":\"[^\"]+\"",
				"\"organization_id\":\"" + newOrganizationId + "\""
		);
		String encodedPayload = Base64.getUrlEncoder().withoutPadding()
				.encodeToString(tamperedPayload.getBytes(StandardCharsets.UTF_8));

		return parts[0] + "." + encodedPayload + "." + parts[2];
	}

	private Organization saveOrganization(String name, String slug) {
		Organization organization = new Organization();
		organization.setName(name);
		organization.setSlug(slug);
		organization.setTimezone("Europe/Warsaw");
		return organizationRepository.saveAndFlush(organization);
	}

	private User saveUser(Organization organization, String email, String rawPassword) {
		User user = new User();
		user.setOrganization(organization);
		user.setEmail(email);
		user.setPasswordHash(passwordEncoder.encode(rawPassword));
		return userRepository.saveAndFlush(user);
	}
}
