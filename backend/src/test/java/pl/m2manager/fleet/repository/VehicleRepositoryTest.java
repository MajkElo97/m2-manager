package pl.m2manager.fleet.repository;

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
import pl.m2manager.fleet.entity.Vehicle;
import pl.m2manager.fleet.entity.VehicleStatus;
import pl.m2manager.fleet.entity.VehicleType;
import pl.m2manager.organization.entity.Organization;
import pl.m2manager.organization.repository.OrganizationRepository;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class VehicleRepositoryTest {

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
	private VehicleRepository vehicleRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void flywayV28_createsVehiclesTable() {
		Integer count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'vehicles'",
				Integer.class
		);
		assertThat(count).isEqualTo(1);
	}

	@Test
	void create_persistsVehicle() {
		Organization organization = saveOrganization("Org Fleet Persist");
		Vehicle vehicle = saveVehicle(organization.getId(), "FL0101", "SK 12121");

		assertThat(vehicle.getId()).isNotNull();
		assertThat(vehicleRepository.findByIdAndOrganizationId(vehicle.getId(), organization.getId())).isPresent();
	}

	@Test
	void findAllByFilters_searchByRegistration() {
		Organization organization = saveOrganization("Org Fleet Search");
		saveVehicle(organization.getId(), "FL0102", "SK UNIQUE1");
		saveVehicle(organization.getId(), "FL0103", "SK OTHER1");

		assertThat(vehicleRepository.findAllByOrganizationIdAndFilters(organization.getId(), "unique1", null, null, null))
				.extracting(Vehicle::getCode)
				.containsExactly("FL0102");
	}

	@Test
	void findAllByFilters_statusFilter() {
		Organization organization = saveOrganization("Org Fleet Status");
		Vehicle active = saveVehicle(organization.getId(), "FL0104", "SK 14141");
		Vehicle inactive = newVehicle(organization.getId(), "FL0105", "SK 15151");
		inactive.setStatus(VehicleStatus.INACTIVE);
		vehicleRepository.saveAndFlush(inactive);

		assertThat(vehicleRepository.findAllByOrganizationIdAndFilters(organization.getId(), null, VehicleStatus.ACTIVE, null, null))
				.extracting(Vehicle::getId)
				.containsExactly(active.getId());
	}

	@Test
	void duplicateCode_rejectedWithinOrganization() {
		Organization organization = saveOrganization("Org Fleet Code Dup");
		saveVehicle(organization.getId(), "FL0106", "SK 16161");

		Vehicle duplicate = newVehicle(organization.getId(), "FL0106", "SK 17171");
		assertThatThrownBy(() -> vehicleRepository.saveAndFlush(duplicate))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	private Organization saveOrganization(String name) {
		Organization organization = new Organization();
		organization.setName(name);
		organization.setSlug(name.toLowerCase().replace(' ', '-') + "-" + UUID.randomUUID().toString().substring(0, 8));
		organization.setTimezone("Europe/Warsaw");
		return organizationRepository.saveAndFlush(organization);
	}

	private Vehicle saveVehicle(UUID organizationId, String code, String registration) {
		return vehicleRepository.saveAndFlush(newVehicle(organizationId, code, registration));
	}

	private Vehicle newVehicle(UUID organizationId, String code, String registration) {
		Vehicle vehicle = new Vehicle();
		vehicle.setOrganizationId(organizationId);
		vehicle.setCode(code);
		vehicle.setRegistrationNumber(registration);
		vehicle.setMake("Make");
		vehicle.setModel("Model");
		vehicle.setVehicleType(VehicleType.VAN);
		vehicle.setStatus(VehicleStatus.ACTIVE);
		return vehicle;
	}
}
