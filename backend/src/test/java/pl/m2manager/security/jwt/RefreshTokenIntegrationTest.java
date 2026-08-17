package pl.m2manager.security.jwt;

import jakarta.servlet.http.Cookie;
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
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.m2manager.organization.entity.Organization;
import pl.m2manager.organization.repository.OrganizationRepository;
import pl.m2manager.user.entity.User;
import pl.m2manager.user.repository.UserRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class RefreshTokenIntegrationTest {

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
	private RefreshTokenRepository refreshTokenRepository;

	@Autowired
	private RefreshTokenService refreshTokenService;

	@Autowired
	private RefreshTokenHasher refreshTokenHasher;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private TransactionTemplate transactionTemplate;

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
	void generateRawToken_producesUniqueCryptographicValues() {
		String first = refreshTokenService.generateRawToken();
		String second = refreshTokenService.generateRawToken();

		assertThat(first).isNotBlank();
		assertThat(second).isNotBlank();
		assertThat(first).isNotEqualTo(second);
	}

	@Test
	void hashRawToken_matchesSha256Hex() {
		String raw = refreshTokenService.generateRawToken();
		String hash = refreshTokenService.hashRawToken(raw);

		assertThat(hash).hasSize(64);
		assertThat(hash).isEqualTo(refreshTokenHasher.hash(raw));
	}

	@Test
	void issueNewFamily_storesHashNotRawToken() {
		IssuedRefreshToken issued = refreshTokenService.issueNewFamily(userA.getId(), organizationA.getId());
		RefreshToken stored = refreshTokenRepository.findByTokenHash(refreshTokenService.hashRawToken(issued.rawToken()))
				.orElseThrow();

		assertThat(stored.getTokenHash()).isNotEqualTo(issued.rawToken());
		assertThat(stored.getExpiresAt()).isAfter(stored.getCreatedAt());
	}

	@Test
	void login_successCreatesRefreshTokenAndHttpOnlyCookie() throws Exception {
		long beforeCount = refreshTokenRepository.count();

		MvcResult result = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(loginRequest(slugA, "john@example.com", "passwordA")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andExpect(jsonPath("$.refreshToken").doesNotExist())
				.andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("m2_refresh_token=")))
				.andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("HttpOnly")))
				.andReturn();

		assertThat(refreshTokenRepository.count()).isEqualTo(beforeCount + 1);
		assertThat(result.getResponse().getCookie("m2_refresh_token")).isNotNull();
		assertThat(userRepository.findById(userA.getId()).orElseThrow().getLastLoginAt()).isNotNull();
	}

	@Test
	void login_failureDoesNotCreateRefreshToken() throws Exception {
		long beforeCount = refreshTokenRepository.count();

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(loginRequest(slugA, "john@example.com", "wrong-password")))
				.andExpect(status().isUnauthorized());

		assertThat(refreshTokenRepository.count()).isEqualTo(beforeCount);
	}

	@Test
	void refresh_validTokenRotatesAndReturnsNewAccessToken() throws Exception {
		Cookie initialCookie = loginAndGetRefreshCookie();

		MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh").cookie(initialCookie))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.expiresIn").value(900))
				.andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("m2_refresh_token=")))
				.andReturn();

		RefreshToken oldToken = refreshTokenRepository.findByTokenHash(
				refreshTokenService.hashRawToken(initialCookie.getValue())
		).orElseThrow();
		assertThat(oldToken.getRevokedAt()).isNotNull();
		assertThat(oldToken.getReplacedByTokenId()).isNotNull();

		Cookie newCookie = refreshResult.getResponse().getCookie("m2_refresh_token");
		assertThat(newCookie.getValue()).isNotEqualTo(initialCookie.getValue());
	}

	@Test
	void refresh_sameTokenCannotBeUsedTwice() throws Exception {
		Cookie initialCookie = loginAndGetRefreshCookie();
		mockMvc.perform(post("/api/auth/refresh").cookie(initialCookie)).andExpect(status().isOk());

		mockMvc.perform(post("/api/auth/refresh").cookie(initialCookie))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value(InvalidRefreshTokenException.MESSAGE));
	}

	@Test
	void refresh_unknownTokenReturns401() throws Exception {
		mockMvc.perform(post("/api/auth/refresh")
						.cookie(new Cookie("m2_refresh_token", refreshTokenService.generateRawToken())))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value(InvalidRefreshTokenException.MESSAGE));
	}

	@Test
	void refresh_expiredTokenReturns401() throws Exception {
		IssuedRefreshToken issued = refreshTokenService.issueNewFamily(userA.getId(), organizationA.getId());
		RefreshToken stored = refreshTokenRepository.findByTokenHash(refreshTokenService.hashRawToken(issued.rawToken()))
				.orElseThrow();
		stored.setExpiresAt(Instant.parse("2020-01-01T00:00:00Z"));
		refreshTokenRepository.saveAndFlush(stored);

		mockMvc.perform(post("/api/auth/refresh").cookie(new Cookie("m2_refresh_token", issued.rawToken())))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void refresh_inactiveUserReturns401() throws Exception {
		Cookie cookie = loginAndGetRefreshCookie();
		userA.setActive(false);
		userRepository.saveAndFlush(userA);

		mockMvc.perform(post("/api/auth/refresh").cookie(cookie))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void reuseDetection_revokesEntireFamily() throws Exception {
		Cookie tokenOne = loginAndGetRefreshCookie();
		MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh").cookie(tokenOne))
				.andExpect(status().isOk())
				.andReturn();
		Cookie tokenTwo = refreshResult.getResponse().getCookie("m2_refresh_token");

		mockMvc.perform(post("/api/auth/refresh").cookie(tokenOne))
				.andExpect(status().isUnauthorized());

		RefreshToken revokedTokenOne = refreshTokenRepository.findByTokenHash(
				refreshTokenService.hashRawToken(tokenOne.getValue())
		).orElseThrow();
		RefreshToken revokedTokenTwo = refreshTokenRepository.findByTokenHash(
				refreshTokenService.hashRawToken(tokenTwo.getValue())
		).orElseThrow();

		assertThat(revokedTokenOne.getReuseDetectedAt()).isNotNull();
		assertThat(revokedTokenOne.getRevokedAt()).isNotNull();
		assertThat(revokedTokenTwo.getRevokedAt()).isNotNull();

		mockMvc.perform(post("/api/auth/refresh").cookie(tokenTwo))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void logout_revokesTokenAndClearsCookie() throws Exception {
		Cookie cookie = loginAndGetRefreshCookie();

		mockMvc.perform(post("/api/auth/logout").cookie(cookie))
				.andExpect(status().isNoContent())
				.andExpect(header().string("Set-Cookie", org.hamcrest.Matchers.containsString("Max-Age=0")));

		RefreshToken stored = refreshTokenRepository.findByTokenHash(refreshTokenService.hashRawToken(cookie.getValue()))
				.orElseThrow();
		assertThat(stored.getRevokedAt()).isNotNull();

		mockMvc.perform(post("/api/auth/refresh").cookie(cookie))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void logout_withoutCookieIsIdempotent() throws Exception {
		mockMvc.perform(post("/api/auth/logout"))
				.andExpect(status().isNoContent());
	}

	@Test
	void logout_withAlreadyRevokedTokenIsIdempotent() throws Exception {
		Cookie cookie = loginAndGetRefreshCookie();
		mockMvc.perform(post("/api/auth/logout").cookie(cookie)).andExpect(status().isNoContent());

		mockMvc.perform(post("/api/auth/logout").cookie(cookie))
				.andExpect(status().isNoContent());
	}

	@Test
	void tenantIsolation_refreshTokenAlwaysIssuesOrganizationFromRecord() throws Exception {
		Cookie cookie = loginAndGetRefreshCookie();

		mockMvc.perform(post("/api/auth/refresh").cookie(cookie))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isNotEmpty());

		RefreshToken stored = refreshTokenRepository.findByTokenHash(refreshTokenService.hashRawToken(cookie.getValue()))
				.orElseThrow();
		assertThat(stored.getOrganizationId()).isEqualTo(organizationA.getId());
	}

	@Test
	void concurrentRefresh_onlyOneRotationSucceeds() throws Exception {
		IssuedRefreshToken issued = refreshTokenService.issueNewFamily(userA.getId(), organizationA.getId());
		String rawToken = issued.rawToken();

		ExecutorService executor = Executors.newFixedThreadPool(2);
		CountDownLatch startLatch = new CountDownLatch(1);
		CountDownLatch doneLatch = new CountDownLatch(2);
		AtomicInteger successes = new AtomicInteger();
		AtomicInteger failures = new AtomicInteger();

		for (int i = 0; i < 2; i++) {
			executor.submit(() -> {
				try {
					startLatch.await();
					transactionTemplate.executeWithoutResult(status -> {
						try {
							refreshTokenService.rotate(rawToken);
							successes.incrementAndGet();
						} catch (InvalidRefreshTokenException ex) {
							failures.incrementAndGet();
						}
					});
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				} finally {
					doneLatch.countDown();
				}
			});
		}

		startLatch.countDown();
		doneLatch.await();
		executor.shutdown();

		assertThat(successes.get()).isEqualTo(1);
		assertThat(failures.get()).isEqualTo(1);

		List<RefreshToken> familyTokens = refreshTokenRepository.findByFamilyId(
				refreshTokenRepository.findByTokenHash(refreshTokenService.hashRawToken(rawToken)).orElseThrow().getFamilyId()
		);
		assertThat(familyTokens.stream().filter(token -> token.getRevokedAt() != null).count()).isGreaterThanOrEqualTo(1);
	}

	private Cookie loginAndGetRefreshCookie() throws Exception {
		MvcResult result = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(loginRequest(slugA, "john@example.com", "passwordA")))
				.andExpect(status().isOk())
				.andReturn();
		return result.getResponse().getCookie("m2_refresh_token");
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
