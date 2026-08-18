package pl.m2manager.staircase.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.m2manager.building.entity.Building;
import pl.m2manager.building.entity.BuildingStatus;
import pl.m2manager.building.repository.BuildingRepository;
import pl.m2manager.organization.entity.Organization;
import pl.m2manager.organization.repository.OrganizationRepository;
import pl.m2manager.staircase.entity.Staircase;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class StaircaseRepositoryTest {

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
	private StaircaseRepository staircaseRepository;

	@Autowired
	private BuildingRepository buildingRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void flywayV10_createsStaircasesTable() {
		Integer count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'staircases'",
				Integer.class
		);
		assertThat(count).isEqualTo(1);
	}

	@Test
	void create_persistsStaircase() {
		Organization organization = saveOrganization("Org Staircase Persist");
		Building building = saveBuilding(organization, "BLD1");
		Staircase staircase = saveStaircase(organization.getId(), building.getId(), "KL001", "1");

		assertThat(staircase.getId()).isNotNull();
		assertThat(staircaseRepository.findByIdAndOrganizationId(staircase.getId(), organization.getId())).isPresent();
	}

	@Test
	void findByBuilding_returnsStaircases() {
		Organization organization = saveOrganization("Org Staircase List");
		Building building = saveBuilding(organization, "BLD2");
		saveStaircase(organization.getId(), building.getId(), "KL002", "1");
		saveStaircase(organization.getId(), building.getId(), "KL003", "2");

		assertThat(staircaseRepository.findAllByOrganizationIdAndBuildingIdOrderByDesignationAsc(
				organization.getId(),
				building.getId()
		)).hasSize(2);
	}

	@Test
	void duplicateCode_rejectedWithinOrganization() {
		Organization organization = saveOrganization("Org Staircase Code Dup");
		Building building = saveBuilding(organization, "BLD3");
		saveStaircase(organization.getId(), building.getId(), "KL004", "1");

		Staircase duplicate = newStaircase(organization.getId(), building.getId(), "KL004", "2");
		assertThatThrownBy(() -> staircaseRepository.saveAndFlush(duplicate))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void duplicateDesignation_rejectedWithinBuilding() {
		Organization organization = saveOrganization("Org Staircase Desig Dup");
		Building building = saveBuilding(organization, "BLD4");
		saveStaircase(organization.getId(), building.getId(), "KL005", "1");

		Staircase duplicate = newStaircase(organization.getId(), building.getId(), "KL006", "1");
		assertThatThrownBy(() -> staircaseRepository.saveAndFlush(duplicate))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void sameDesignation_allowedInDifferentBuildings() {
		Organization organization = saveOrganization("Org Staircase Desig Cross");
		Building buildingA = saveBuilding(organization, "BLDA");
		Building buildingB = saveBuilding(organization, "BLDB");
		saveStaircase(organization.getId(), buildingA.getId(), "KL007", "1");
		Staircase otherBuilding = saveStaircase(organization.getId(), buildingB.getId(), "KL008", "1");

		assertThat(otherBuilding.getId()).isNotNull();
	}

	@Test
	void crossTenantFk_rejectsForeignBuilding() {
		Organization orgA = saveOrganization("Org Staircase FK A");
		Organization orgB = saveOrganization("Org Staircase FK B");
		Building buildingB = saveBuilding(orgB, "BLDB2");

		Staircase invalid = newStaircase(orgA.getId(), buildingB.getId(), "KL009", "1");
		assertThatThrownBy(() -> staircaseRepository.saveAndFlush(invalid))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void booleanFields_persist() {
		Organization organization = saveOrganization("Org Staircase Booleans");
		Building building = saveBuilding(organization, "BLD5");
		Staircase staircase = newStaircase(organization.getId(), building.getId(), "KL010", "A");
		staircase.setKeyRequired(true);
		staircase.setElevator(true);
		staircaseRepository.saveAndFlush(staircase);

		Staircase reloaded = staircaseRepository.findById(staircase.getId()).orElseThrow();
		assertThat(reloaded.isKeyRequired()).isTrue();
		assertThat(reloaded.isElevator()).isTrue();
	}

	@Test
	void floors_persist() {
		Organization organization = saveOrganization("Org Staircase Floors");
		Building building = saveBuilding(organization, "BLD6");
		Staircase staircase = newStaircase(organization.getId(), building.getId(), "KL011", "1");
		staircase.setFloors(6);
		staircaseRepository.saveAndFlush(staircase);

		Staircase reloaded = staircaseRepository.findById(staircase.getId()).orElseThrow();
		assertThat(reloaded.getFloors()).isEqualTo(6);
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
		Staircase staircase = newStaircase(organizationId, buildingId, code, designation);
		return staircaseRepository.saveAndFlush(staircase);
	}

	private Staircase newStaircase(UUID organizationId, UUID buildingId, String code, String designation) {
		Staircase staircase = new Staircase();
		staircase.setOrganizationId(organizationId);
		staircase.setBuildingId(buildingId);
		staircase.setCode(code);
		staircase.setDesignation(designation);
		staircase.setFloors(4);
		return staircase;
	}
}
