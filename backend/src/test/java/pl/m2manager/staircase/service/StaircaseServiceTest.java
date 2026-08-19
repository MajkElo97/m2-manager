package pl.m2manager.staircase.service;

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
import pl.m2manager.building.entity.Building;
import pl.m2manager.building.entity.BuildingStatus;
import pl.m2manager.building.repository.BuildingRepository;
import pl.m2manager.common.exception.BusinessConflictException;
import pl.m2manager.common.exception.ResourceNotFoundException;
import pl.m2manager.organization.entity.Organization;
import pl.m2manager.organization.repository.OrganizationRepository;
import pl.m2manager.staircase.dto.request.CreateStaircaseRequest;
import pl.m2manager.staircase.dto.request.UpdateStaircaseRequest;
import pl.m2manager.staircase.entity.Staircase;
import pl.m2manager.staircase.repository.StaircaseRepository;
import pl.m2manager.tenant.TenantContext;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class StaircaseServiceTest {

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
	private StaircaseService staircaseService;

	@Autowired
	private StaircaseRepository staircaseRepository;

	@Autowired
	private BuildingRepository buildingRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	private Organization organizationA;
	private Organization organizationB;

	@BeforeEach
	void setUp() {
		organizationA = saveOrganization("Org Staircase Service A");
		organizationB = saveOrganization("Org Staircase Service B");
		when(tenantContext.getCurrentOrganizationId()).thenReturn(organizationA.getId());
	}

	@Test
	void create_usesCurrentTenantContext() {
		Building building = saveBuilding(organizationA, "SRV1");
		var created = staircaseService.create(sampleCreateRequest(building.getId(), "KL100", "1"));

		Staircase persisted = staircaseRepository.findByIdAndOrganizationId(created.id(), organizationA.getId()).orElseThrow();
		assertThat(persisted.getOrganizationId()).isEqualTo(organizationA.getId());
	}

	@Test
	void create_buildingMustBelongToTenant() {
		Building buildingB = saveBuilding(organizationB, "SRV2");

		assertThatThrownBy(() -> staircaseService.create(sampleCreateRequest(buildingB.getId(), "KL101", "1")))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void create_duplicateCode_throwsBusinessConflict() {
		Building building = saveBuilding(organizationA, "SRV3");
		saveStaircase(organizationA.getId(), building.getId(), "KL102", "1");

		assertThatThrownBy(() -> staircaseService.create(sampleCreateRequest(building.getId(), "KL102", "2")))
				.isInstanceOf(BusinessConflictException.class);
	}

	@Test
	void create_duplicateDesignation_throwsBusinessConflict() {
		Building building = saveBuilding(organizationA, "SRV4");
		saveStaircase(organizationA.getId(), building.getId(), "KL103", "1");

		assertThatThrownBy(() -> staircaseService.create(sampleCreateRequest(building.getId(), "KL104", "1")))
				.isInstanceOf(BusinessConflictException.class);
	}

	@Test
	void getById_tenantIsolation() {
		Building buildingA = saveBuilding(organizationA, "SRV5");
		Building buildingB = saveBuilding(organizationB, "SRV6");
		Staircase staircaseA = saveStaircase(organizationA.getId(), buildingA.getId(), "KL105", "1");
		Staircase staircaseB = saveStaircase(organizationB.getId(), buildingB.getId(), "KL106", "1");

		assertThat(staircaseService.getById(staircaseA.getId()).code()).isEqualTo("KL105");

		assertThatThrownBy(() -> staircaseService.getById(staircaseB.getId()))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void getAllForOrganization_returnsOnlyCurrentTenantStaircases() {
		Building buildingA = saveBuilding(organizationA, "SRV9");
		Building buildingB = saveBuilding(organizationB, "SRV10");
		saveStaircase(organizationA.getId(), buildingA.getId(), "KL109", "1");
		saveStaircase(organizationB.getId(), buildingB.getId(), "KL110", "1");

		assertThat(staircaseService.getAllForOrganization())
				.extracting(response -> response.code())
				.containsExactly("KL109");
	}

	@Test
	void update_tenantIsolation() {
		Building buildingB = saveBuilding(organizationB, "SRV7");
		Staircase staircaseB = saveStaircase(organizationB.getId(), buildingB.getId(), "KL107", "1");

		assertThatThrownBy(() -> staircaseService.update(staircaseB.getId(), sampleUpdateRequest("KL107", "1")))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void delete_tenantIsolation() {
		Building buildingB = saveBuilding(organizationB, "SRV8");
		Staircase staircaseB = saveStaircase(organizationB.getId(), buildingB.getId(), "KL108", "1");

		assertThatThrownBy(() -> staircaseService.delete(staircaseB.getId()))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	private CreateStaircaseRequest sampleCreateRequest(UUID buildingId, String code, String designation) {
		return new CreateStaircaseRequest(
				buildingId,
				code,
				designation,
				null,
				false,
				false,
				4,
				null
		);
	}

	private UpdateStaircaseRequest sampleUpdateRequest(String code, String designation) {
		return new UpdateStaircaseRequest(
				code,
				designation,
				null,
				false,
				false,
				4,
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

	private Staircase saveStaircase(UUID organizationId, UUID buildingId, String code, String designation) {
		Staircase staircase = new Staircase();
		staircase.setOrganizationId(organizationId);
		staircase.setBuildingId(buildingId);
		staircase.setCode(code);
		staircase.setDesignation(designation);
		staircase.setFloors(4);
		return staircaseRepository.saveAndFlush(staircase);
	}
}
