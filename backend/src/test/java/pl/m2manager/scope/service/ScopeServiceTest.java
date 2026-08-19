package pl.m2manager.scope.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
import pl.m2manager.common.exception.ResourceNotFoundException;
import pl.m2manager.organization.entity.Organization;
import pl.m2manager.organization.repository.OrganizationRepository;
import pl.m2manager.scope.dto.request.CreateScopeRequest;
import pl.m2manager.scope.entity.ScopePlanningType;
import pl.m2manager.scope.repository.ActivityScopeRepository;
import pl.m2manager.tenant.TenantContext;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class ScopeServiceTest {

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

	@MockitoBean
	private TenantContext tenantContext;

	@Autowired
	private ScopeService scopeService;

	@Autowired
	private ActivityScopeRepository scopeRepository;

	@Autowired
	private BuildingRepository buildingRepository;

	@Autowired
	private ActivityRepository activityRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	private Organization organizationA;
	private Organization organizationB;

	@BeforeEach
	void setUp() {
		organizationA = saveOrganization("Scope Org A");
		organizationB = saveOrganization("Scope Org B");
		when(tenantContext.getCurrentOrganizationId()).thenReturn(organizationA.getId());
	}

	@Test
	void create_buildingMustBelongToTenant() {
		Building buildingB = saveBuilding(organizationB, "BLD-B");
		Activity activity = saveActivity("CZ9001");

		assertThatThrownBy(() -> scopeService.create(new CreateScopeRequest(
				"ZP9001",
				buildingB.getId(),
				activity.getId(),
				ScopePlanningType.WEEKLY,
				1,
				"Wtorek",
				null
		))).isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void getById_tenantIsolation() {
		Building buildingA = saveBuilding(organizationA, "BLD-A");
		Building buildingB = saveBuilding(organizationB, "BLD-B");
		Activity activity = saveActivity("CZ9002");
		var scopeA = scopeService.create(new CreateScopeRequest(
				"ZP9002", buildingA.getId(), activity.getId(), ScopePlanningType.WEEKLY, 1, "Wtorek", null
		));

		when(tenantContext.getCurrentOrganizationId()).thenReturn(organizationB.getId());
		assertThatThrownBy(() -> scopeService.getById(scopeA.id())).isInstanceOf(ResourceNotFoundException.class);
	}

	private Organization saveOrganization(String name) {
		Organization organization = new Organization();
		organization.setName(name);
		organization.setSlug(name.toLowerCase().replace(' ', '-') + "-" + UUID.randomUUID().toString().substring(0, 8));
		organization.setTimezone("Europe/Warsaw");
		return organizationRepository.saveAndFlush(organization);
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

	private Activity saveActivity(String code) {
		Activity activity = new Activity();
		activity.setCode(code);
		activity.setName(code);
		activity.setCategory("Sprzątanie");
		activity.setPlanningType(ActivityPlanningType.CYCLIC);
		activity.setDurationMinutes(30);
		activity.setPriority(ActivityPriority.NORMAL);
		activity.setActive(true);
		return activityRepository.saveAndFlush(activity);
	}
}
