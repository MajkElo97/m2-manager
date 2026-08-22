package pl.m2manager.building.controller;

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
import pl.m2manager.staircase.entity.Staircase;
import pl.m2manager.staircase.repository.StaircaseRepository;
import pl.m2manager.user.entity.User;
import pl.m2manager.user.entity.UserRole;
import pl.m2manager.user.repository.UserRepository;
import pl.m2manager.user.repository.UserRoleRepository;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("jwt-it")
@Testcontainers(disabledWithoutDocker = true)
class BuildingPermanentDeleteIntegrationTest {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	private static final UUID DEV_ORGANIZATION_ID = UUID.fromString("a0000000-0000-4000-8000-000000000001");
	private static final UUID SECONDARY_ORGANIZATION_ID = UUID.fromString("a0000000-0000-4000-8000-000000000002");
	private static final UUID INACTIVE_BUILDING_WITHOUT_DEPS = UUID.fromString("d0000000-0000-4000-8000-000000000008");
	private static final UUID ACTIVE_BUILDING_WITH_STAIRCASE = UUID.fromString("d0000000-0000-4000-8000-000000000001");
	private static final UUID BIURO_ROLE_ID = UUID.fromString("b0000000-0000-4000-8000-000000000011");
	private static final UUID KOORDYNATOR_ROLE_ID = UUID.fromString("b0000000-0000-4000-8000-000000000012");

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
	private BuildingRepository buildingRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserRoleRepository userRoleRepository;

	@Autowired
	private StaircaseRepository staircaseRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private String biuroEmail;
	private String koordynatorEmail;

	@BeforeEach
	void setUp() {
		String suffix = UUID.randomUUID().toString().substring(0, 8);
		biuroEmail = "biuro-" + suffix + "@example.com";
		koordynatorEmail = "koord-" + suffix + "@example.com";

		Organization devOrganization = organizationRepository.findById(DEV_ORGANIZATION_ID).orElseThrow();
		User biuroUser = saveUser(devOrganization, biuroEmail, "password");
		User koordynatorUser = saveUser(devOrganization, koordynatorEmail, "password");
		assignRole(biuroUser, BIURO_ROLE_ID, DEV_ORGANIZATION_ID);
		assignRole(koordynatorUser, KOORDYNATOR_ROLE_ID, DEV_ORGANIZATION_ID);
	}

	@Test
	void superAdmin_canPermanentlyDeleteInactiveBuildingInActiveBusinessOrganization() throws Exception {
		Organization devOrganization = organizationRepository.findById(DEV_ORGANIZATION_ID).orElseThrow();
		Building deletableBuilding = saveInactiveBuilding(devOrganization, "DEL-OK-" + UUID.randomUUID().toString().substring(0, 6));
		UUID buildingId = deletableBuilding.getId();

		String token = switchSuperAdminToBusinessOrganization();

		mockMvc.perform(delete("/api/buildings/{id}/permanent", buildingId)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isNoContent());

		assertThat(buildingRepository.findById(buildingId)).isEmpty();
	}

	@Test
	void superAdmin_cannotDeleteActiveBuilding_returns409() throws Exception {
		String token = switchSuperAdminToBusinessOrganization();

		mockMvc.perform(delete("/api/buildings/{id}/permanent", ACTIVE_BUILDING_WITH_STAIRCASE)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value(
						"Aktywnego budynku nie można usunąć na stałe. Najpierw go dezaktywuj."
				));

		assertThat(buildingRepository.findById(ACTIVE_BUILDING_WITH_STAIRCASE)).isPresent();
	}

	@Test
	void admin_cannotPermanentDelete_returns403() throws Exception {
		String token = login("m2-manager-dev", "multiadmin@m2manager.local", "Admin123!");

		mockMvc.perform(delete("/api/buildings/{id}/permanent", INACTIVE_BUILDING_WITHOUT_DEPS)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isForbidden());
	}

	@Test
	void biuro_cannotPermanentDelete_returns403() throws Exception {
		String token = login("m2-manager-dev", biuroEmail, "password");

		mockMvc.perform(delete("/api/buildings/{id}/permanent", INACTIVE_BUILDING_WITHOUT_DEPS)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isForbidden());
	}

	@Test
	void koordynator_cannotPermanentDelete_returns403() throws Exception {
		String token = login("m2-manager-dev", koordynatorEmail, "password");

		mockMvc.perform(delete("/api/buildings/{id}/permanent", INACTIVE_BUILDING_WITHOUT_DEPS)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isForbidden());
	}

	@Test
	void superAdmin_cannotDeleteOtherOrganizationBuilding_returns404() throws Exception {
		Organization secondaryOrganization = organizationRepository.findById(SECONDARY_ORGANIZATION_ID).orElseThrow();
		Building secondaryBuilding = saveInactiveBuilding(
				secondaryOrganization,
				"OTHER-" + UUID.randomUUID().toString().substring(0, 6)
		);

		String token = switchSuperAdminToBusinessOrganization();

		mockMvc.perform(delete("/api/buildings/{id}/permanent", secondaryBuilding.getId())
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));

