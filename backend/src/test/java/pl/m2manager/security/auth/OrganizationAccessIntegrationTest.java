package pl.m2manager.security.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("jwt-it")
@Testcontainers(disabledWithoutDocker = true)
class OrganizationAccessIntegrationTest {

	private static final ObjectMapper objectMapper = new ObjectMapper();
	private static final UUID SYSTEM_ORGANIZATION_ID = UUID.fromString("00000000-0000-4000-8000-000000000001");

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

	@Test
	void superAdmin_context_excludesSystemOrganizationFromAvailableOrganizations() throws Exception {
		String token = login("admin", "admin@m2manager.local", "Admin123!");

		MvcResult result = mockMvc.perform(get("/api/auth/context")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.activeOrganization").isEmpty())
				.andExpect(jsonPath("$.canSwitchOrganizations").value(true))
				.andExpect(jsonPath("$.superAdmin").value(true))
				.andReturn();

		JsonNode organizations = objectMapper.readTree(result.getResponse().getContentAsString())
				.get("availableOrganizations");
		var slugs = StreamSupport.stream(organizations.spliterator(), false)
				.map(node -> node.get("slug").asText())
				.toList();

		assertThat(slugs).contains("m2-manager-dev", "m2-manager-dev-secondary");
		assertThat(slugs).doesNotContain("admin");
	}

	@Test
	void superAdmin_canSwitchToBusinessOrganization() throws Exception {
		String token = login("admin", "admin@m2manager.local", "Admin123!");
		Cookie refreshCookie = loginRefreshCookie("admin", "admin@m2manager.local", "Admin123!");

		mockMvc.perform(post("/api/auth/context/organization")
						.header("Authorization", "Bearer " + token)
						.cookie(refreshCookie)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"organizationId":"a0000000-0000-4000-8000-000000000001"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isNotEmpty());
	}

	@Test
	void superAdmin_cannotSwitchToSystemOrganization() throws Exception {
		String token = login("admin", "admin@m2manager.local", "Admin123!");

		mockMvc.perform(post("/api/auth/context/organization")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"organizationId":"%s"}
								""".formatted(SYSTEM_ORGANIZATION_ID)))
				.andExpect(status().isForbidden());
	}

	private String login(String organizationSlug, String email, String password) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "organizationSlug": "%s",
								  "email": "%s",
								  "password": "%s"
								}
								""".formatted(organizationSlug, email, password)))
				.andExpect(status().isOk())
				.andReturn();
		return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
	}

	private Cookie loginRefreshCookie(String organizationSlug, String email, String password) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "organizationSlug": "%s",
								  "email": "%s",
								  "password": "%s"
								}
								""".formatted(organizationSlug, email, password)))
				.andExpect(status().isOk())
				.andReturn();
		return result.getResponse().getCookie("m2_refresh_token");
	}
}
