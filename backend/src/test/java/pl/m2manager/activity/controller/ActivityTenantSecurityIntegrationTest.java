package pl.m2manager.activity.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import pl.m2manager.activity.entity.Activity;
import pl.m2manager.activity.entity.ActivityPlanningType;
import pl.m2manager.activity.entity.ActivityPriority;
import pl.m2manager.activity.repository.ActivityRepository;
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
import pl.m2manager.user.entity.UserRole;
import pl.m2manager.user.repository.UserRepository;
import pl.m2manager.user.repository.UserRoleRepository;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("jwt-it")
@Testcontainers(disabledWithoutDocker = true)
class ActivityTenantSecurityIntegrationTest {

	private static final ObjectMapper objectMapper = new ObjectMapper();

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
	private ActivityRepository activityRepository;

	@Autowired
	private BuildingRepository buildingRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private String slugA;
	private String slugB;
	private Organization organizationA;
	private Organization organizationB;
	private User adminA;
	private User adminB;
	private User superAdmin;
	private Activity systemActivity;
	private Activity orgActivityA;
	private Activity orgActivityB;
	private UUID buildingAId;
	private UUID buildingBId;
	private String activitySuffix;

	@BeforeEach
	void setUp() {
		String suffix = UUID.randomUUID().toString().substring(0, 8);
		activitySuffix = suffix;
		slugA = "act-a-" + suffix;
		slugB = "act-b-" + suffix;

		organizationA = saveOrganization("Activity Org A", slugA);
		organizationB = saveOrganization("Activity Org B", slugB);

		adminA = saveUser(organizationA, "admin-a@example.com", "password");
		adminB = saveUser(organizationB, "admin-b@example.com", "password");
		superAdmin = saveUser(organizationA, "super@example.com", "password");

		Role activitiesAdminA = saveRole(organizationA, "Activities Admin A");
		assignActivitiesPermissions(activitiesAdminA);
		assignRole(adminA, activitiesAdminA, organizationA);

		Role activitiesAdminB = saveRole(organizationB, "Activities Admin B");
		assignActivitiesPermissions(activitiesAdminB);
		assignRole(adminB, activitiesAdminB, organizationB);

		Role superAdminRole = saveRole(organizationA, "SUPER_ADMIN");
		superAdminRole.setSystemRole(true);
		roleRepository.saveAndFlush(superAdminRole);
		for (var permission : permissionRepository.findAll()) {
			rolePermissionRepository.saveAndFlush(new RolePermission(superAdminRole.getId(), permission.getId()));
		}
		assignRole(superAdmin, superAdminRole, organizationA);

		systemActivity = saveSystemActivity("CZ" + suffix, "Tereny zewnętrzne test");
		orgActivityA = saveOrganizationActivity(organizationA, "ORG-A" + suffix, "Czyszczenie placu zabaw");
		orgActivityB = saveOrganizationActivity(organizationB, "ORG-B" + suffix, "Mycie paneli fotowoltaicznych");

		buildingAId = saveBuilding(organizationA, "BUILD-A").getId();
		buildingBId = saveBuilding(organizationB, "BUILD-B").getId();
	}

	@Test
	void systemActivity_isVisibleForOrganizationA() throws Exception {
		String token = login(slugA, adminA.getEmail());

		mockMvc.perform(get("/api/activities/{id}", systemActivity.getId())
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(systemActivity.getCode()))
				.andExpect(jsonPath("$.system").value(true));
	}

