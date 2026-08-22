package pl.m2manager.organization.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import pl.m2manager.organization.repository.OrganizationRepository;
import pl.m2manager.role.repository.RoleRepository;
import pl.m2manager.user.entity.User;
import pl.m2manager.user.repository.UserRepository;
import pl.m2manager.user.repository.UserRoleRepository;

import java.util.UUID;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("jwt-it")
@Testcontainers(disabledWithoutDocker = true)
class OrganizationsAdminIntegrationTest {

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

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserRoleRepository userRoleRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void superAdmin_canListOrganizations() throws Exception {
		String token = loginSuperAdmin();

		mockMvc.perform(get("/api/organizations")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.slug=='m2-manager-dev')]").exists())
				.andExpect(jsonPath("$[?(@.slug=='admin')]").doesNotExist());
	}

	@Test
	void organizationAdmin_cannotListOrganizations() throws Exception {
		String token = login("m2-manager-dev", "multiadmin@m2manager.local", "Admin123!");

		mockMvc.perform(get("/api/organizations")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isForbidden());
	}

	@Test
	void superAdmin_canCreateOrganization() throws Exception {
		String token = loginSuperAdmin();

		MvcResult result = mockMvc.perform(post("/api/organizations")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Firma ABC",
								  "slug": "firma-abc",
								  "adminEmail": "admin@firma-abc.test"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("Firma ABC"))
				.andExpect(jsonPath("$.adminEmail").value("admin@firma-abc.test"))
				.andExpect(jsonPath("$.temporaryPassword").isNotEmpty())
				.andReturn();

		JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
		UUID organizationId = UUID.fromString(body.get("id").asText());
		String temporaryPassword = body.get("temporaryPassword").asText();

		assertThat(roleRepository.findByOrganizationId(organizationId))
				.extracting(role -> role.getName())
				.containsExactlyInAnyOrder("ADMIN", "BIURO", "KOORDYNATOR", "PRACOWNIK");
		assertThat(roleRepository.findByOrganizationId(organizationId))
				.extracting(role -> role.getName())
				.doesNotContain("SUPER_ADMIN");

		User admin = userRepository.findByEmail("admin@firma-abc.test").orElseThrow();
		assertThat(admin.isMustChangePassword()).isTrue();
		assertThat(passwordEncoder.matches(temporaryPassword, admin.getPasswordHash())).isTrue();
		assertThat(roleRepository.findByOrganizationIdAndName(organizationId, "ADMIN")).isPresent();
	}

	@Test
	void cannotCreateOrganizationWithDuplicateSlug() throws Exception {
		String token = loginSuperAdmin();

		mockMvc.perform(post("/api/organizations")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Duplicate Slug Org",
								  "slug": "m2-manager-dev",
								  "adminEmail": "dup-slug@test.local"
								}
								"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Organizacja o takim slug już istnieje."));

		assertThat(userRepository.findByEmail("dup-slug@test.local")).isEmpty();
	}

	@Test
	void cannotCreateOrganizationWithDuplicateName() throws Exception {
		String token = loginSuperAdmin();

		mockMvc.perform(post("/api/organizations")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "M2 Manager Dev",
								  "slug": "duplicate-name-org",
								  "adminEmail": "dup-name@test.local"
								}
								"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Organizacja o takiej nazwie już istnieje."));

		assertThat(userRepository.findByEmail("dup-name@test.local")).isEmpty();
	}

	@Test
	void cannotCreateOrganizationWithExistingLogin() throws Exception {
		String token = loginSuperAdmin();

		mockMvc.perform(post("/api/organizations")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Existing Login Org",
								  "slug": "existing-login-org",
								  "adminEmail": "multiadmin@m2manager.local"
								}
								"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Użytkownik o takim loginie już istnieje."));
	}

	@Test
	void createdAdminMustChangePasswordOnFirstLogin() throws Exception {
		String token = loginSuperAdmin();

		MvcResult createResult = mockMvc.perform(post("/api/organizations")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Password Flow Org",
								  "slug": "password-flow-org",
								  "adminEmail": "admin@password-flow.test"
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn();

		JsonNode body = objectMapper.readTree(createResult.getResponse().getContentAsString());
		String temporaryPassword = body.get("temporaryPassword").asText();

		MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "organizationSlug": "password-flow-org",
								  "email": "admin@password-flow.test",
								  "password": "%s"
								}
								""".formatted(temporaryPassword)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.mustChangePassword").value(true))
				.andReturn();

		String userToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
				.get("accessToken").asText();

		mockMvc.perform(get("/api/buildings")
						.header("Authorization", "Bearer " + userToken))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("Password change required"));

		mockMvc.perform(post("/api/auth/change-password")
						.header("Authorization", "Bearer " + userToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "currentPassword": "%s",
								  "newPassword": "NewSecure123!",
								  "confirmPassword": "NewSecure123!"
								}
								""".formatted(temporaryPassword)))
				.andExpect(status().isNoContent());

		User user = userRepository.findByEmail("admin@password-flow.test").orElseThrow();
		assertThat(user.isMustChangePassword()).isFalse();
	}

	@Test
	void superAdmin_canResetAdminPassword() throws Exception {
		String superAdminToken = loginSuperAdmin();

		MvcResult createResult = mockMvc.perform(post("/api/organizations")
						.header("Authorization", "Bearer " + superAdminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Reset Password Org",
								  "slug": "reset-password-org",
								  "adminEmail": "admin@reset-password.test"
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn();

		JsonNode createBody = objectMapper.readTree(createResult.getResponse().getContentAsString());
		UUID organizationId = UUID.fromString(createBody.get("id").asText());
		String oldPassword = createBody.get("temporaryPassword").asText();

		MvcResult resetResult = mockMvc.perform(post("/api/organizations/{id}/reset-admin-password", organizationId)
						.header("Authorization", "Bearer " + superAdminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.temporaryPassword").isNotEmpty())
				.andReturn();

		String newPassword = objectMapper.readTree(resetResult.getResponse().getContentAsString())
				.get("temporaryPassword").asText();

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "organizationSlug": "reset-password-org",
								  "email": "admin@reset-password.test",
								  "password": "%s"
								}
								""".formatted(oldPassword)))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "organizationSlug": "reset-password-org",
								  "email": "admin@reset-password.test",
								  "password": "%s"
								}
								""".formatted(newPassword)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.mustChangePassword").value(true));

		User admin = userRepository.findByEmail("admin@reset-password.test").orElseThrow();
		assertThat(admin.isMustChangePassword()).isTrue();
	}

	@Test
	void systemOrganizationNotListedOrDeactivatable() throws Exception {
		String token = loginSuperAdmin();

		MvcResult listResult = mockMvc.perform(get("/api/organizations")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andReturn();

		JsonNode organizations = objectMapper.readTree(listResult.getResponse().getContentAsString());
		var ids = StreamSupport.stream(organizations.spliterator(), false)
				.map(node -> node.get("id").asText())
				.toList();
		assertThat(ids).doesNotContain(SYSTEM_ORGANIZATION_ID.toString());

		mockMvc.perform(delete("/api/organizations/{id}", SYSTEM_ORGANIZATION_ID)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isNotFound());
	}

	@Test
	void superAdmin_canDeactivateBusinessOrganization() throws Exception {
		String token = loginSuperAdmin();

		MvcResult createResult = mockMvc.perform(post("/api/organizations")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Deactivate Me Org",
								  "slug": "deactivate-me-org",
								  "adminEmail": "admin@deactivate-me.test"
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn();

		UUID organizationId = UUID.fromString(objectMapper.readTree(createResult.getResponse().getContentAsString())
				.get("id").asText());

		mockMvc.perform(delete("/api/organizations/{id}", organizationId)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.active").value(false));
	}

	@Test
	void superAdmin_canUpdateOrganization() throws Exception {
		String token = loginSuperAdmin();

		MvcResult createResult = mockMvc.perform(post("/api/organizations")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Update Me Org",
								  "slug": "update-me-org",
								  "adminEmail": "admin@update-me.test"
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn();

		UUID organizationId = UUID.fromString(objectMapper.readTree(createResult.getResponse().getContentAsString())
				.get("id").asText());

		mockMvc.perform(put("/api/organizations/{id}", organizationId)
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Updated Org Name",
								  "slug": "updated-org-slug"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Updated Org Name"))
				.andExpect(jsonPath("$.slug").value("updated-org-slug"));
	}

	private String loginSuperAdmin() throws Exception {
		return login("admin", "admin@m2manager.local", "Admin123!");
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
}