		assertThat(buildingRepository.findById(secondaryBuilding.getId())).isPresent();
	}

	@Test
	void superAdmin_cannotDeleteInSystemOrganizationContext_returns403() throws Exception {
		String token = loginSuperAdmin();

		mockMvc.perform(delete("/api/buildings/{id}/permanent", INACTIVE_BUILDING_WITHOUT_DEPS)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isForbidden());
	}

	@Test
	void buildingWithBlockingDependencies_returns409() throws Exception {
		Organization devOrganization = organizationRepository.findById(DEV_ORGANIZATION_ID).orElseThrow();
		Building building = saveInactiveBuilding(devOrganization, "BLOCK-" + UUID.randomUUID().toString().substring(0, 6));
		saveStaircase(devOrganization.getId(), building.getId(), "KL-BLOCK");

		String token = switchSuperAdminToBusinessOrganization();

		mockMvc.perform(delete("/api/buildings/{id}/permanent", building.getId())
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value(
						"Nie można usunąć budynku, ponieważ posiada: 1 klatka"
				));

		assertThat(buildingRepository.findById(building.getId())).isPresent();
	}

	@Test
	void failedDeleteDueToDependencies_leavesBuildingInDatabase() throws Exception {
		Organization devOrganization = organizationRepository.findById(DEV_ORGANIZATION_ID).orElseThrow();
		Building isolatedInactiveBuilding = saveInactiveBuilding(devOrganization, "ISO-" + UUID.randomUUID().toString().substring(0, 6));
		saveStaircase(devOrganization.getId(), isolatedInactiveBuilding.getId(), "KL-ISO");

		String token = switchSuperAdminToBusinessOrganization();

		mockMvc.perform(delete("/api/buildings/{id}/permanent", isolatedInactiveBuilding.getId())
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isConflict());

		assertThat(buildingRepository.findById(isolatedInactiveBuilding.getId())).isPresent();
		assertThat(staircaseRepository.countByOrganizationIdAndBuildingId(
				devOrganization.getId(),
				isolatedInactiveBuilding.getId()
		)).isEqualTo(1);
	}

	@Test
	void afterSuccessfulDelete_buildingDoesNotExistInDatabase() throws Exception {
		Organization devOrganization = organizationRepository.findById(DEV_ORGANIZATION_ID).orElseThrow();
		Building deletableBuilding = saveInactiveBuilding(devOrganization, "DEL-" + UUID.randomUUID().toString().substring(0, 6));
		UUID buildingId = deletableBuilding.getId();

		String token = switchSuperAdminToBusinessOrganization();

		mockMvc.perform(delete("/api/buildings/{id}/permanent", buildingId)
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isNoContent());

		assertThat(buildingRepository.findById(buildingId)).isEmpty();
	}

	private String switchSuperAdminToBusinessOrganization() throws Exception {
		String token = loginSuperAdmin();
		Cookie refreshCookie = loginRefreshCookie("admin", "admin@m2manager.local", "Admin123!");

		MvcResult switchResult = mockMvc.perform(post("/api/auth/context/organization")
						.header("Authorization", "Bearer " + token)
						.cookie(refreshCookie)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"organizationId":"%s"}
								""".formatted(DEV_ORGANIZATION_ID)))
				.andExpect(status().isOk())
				.andReturn();

		return objectMapper.readTree(switchResult.getResponse().getContentAsString()).get("accessToken").asText();
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

	private User saveUser(Organization organization, String email, String rawPassword) {
		User user = new User();
		user.setOrganization(organization);
		user.setEmail(email);
		user.setPasswordHash(passwordEncoder.encode(rawPassword));
		return userRepository.saveAndFlush(user);
	}

	private void assignRole(User user, UUID roleId, UUID organizationId) {
		userRoleRepository.saveAndFlush(new UserRole(organizationId, user.getId(), roleId));
	}

	private Building saveInactiveBuilding(Organization organization, String code) {
		Building building = new Building();
		building.setOrganization(organization);
		building.setCode(code);
		building.setName(code);
		building.setAddress("Address");
		building.setCity("City");
		building.setNoticePeriodMonths(3);
		building.setStatus(BuildingStatus.INACTIVE);
		return buildingRepository.saveAndFlush(building);
	}

	private void saveStaircase(UUID organizationId, UUID buildingId, String code) {
		Staircase staircase = new Staircase();
		staircase.setOrganizationId(organizationId);
		staircase.setBuildingId(buildingId);
		staircase.setCode(code);
		staircase.setDesignation("1");
		staircase.setFloors(4);
		staircaseRepository.saveAndFlush(staircase);
	}
}
