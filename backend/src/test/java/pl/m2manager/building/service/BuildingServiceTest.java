package pl.m2manager.building.service;

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
import pl.m2manager.building.dto.request.CreateBuildingRequest;
import pl.m2manager.building.dto.request.UpdateBuildingRequest;
import pl.m2manager.building.entity.Building;
import pl.m2manager.building.entity.BuildingStatus;
import pl.m2manager.building.repository.BuildingRepository;
import pl.m2manager.common.exception.BusinessConflictException;
import pl.m2manager.common.exception.ResourceNotFoundException;
import pl.m2manager.organization.entity.Organization;
import pl.m2manager.organization.repository.OrganizationRepository;
import pl.m2manager.tenant.TenantContext;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class BuildingServiceTest {

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
	private BuildingService buildingService;

	@Autowired
	private BuildingRepository buildingRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	private Organization organizationA;
	private Organization organizationB;

	@BeforeEach
	void setUp() {
		organizationA = saveOrganization("Org Service A");
		organizationB = saveOrganization("Org Service B");
		when(tenantContext.getCurrentOrganizationId()).thenReturn(organizationA.getId());
	}

	@Test
	void create_usesCurrentTenantContext() {
		var created = buildingService.create(sampleCreateRequest("NEWCODE"));

		Building persisted = buildingRepository.findByIdAndOrganizationId(created.id(), organizationA.getId()).orElseThrow();
		assertThat(persisted.getOrganization().getId()).isEqualTo(organizationA.getId());
	}

	@Test
	void create_duplicateCode_throwsBusinessConflict() {
		saveBuilding(organizationA, "DUPLICATE");

		assertThatThrownBy(() -> buildingService.create(sampleCreateRequest("DUPLICATE")))
				.isInstanceOf(BusinessConflictException.class);
	}

	@Test
	void getById_tenantIsolation() {
		Building buildingA = saveBuilding(organizationA, "A1");
		Building buildingB = saveBuilding(organizationB, "B1");

		assertThat(buildingService.getById(buildingA.getId()).code()).isEqualTo("A1");

		assertThatThrownBy(() -> buildingService.getById(buildingB.getId()))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void update_tenantIsolation() {
		Building buildingB = saveBuilding(organizationB, "UPDATEB");

		assertThatThrownBy(() -> buildingService.update(
				buildingB.getId(),
				sampleUpdateRequest("UPDATEB", BuildingStatus.ACTIVE)
		)).isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void deactivate_tenantIsolation() {
		Building buildingB = saveBuilding(organizationB, "DELETEB");

		assertThatThrownBy(() -> buildingService.deactivate(buildingB.getId()))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void create_serviceStartDateBeforeContractSignedAt_rejected() {
		var request = new CreateBuildingRequest(
				"DATE1",
				"Name",
				"Address",
				"City",
				null,
				null,
				null,
				null,
				null,
				null,
				LocalDate.of(2026, 10, 1),
				LocalDate.of(2026, 7, 23),
				3,
				null
		);

		assertThatThrownBy(() -> buildingService.create(request))
				.isInstanceOf(BusinessConflictException.class);
	}

	@Test
	void deactivate_alreadyInactive_isIdempotent() {
		Building building = saveBuilding(organizationA, "INACTIVE", BuildingStatus.INACTIVE);

		buildingService.deactivate(building.getId());

		Building reloaded = buildingRepository.findById(building.getId()).orElseThrow();
		assertThat(reloaded.getStatus()).isEqualTo(BuildingStatus.INACTIVE);
	}

	private CreateBuildingRequest sampleCreateRequest(String code) {
		return new CreateBuildingRequest(
				code,
				"Name",
				"Address",
				"City",
				null,
				null,
				null,
				"ZA0001",
				"OP0001",
				"E0001",
				null,
				LocalDate.of(2025, 1, 5),
				3,
				null
		);
	}

	private UpdateBuildingRequest sampleUpdateRequest(String code, BuildingStatus status) {
		return new UpdateBuildingRequest(
				code,
				"Updated",
				"Updated Address",
				"Updated City",
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				LocalDate.of(2025, 1, 5),
				3,
				status,
				null
		);
	}

	private Organization saveOrganization(String name) {
		Organization organization = new Organization();
		organization.setName(name);
		organization.setSlug(name.toLowerCase().replace(' ', '-') + "-" + UUID.randomUUID().toString().substring(0, 8));
		organization.setTimezone("Europe/Warsaw");
		return organizationRepository.saveAndFlush(organization);
	}

	private Building saveBuilding(Organization organization, String code) {
		return saveBuilding(organization, code, BuildingStatus.ACTIVE);
	}

	private Building saveBuilding(Organization organization, String code, BuildingStatus status) {
		Building building = new Building();
		building.setOrganization(organization);
		building.setCode(code);
		building.setName(code);
		building.setAddress("Address");
		building.setCity("City");
		building.setNoticePeriodMonths(3);
		building.setStatus(status);
		return buildingRepository.saveAndFlush(building);
	}
}
