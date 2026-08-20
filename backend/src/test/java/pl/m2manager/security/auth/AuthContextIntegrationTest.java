package pl.m2manager.security.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.m2manager.building.entity.Building;
import pl.m2manager.building.entity.BuildingStatus;
import pl.m2manager.building.repository.BuildingRepository;
import pl.m2manager.organization.entity.Organization;
import pl.m2manager.organization.repository.OrganizationRepository;
import pl.m2manager.permission.repository.PermissionRepository;
import pl.m2manager.role.entity.Role;
import pl.m2manager.role.entity.RolePermission;
import pl.m2manager.role.repository.RolePermissionRepository;
import pl.m2manager.role.repository.RoleRepository;
import pl.m2manager.user.entity.User;
import pl.m2manager.user.entity.UserOrganization;
import pl.m2manager.user.entity.UserRole;
import pl.m2manager.user.repository.UserOrganizationRepository;
import pl.m2manager.user.repository.UserRepository;
import pl.m2manager.user.repository.UserRoleRepository;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
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
class AuthContextIntegrationTest {

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

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserOrganizationRepository userOrganizationRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PermissionRepository permissionRepository;

	@Autowired
	private RolePermissionRepository rolePermissionRepository;

	@Autowired
	private UserRoleRepository userRoleRepository;

	@Autowired
	private BuildingRepository buildingRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private Organization organizationA;
	private Organization organizationB;
	private Organization organizationC;
	private User multiOrgUser;
	private User singleOrgUser;
	private User superAdminUser;
	private String slugA;
	private String slugB;
	private UUID buildingAId;
	private UUID buildingBId;

	@BeforeEach
	void setUp() {
		String suffix = UUID.randomUUID().toString().substring(0, 8);
		slugA = "ctx-a-" + suffix;
		slugB = "ctx-b-" + suffix;

		organizationA = saveOrganization("Context Org A", slugA);
		organizationB = saveOrganization("Context Org B", slugB);
		organizationC = saveOrganization("Context Org C", "ctx-c-" + suffix);

		multiOrgUser = saveUser(organizationA, "multi@example.com", "password");
		singleOrgUser = saveUser(organizationA, "single@example.com", "password");
		superAdminUser = saveUser(organizationA, "super@example.com", "password");

		grantOrganizationAccess(multiOrgUser, organizationA);
		grantOrganizationAccess(multiOrgUser, organizationB);

		Role adminRoleA = saveRole(organizationA, "ADMIN-A");
		assignPermission(adminRoleA, "BUILDINGS_VIEW");
		assignPermission(adminRoleA, "BUILDINGS_ADMIN");
		assignRole(multiOrgUser, adminRoleA, organizationA);

		Role biuroRoleB = saveRole(organizationB, "BIURO-B");
		assignPermission(biuroRoleB, "BUILDINGS_VIEW");
		assignRole(multiOrgUser, biuroRoleB, organizationB);

		Role singleRole = saveRole(organizationA, "SINGLE");
		assignPermission(singleRole, "BUILDINGS_VIEW");
		assignRole(singleOrgUser, singleRole, organizationA);

		Role superAdminRole = saveRole(organizationA, "SUPER_ADMIN");
		superAdminRole.setSystemRole(true);
		roleRepository.saveAndFlush(superAdminRole);
		for (var permission : permissionRepository.findAll()) {
			rolePermissionRepository.saveAndFlush(new RolePermission(superAdminRole.getId(), permission.getId()));
		}
		assignRole(superAdminUser, superAdminRole, organizationA);

		buildingAId = saveBuilding(organizationA, "BUILDING-A").getId();
		buildingBId = saveBuilding(organizationB, "BUILDING-B").getId();
	}

