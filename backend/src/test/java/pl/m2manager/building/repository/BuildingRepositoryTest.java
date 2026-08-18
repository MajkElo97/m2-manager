package pl.m2manager.building.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.m2manager.building.entity.Building;
import pl.m2manager.building.entity.BuildingStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import pl.m2manager.organization.entity.Organization;
import pl.m2manager.organization.repository.OrganizationRepository;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class BuildingRepositoryTest {

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
	private BuildingRepository buildingRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void flywayV8_createsBuildingsTable() {
		Integer count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'buildings'",
				Integer.class
		);
		assertThat(count).isEqualTo(1);
	}

	@Test
	void create_persistsBuilding() {
		Organization organization = saveOrganization("Org Building Persist");
		Building building = saveBuilding(organization, "CODE1", BuildingStatus.ACTIVE);

		assertThat(building.getId()).isNotNull();
		assertThat(buildingRepository.findByIdAndOrganizationId(building.getId(), organization.getId())).isPresent();
	}

	@Test
	void findByOrganization_returnsTenantBuildings() {
		Organization organization = saveOrganization("Org Building List");
		saveBuilding(organization, "LIST1", BuildingStatus.ACTIVE);
		saveBuilding(organization, "LIST2", BuildingStatus.INACTIVE);

		assertThat(buildingRepository.findAllByOrganizationIdAndFilters(organization.getId(), null, null)).hasSize(2);
		assertThat(buildingRepository.findAllByOrganizationIdAndFilters(organization.getId(), BuildingStatus.ACTIVE, null))
				.hasSize(1);
	}

	@Test
	void duplicateCode_rejectedWithinOrganization() {
		Organization organization = saveOrganization("Org Building Duplicate");
		saveBuilding(organization, "DUP", BuildingStatus.ACTIVE);

		Building duplicate = new Building();
		duplicate.setOrganization(organization);
		duplicate.setCode("DUP");
		duplicate.setName("Duplicate");
		duplicate.setAddress("Address");
		duplicate.setCity("City");
		duplicate.setNoticePeriodMonths(3);
		duplicate.setStatus(BuildingStatus.ACTIVE);

		assertThatThrownBy(() -> buildingRepository.saveAndFlush(duplicate))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void sameCode_allowedInDifferentOrganizations() {
		Organization orgA = saveOrganization("Org Building A");
		Organization orgB = saveOrganization("Org Building B");
		saveBuilding(orgA, "SHARED", BuildingStatus.ACTIVE);
		Building buildingB = saveBuilding(orgB, "SHARED", BuildingStatus.ACTIVE);

		assertThat(buildingB.getId()).isNotNull();
	}

	@Test
	void crossTenantFind_cannotAccessAnotherOrganization() {
		Organization orgA = saveOrganization("Org Building Cross A");
		Organization orgB = saveOrganization("Org Building Cross B");
		Building buildingB = saveBuilding(orgB, "CROSS", BuildingStatus.ACTIVE);

		assertThat(buildingRepository.findByIdAndOrganizationId(buildingB.getId(), orgA.getId())).isEmpty();
	}

	@Test
	void status_persists() {
		Organization organization = saveOrganization("Org Building Status");
		Building building = saveBuilding(organization, "STATUS", BuildingStatus.INACTIVE);

		Building reloaded = buildingRepository.findById(building.getId()).orElseThrow();
		assertThat(reloaded.getStatus()).isEqualTo(BuildingStatus.INACTIVE);
	}

	@Test
	void dates_persistCorrectly() {
		Organization organization = saveOrganization("Org Building Dates");
		Building building = new Building();
		building.setOrganization(organization);
		building.setCode("DATES");
		building.setName("Dates Building");
		building.setAddress("Address");
		building.setCity("City");
		building.setContractSignedAt(LocalDate.of(2026, 7, 23));
		building.setServiceStartDate(LocalDate.of(2026, 10, 1));
		building.setNoticePeriodMonths(2);
		building.setStatus(BuildingStatus.ACTIVE);
		buildingRepository.saveAndFlush(building);

		Building reloaded = buildingRepository.findById(building.getId()).orElseThrow();
		assertThat(reloaded.getContractSignedAt()).isEqualTo(LocalDate.of(2026, 7, 23));
		assertThat(reloaded.getServiceStartDate()).isEqualTo(LocalDate.of(2026, 10, 1));
	}

	@Test
	void search_matchesCodeNameAddressCity() {
		Organization organization = saveOrganization("Org Building Search");
		saveBuilding(organization, "KASPRZAKA6", "Kasprzaka 6", "ul. Kasprzaka 6", "Dąbrowa Górnicza");

		assertThat(buildingRepository.findAllByOrganizationIdAndFilters(organization.getId(), null, "kasprzaka")).hasSize(1);
		assertThat(buildingRepository.findAllByOrganizationIdAndFilters(organization.getId(), null, "Dąbrowa")).hasSize(1);
	}

	private Organization saveOrganization(String name) {
		Organization organization = new Organization();
		organization.setName(name);
		organization.setSlug(name.toLowerCase().replace(' ', '-') + "-" + UUID.randomUUID().toString().substring(0, 8));
		organization.setTimezone("Europe/Warsaw");
		return organizationRepository.saveAndFlush(organization);
	}

	private Building saveBuilding(Organization organization, String code, BuildingStatus status) {
		return saveBuilding(organization, code, code, "Address", "City", status);
	}

	private Building saveBuilding(
			Organization organization,
			String code,
			String name,
			String address,
			String city
	) {
		return saveBuilding(organization, code, name, address, city, BuildingStatus.ACTIVE);
	}

	private Building saveBuilding(
			Organization organization,
			String code,
			String name,
			String address,
			String city,
			BuildingStatus status
	) {
		Building building = new Building();
		building.setOrganization(organization);
		building.setCode(code);
		building.setName(name);
		building.setAddress(address);
		building.setCity(city);
		building.setNoticePeriodMonths(3);
		building.setStatus(status);
		return buildingRepository.saveAndFlush(building);
	}
}
