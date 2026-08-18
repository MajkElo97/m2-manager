package pl.m2manager.building.controller;

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
import pl.m2manager.permission.repository.PermissionRepository;
import pl.m2manager.role.entity.Role;
import pl.m2manager.role.entity.RolePermission;
import pl.m2manager.role.repository.RolePermissionRepository;
import pl.m2manager.role.repository.RoleRepository;
import pl.m2manager.user.entity.User;
import pl.m2manager.user.entity.UserRole;
import pl.m2manager.user.repository.UserRepository;
import pl.m2manager.user.repository.UserRoleRepository;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("jwt-it")
@Testcontainers(disabledWithoutDocker = true)
class BuildingControllerSecurityIntegrationTest {

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
	private RoleRepository roleRepository;

	@Autowired
	private PermissionRepository permissionRepository;

	@Autowired
	private RolePermissionRepository rolePermissionRepository;

	@Autowired
	private UserRoleRepository userRoleRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private String slug;

	@BeforeEach
	void setUp() {
		slug = "buildings-it-" + UUID.randomUUID().toString().substring(0, 8);
		Organization organization = saveOrganization("Buildings IT Org", slug);
		User authorizedUser = saveUser(organization, "buildings-view@example.com", "password");
		saveUser(organization, "buildings-denied@example.com", "password");

		Role role = saveRole(organization, "Buildings Viewer");
		assignPermission(role, "BUILDINGS_VIEW");
		assignRole(authorizedUser, role, organization);
	}

	@Test
	void list_withoutJwt_returns401() throws Exception {
		mockMvc.perform(get("/api/buildings"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void list_authenticatedWithoutPermission_returns403() throws Exception {
		String token = loginAndExtractToken("buildings-denied@example.com", "password");

		mockMvc.perform(get("/api/buildings")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.status").value(403));
	}

	@Test
	void list_authenticatedWithBuildingsView_returns200() throws Exception {
		String token = loginAndExtractToken("buildings-view@example.com", "password");

		mockMvc.perform(get("/api/buildings")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray());
	}

	private String loginAndExtractToken(String email, String password) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(loginRequest(email, password)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andReturn();

		String responseBody = result.getResponse().getContentAsString();
		int tokenStart = responseBody.indexOf("\"accessToken\":\"") + 15;
		int tokenEnd = responseBody.indexOf('"', tokenStart);
		return responseBody.substring(tokenStart, tokenEnd);
	}

	private String loginRequest(String email, String password) {
		return """
				{
				  "organizationSlug": "%s",
				  "email": "%s",
				  "password": "%s"
				}
				""".formatted(slug, email, password);
	}

	private Organization saveOrganization(String name, String slug) {
		Organization org = new Organization();
		org.setName(name);
		org.setSlug(slug);
		org.setTimezone("Europe/Warsaw");
		return organizationRepository.saveAndFlush(org);
	}

	private User saveUser(Organization organization, String email, String rawPassword) {
		User user = new User();
		user.setOrganization(organization);
		user.setEmail(email);
		user.setPasswordHash(passwordEncoder.encode(rawPassword));
		return userRepository.saveAndFlush(user);
	}

	private Role saveRole(Organization organization, String name) {
		Role role = new Role();
		role.setOrganization(organization);
		role.setName(name);
		return roleRepository.saveAndFlush(role);
	}

	private void assignPermission(Role role, String permissionCode) {
		var permission = permissionRepository.findByCode(permissionCode).orElseThrow();
		rolePermissionRepository.saveAndFlush(new RolePermission(role.getId(), permission.getId()));
	}

	private void assignRole(User user, Role role, Organization organization) {
		userRoleRepository.saveAndFlush(new UserRole(organization.getId(), user.getId(), role.getId()));
	}
}