	@Test
	void systemActivity_isVisibleForOrganizationB() throws Exception {
		String token = login(slugB, adminB.getEmail());

		mockMvc.perform(get("/api/activities/{id}", systemActivity.getId())
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.system").value(true));
	}

	@Test
	void organizationA_seesOwnActivity() throws Exception {
		String token = login(slugA, adminA.getEmail());

		mockMvc.perform(get("/api/activities/{id}", orgActivityA.getId())
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(orgActivityA.getCode()))
				.andExpect(jsonPath("$.system").value(false))
				.andExpect(jsonPath("$.manageable").value(true));
	}

	@Test
	void organizationB_seesOwnActivity() throws Exception {
		String token = login(slugB, adminB.getEmail());

		mockMvc.perform(get("/api/activities/{id}", orgActivityB.getId())
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value(orgActivityB.getCode()));
	}

	@Test
	void organizationA_doesNotSeeOrganizationBActivity() throws Exception {
		String token = login(slugA, adminA.getEmail());

		mockMvc.perform(get("/api/activities/{id}", orgActivityB.getId())
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isNotFound());
	}

	@Test
	void organizationB_doesNotSeeOrganizationAActivity() throws Exception {
		String token = login(slugB, adminB.getEmail());

		mockMvc.perform(get("/api/activities/{id}", orgActivityA.getId())
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isNotFound());
	}

	@Test
	void adminOrganizationA_canCreateOwnActivity() throws Exception {
		String token = login(slugA, adminA.getEmail());

		MvcResult result = mockMvc.perform(post("/api/activities")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Nowa czynność A",
								  "category": "Sprzątanie",
								  "planningType": "CYCLIC",
								  "priority": "NORMAL"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.system").value(false))
				.andExpect(jsonPath("$.manageable").value(true))
				.andReturn();

		JsonNode created = objectMapper.readTree(result.getResponse().getContentAsString());
		assertThat(created.get("code").asText()).startsWith("ORG-");
		UUID createdId = UUID.fromString(created.get("id").asText());
		Activity saved = activityRepository.findById(createdId).orElseThrow();
		assertThat(saved.getOrganizationId()).isEqualTo(organizationA.getId());
	}

	@Test
	void adminOrganizationA_canUpdateOwnActivity() throws Exception {
		String token = login(slugA, adminA.getEmail());

		mockMvc.perform(put("/api/activities/{id}", orgActivityA.getId())
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(updatePayload(orgActivityA.getCode(), "Czyszczenie placu zabaw - zmiana")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Czyszczenie placu zabaw - zmiana"));
	}

	@Test
	void adminOrganizationA_cannotUpdateSystemActivity() throws Exception {
		String token = login(slugA, adminA.getEmail());

		mockMvc.perform(put("/api/activities/{id}", systemActivity.getId())
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(updatePayload(systemActivity.getCode(), "Zmiana systemowa")))
				.andExpect(status().isForbidden());
	}

	@Test
	void adminOrganizationA_cannotUpdateOrganizationBActivity() throws Exception {
		String token = login(slugA, adminA.getEmail());

		mockMvc.perform(put("/api/activities/{id}", orgActivityB.getId())
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(updatePayload(orgActivityB.getCode(), "Próba edycji B")))
				.andExpect(status().isNotFound());
	}

	@Test
	void superAdmin_canManageSystemActivity() throws Exception {
		String token = login(slugA, superAdmin.getEmail());

		mockMvc.perform(put("/api/activities/{id}", systemActivity.getId())
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(updatePayload(systemActivity.getCode(), "Tereny zewnętrzne - super admin")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.manageable").value(true))
				.andExpect(jsonPath("$.name").value("Tereny zewnętrzne - super admin"));
	}

	@Test
	void adminOrganizationA_cannotDeactivateSystemActivity() throws Exception {
		String token = login(slugA, adminA.getEmail());

		mockMvc.perform(delete("/api/activities/{id}", systemActivity.getId())
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isForbidden());
	}

	@Test
	void superAdmin_canDeactivateSystemActivity() throws Exception {
		String token = login(slugA, superAdmin.getEmail());

		mockMvc.perform(delete("/api/activities/{id}", systemActivity.getId())
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isNoContent());
	}

	@Test
	void adminOrganizationA_canDeactivateOwnActivity() throws Exception {
		String token = login(slugA, adminA.getEmail());

		mockMvc.perform(delete("/api/activities/{id}", orgActivityA.getId())
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isNoContent());
	}

	@Test
	void globalActivity_canBeUsedInScopesForOrganizationAAndB() throws Exception {
		String tokenA = login(slugA, adminA.getEmail());
		String tokenB = login(slugB, adminB.getEmail());

		mockMvc.perform(post("/api/scopes")
						.header("Authorization", "Bearer " + tokenA)
						.contentType(MediaType.APPLICATION_JSON)
						.content(scopePayload("ZP-A-SYS-" + activitySuffix, buildingAId, systemActivity.getId())))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/scopes")
						.header("Authorization", "Bearer " + tokenB)
						.contentType(MediaType.APPLICATION_JSON)
						.content(scopePayload("ZP-B-SYS-" + activitySuffix, buildingBId, systemActivity.getId())))
				.andExpect(status().isCreated());
	}

	@Test
	void organizationAActivity_canBeUsedOnlyInOrganizationAScope() throws Exception {
		String tokenA = login(slugA, adminA.getEmail());
		String tokenB = login(slugB, adminB.getEmail());

		mockMvc.perform(post("/api/scopes")
						.header("Authorization", "Bearer " + tokenA)
						.contentType(MediaType.APPLICATION_JSON)
						.content(scopePayload("ZP-A-ORG-" + activitySuffix, buildingAId, orgActivityA.getId())))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/scopes")
						.header("Authorization", "Bearer " + tokenB)
						.contentType(MediaType.APPLICATION_JSON)
						.content(scopePayload("ZP-B-ORG-" + activitySuffix, buildingBId, orgActivityA.getId())))
				.andExpect(status().isNotFound());
	}

	@Test
	void list_includesSystemAndOwnActivitiesOnly() throws Exception {
		String token = login(slugA, adminA.getEmail());

		MvcResult result = mockMvc.perform(get("/api/activities")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andReturn();

		JsonNode activities = objectMapper.readTree(result.getResponse().getContentAsString());
		List<String> codes = java.util.stream.StreamSupport.stream(activities.spliterator(), false)
				.map(node -> node.get("code").asText())
				.toList();

		assertThat(codes).contains(systemActivity.getCode(), orgActivityA.getCode());
		assertThat(codes).doesNotContain(orgActivityB.getCode());
	}

	private String updatePayload(String code, String name) {
		return """
				{
				  "code": "%s",
				  "name": "%s",
				  "category": "Sprzątanie",
				  "planningType": "CYCLIC",
				  "priority": "NORMAL",
				  "active": true
				}
				""".formatted(code, name);
	}

	private String scopePayload(String code, UUID buildingId, UUID activityId) {
		return """
				{
				  "code": "%s",
				  "buildingId": "%s",
				  "activityId": "%s",
				  "planningType": "WEEKLY",
				  "frequency": 1,
				  "weekdays": "MON"
				}
				""".formatted(code, buildingId, activityId);
	}

	private String login(String organizationSlug, String email) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "organizationSlug": "%s",
								  "email": "%s",
								  "password": "password"
								}
								""".formatted(organizationSlug, email)))
				.andExpect(status().isOk())
				.andReturn();
		return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
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

	private Role saveRole(Organization organization, String name) {
		Role role = new Role();
		role.setOrganization(organization);
		role.setName(name);
		return roleRepository.saveAndFlush(role);
	}

	private void assignActivitiesPermissions(Role role) {
		for (String permissionCode : List.of(
				"ACTIVITIES_VIEW",
				"ACTIVITIES_CREATE",
				"ACTIVITIES_EDIT",
				"ACTIVITIES_DELETE",
				"SCOPES_VIEW",
				"SCOPES_CREATE"
		)) {
			assignPermission(role, permissionCode);
		}
	}

	private void assignPermission(Role role, String permissionCode) {
		var permission = permissionRepository.findByCode(permissionCode).orElseThrow();
		rolePermissionRepository.saveAndFlush(new RolePermission(role.getId(), permission.getId()));
	}

	private void assignRole(User user, Role role, Organization organization) {
		userRoleRepository.saveAndFlush(new UserRole(organization.getId(), user.getId(), role.getId()));
	}

	private Activity saveSystemActivity(String code, String name) {
		Activity activity = new Activity();
		activity.setCode(code);
		activity.setName(name);
		activity.setCategory("Sprzątanie");
		activity.setPlanningType(ActivityPlanningType.CYCLIC);
		activity.setPriority(ActivityPriority.NORMAL);
		activity.setActive(true);
		return activityRepository.saveAndFlush(activity);
	}

	private Activity saveOrganizationActivity(Organization organization, String code, String name) {
		Activity activity = new Activity();
		activity.setCode(code);
		activity.setName(name);
		activity.setCategory("Sprzątanie");
		activity.setPlanningType(ActivityPlanningType.CYCLIC);
		activity.setPriority(ActivityPriority.NORMAL);
		activity.setActive(true);
		activity.setOrganizationId(organization.getId());
		return activityRepository.saveAndFlush(activity);
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