	@Test
	void getContext_unauthenticated_returns401() throws Exception {
		mockMvc.perform(get("/api/auth/context"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void switchOrganization_unauthenticated_returns401() throws Exception {
		MvcResult csrfBootstrap = mockMvc.perform(get("/actuator/health")).andReturn();
		Cookie csrfCookie = csrfBootstrap.getResponse().getCookie("XSRF-TOKEN");

		mockMvc.perform(post("/api/auth/context/organization")
						.cookie(csrfCookie)
						.header("X-XSRF-TOKEN", csrfCookie.getValue())
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"organizationId":"%s"}
								""".formatted(organizationA.getId())))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void singleOrgUser_canSwitchToOwnOrganization() throws Exception {
		String token = loginAndExtractToken(slugA, singleOrgUser.getEmail(), "password");

		mockMvc.perform(post("/api/auth/context/organization")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"organizationId":"%s"}
								""".formatted(organizationA.getId())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isNotEmpty());
	}

	@Test
	void singleOrgUser_cannotSwitchToForeignOrganization() throws Exception {
		String token = loginAndExtractToken(slugA, singleOrgUser.getEmail(), "password");

		mockMvc.perform(post("/api/auth/context/organization")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"organizationId":"%s"}
								""".formatted(organizationB.getId())))
				.andExpect(status().isForbidden());
	}

	@Test
	void multiOrgUser_canSwitchBetweenAssignedOrganizations() throws Exception {
		String token = loginAndExtractToken(slugA, multiOrgUser.getEmail(), "password");
		Cookie refreshCookie = loginAndGetRefreshCookie(slugA, multiOrgUser.getEmail(), "password");

		MvcResult switchResult = mockMvc.perform(post("/api/auth/context/organization")
						.header("Authorization", "Bearer " + token)
						.cookie(refreshCookie)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"organizationId":"%s"}
								""".formatted(organizationB.getId())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andReturn();

		String newToken = extractAccessToken(switchResult);
		assertThat(decodeOrganizationId(newToken)).isEqualTo(organizationB.getId());
	}

	@Test
	void afterSwitch_tenantScopedEndpointsReturnActiveOrganizationData() throws Exception {
		String token = loginAndExtractToken(slugA, multiOrgUser.getEmail(), "password");
		Cookie refreshCookie = loginAndGetRefreshCookie(slugA, multiOrgUser.getEmail(), "password");

		MvcResult switchResult = mockMvc.perform(post("/api/auth/context/organization")
						.header("Authorization", "Bearer " + token)
						.cookie(refreshCookie)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"organizationId":"%s"}
								""".formatted(organizationB.getId())))
				.andExpect(status().isOk())
				.andReturn();

		String switchedToken = extractAccessToken(switchResult);

		mockMvc.perform(get("/api/buildings/{id}", buildingBId)
						.header("Authorization", "Bearer " + switchedToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("BUILDING-B"));

		mockMvc.perform(get("/api/buildings/{id}", buildingAId)
						.header("Authorization", "Bearer " + switchedToken))
				.andExpect(status().isNotFound());
	}

	@Test
	void afterSwitch_permissionsReflectActiveOrganization() throws Exception {
		String token = loginAndExtractToken(slugA, multiOrgUser.getEmail(), "password");
		Cookie refreshCookie = loginAndGetRefreshCookie(slugA, multiOrgUser.getEmail(), "password");

		mockMvc.perform(get("/api/auth/permissions")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.permissions").isArray())
				.andExpect(jsonPath("$.permissions[?(@ == 'BUILDINGS_ADMIN')]").exists());

		MvcResult switchResult = mockMvc.perform(post("/api/auth/context/organization")
						.header("Authorization", "Bearer " + token)
						.cookie(refreshCookie)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"organizationId":"%s"}
								""".formatted(organizationB.getId())))
				.andExpect(status().isOk())
				.andReturn();

		String switchedToken = extractAccessToken(switchResult);

		mockMvc.perform(get("/api/auth/permissions")
						.header("Authorization", "Bearer " + switchedToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.permissions[?(@ == 'BUILDINGS_VIEW')]").exists())
				.andExpect(jsonPath("$.permissions[?(@ == 'BUILDINGS_ADMIN')]").doesNotExist());
	}

	@Test
	void afterSwitch_refreshTokenRotationWorksInTargetOrganization_multiOrgUser() throws Exception {
		String token = loginAndExtractToken(slugA, multiOrgUser.getEmail(), "password");
		Cookie refreshCookie = loginAndGetRefreshCookie(slugA, multiOrgUser.getEmail(), "password");

		MvcResult switchResult = mockMvc.perform(post("/api/auth/context/organization")
						.header("Authorization", "Bearer " + token)
						.cookie(refreshCookie)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"organizationId":"%s"}
								""".formatted(organizationB.getId())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andReturn();

		String switchedToken = extractAccessToken(switchResult);
		Cookie switchedRefreshCookie = switchResult.getResponse().getCookie("m2_refresh_token");
		assertThat(switchedRefreshCookie).isNotNull();

		MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
						.header("Authorization", "Bearer " + switchedToken)
						.cookie(switchedRefreshCookie))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andReturn();

		assertThat(decodeOrganizationId(extractAccessToken(refreshResult))).isEqualTo(organizationB.getId());
	}

	@Test
	void afterSwitch_refreshTokenRotationWorksInTargetOrganization_superAdmin() throws Exception {
		String token = loginAndExtractToken(slugA, superAdminUser.getEmail(), "password");
		Cookie refreshCookie = loginAndGetRefreshCookie(slugA, superAdminUser.getEmail(), "password");

		MvcResult switchResult = mockMvc.perform(post("/api/auth/context/organization")
						.header("Authorization", "Bearer " + token)
						.cookie(refreshCookie)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"organizationId":"%s"}
								""".formatted(organizationB.getId())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andReturn();

		String switchedToken = extractAccessToken(switchResult);
		Cookie switchedRefreshCookie = switchResult.getResponse().getCookie("m2_refresh_token");
		assertThat(switchedRefreshCookie).isNotNull();

		MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
						.header("Authorization", "Bearer " + switchedToken)
						.cookie(switchedRefreshCookie))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andReturn();

		assertThat(decodeOrganizationId(extractAccessToken(refreshResult))).isEqualTo(organizationB.getId());
	}

	@Test
	void superAdmin_canSwitchToAnyExistingOrganization() throws Exception {
		String token = loginAndExtractToken(slugA, superAdminUser.getEmail(), "password");
		Cookie refreshCookie = loginAndGetRefreshCookie(slugA, superAdminUser.getEmail(), "password");

		MvcResult switchResult = mockMvc.perform(post("/api/auth/context/organization")
						.header("Authorization", "Bearer " + token)
						.cookie(refreshCookie)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"organizationId":"%s"}
								""".formatted(organizationC.getId())))
				.andExpect(status().isOk())
				.andReturn();

		assertThat(decodeOrganizationId(extractAccessToken(switchResult))).isEqualTo(organizationC.getId());
	}

	@Test
	void switchOrganization_unknownOrganization_returns404() throws Exception {
		String token = loginAndExtractToken(slugA, singleOrgUser.getEmail(), "password");
		UUID unknownOrgId = UUID.fromString("f0000000-0000-4000-8000-000000000099");

		mockMvc.perform(post("/api/auth/context/organization")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"organizationId":"%s"}
								""".formatted(unknownOrgId)))
				.andExpect(status().isNotFound());
	}

	@Test
	void getContext_returnsUserOrganizationsAndSwitchFlag() throws Exception {
		String token = loginAndExtractToken(slugA, multiOrgUser.getEmail(), "password");

		mockMvc.perform(get("/api/auth/context")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.user.email").value(multiOrgUser.getEmail()))
				.andExpect(jsonPath("$.user.name").value("multi@example.com"))
				.andExpect(jsonPath("$.activeOrganization.slug").value(slugA))
				.andExpect(jsonPath("$.availableOrganizations.length()").value(2))
				.andExpect(jsonPath("$.canSwitchOrganizations").value(true));
	}

	@Test
	void tamperedOrganizationClaim_cannotBypassMembershipValidation() throws Exception {
		String token = loginAndExtractToken(slugA, singleOrgUser.getEmail(), "password");
		String tamperedToken = tamperOrganizationIdClaim(token, organizationB.getId());

		mockMvc.perform(get("/api/buildings/{id}", buildingBId)
						.header("Authorization", "Bearer " + tamperedToken))
				.andExpect(status().isUnauthorized());
	}

	private String loginAndExtractToken(String organizationSlug, String email, String password) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(loginRequest(organizationSlug, email, password)))
				.andExpect(status().isOk())
				.andReturn();
		return extractAccessToken(result);
	}

	private Cookie loginAndGetRefreshCookie(String organizationSlug, String email, String password) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(loginRequest(organizationSlug, email, password)))
				.andExpect(status().isOk())
				.andReturn();
		return result.getResponse().getCookie("m2_refresh_token");
	}

	private String extractAccessToken(MvcResult result) throws Exception {
		JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
		return json.get("accessToken").asText();
	}

	private UUID decodeOrganizationId(String token) {
		String[] parts = token.split("\\.");
		String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
		try {
			JsonNode payload = objectMapper.readTree(payloadJson);
			return UUID.fromString(payload.get("organization_id").asText());
		} catch (Exception ex) {
			throw new IllegalStateException("Unable to decode organization_id claim", ex);
		}
	}

	private String tamperOrganizationIdClaim(String token, UUID newOrganizationId) {
		String[] parts = token.split("\\.");
		String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
		String tamperedPayload = payloadJson.replaceFirst(
				"\"organization_id\":\"[^\"]+\"",
				"\"organization_id\":\"" + newOrganizationId + "\""
		);
		String encodedPayload = Base64.getUrlEncoder().withoutPadding()
				.encodeToString(tamperedPayload.getBytes(StandardCharsets.UTF_8));
		return parts[0] + "." + encodedPayload + "." + parts[2];
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

	private void grantOrganizationAccess(User user, Organization organization) {
		userOrganizationRepository.saveAndFlush(
				new UserOrganization(user.getId(), organization.getId(), Instant.now())
		);
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

	private Building saveBuilding(Organization organization, String code) {
		Building building = new Building();
		building.setOrganization(organization);
		building.setCode(code);
		building.setName(code);
		building.setAddress("Address");
		building.setCity("City");
		building.setNoticePeriodMonths(3);
		building.setStatus(BuildingStatus.ACTIVE);
		return buildingRepository.saveAndFlush(building);
	}
}
