package pl.m2manager.fleet.service;

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
import pl.m2manager.common.exception.BusinessConflictException;
import pl.m2manager.common.exception.ResourceNotFoundException;
import pl.m2manager.employee.entity.Employee;
import pl.m2manager.employee.entity.EmployeeRole;
import pl.m2manager.employee.repository.EmployeeRepository;
import pl.m2manager.fleet.dto.request.CreateVehicleRequest;
import pl.m2manager.fleet.dto.request.UpdateVehicleRequest;
import pl.m2manager.fleet.entity.Vehicle;
import pl.m2manager.fleet.entity.VehicleStatus;
import pl.m2manager.fleet.entity.VehicleType;
import pl.m2manager.fleet.repository.VehicleRepository;
import pl.m2manager.organization.entity.Organization;
import pl.m2manager.organization.repository.OrganizationRepository;
import pl.m2manager.tenant.TenantContext;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class VehicleServiceTest {

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
	private VehicleService vehicleService;

	@Autowired
	private VehicleRepository vehicleRepository;

	@Autowired
	private EmployeeRepository employeeRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	private Organization organizationA;
	private Organization organizationB;

	@BeforeEach
	void setUp() {
		organizationA = saveOrganization("Org Fleet Service A");
		organizationB = saveOrganization("Org Fleet Service B");
		when(tenantContext.getCurrentOrganizationId()).thenReturn(organizationA.getId());
	}

	@Test
	void create_usesCurrentTenantContext() {
		var created = vehicleService.create(sampleCreateRequest("FL0001", "SK 11111"));

		Vehicle persisted = vehicleRepository.findByIdAndOrganizationId(created.id(), organizationA.getId()).orElseThrow();
		assertThat(persisted.getOrganizationId()).isEqualTo(organizationA.getId());
		assertThat(persisted.getStatus()).isEqualTo(VehicleStatus.ACTIVE);
	}

	@Test
	void create_duplicateCode_throwsBusinessConflict() {
		saveVehicle(organizationA.getId(), "FL0002", "SK 22222");

		assertThatThrownBy(() -> vehicleService.create(sampleCreateRequest("FL0002", "SK 33333")))
				.isInstanceOf(BusinessConflictException.class);
	}

	@Test
	void getById_tenantIsolation() {
		Vehicle vehicleA = saveVehicle(organizationA.getId(), "FL0003", "SK 44444");
		saveVehicle(organizationB.getId(), "FL0004", "SK 55555");

		assertThat(vehicleService.getById(vehicleA.getId()).code()).isEqualTo("FL0003");

		assertThatThrownBy(() -> vehicleService.getById(saveVehicle(organizationB.getId(), "FL0005", "SK 66666").getId()))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void getAll_returnsOnlyCurrentTenantVehicles() {
		saveVehicle(organizationA.getId(), "FL0006", "SK 77777");
		saveVehicle(organizationB.getId(), "FL0007", "SK 88888");

		assertThat(vehicleService.getAll(null, null, null, null))
				.extracting(response -> response.code())
				.containsExactly("FL0006");
	}

	@Test
	void deactivate_setsStatusInactive() {
		Vehicle vehicle = saveVehicle(organizationA.getId(), "FL0008", "SK 99999");

		vehicleService.deactivate(vehicle.getId());

		Vehicle reloaded = vehicleRepository.findById(vehicle.getId()).orElseThrow();
		assertThat(reloaded.getStatus()).isEqualTo(VehicleStatus.INACTIVE);
	}

	@Test
	void create_withEmployeeFromOtherTenant_throwsBusinessConflict() {
		Employee employeeB = saveEmployee(organizationB.getId(), "EB001");

		assertThatThrownBy(() -> vehicleService.create(new CreateVehicleRequest(
				"FL0009",
				"SK 10101",
				"Make",
				"Model",
				null,
				null,
				VehicleType.VAN,
				employeeB.getId(),
				VehicleStatus.ACTIVE,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null
		))).isInstanceOf(BusinessConflictException.class);
	}

	private CreateVehicleRequest sampleCreateRequest(String code, String registration) {
		return new CreateVehicleRequest(
				code,
				registration,
				"Make",
				"Model",
				2020,
				null,
				VehicleType.VAN,
				null,
				VehicleStatus.ACTIVE,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null
		);
	}

	private UpdateVehicleRequest sampleUpdateRequest(String code, String registration) {
		return new UpdateVehicleRequest(
				code,
				registration,
				"Make",
				"Model",
				2020,
				null,
				VehicleType.VAN,
				null,
				VehicleStatus.ACTIVE,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
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

	private Vehicle saveVehicle(UUID organizationId, String code, String registration) {
		Vehicle vehicle = new Vehicle();
		vehicle.setOrganizationId(organizationId);
		vehicle.setCode(code);
		vehicle.setRegistrationNumber(registration);
		vehicle.setMake("Make");
		vehicle.setModel("Model");
		vehicle.setVehicleType(VehicleType.VAN);
		vehicle.setStatus(VehicleStatus.ACTIVE);
		return vehicleRepository.saveAndFlush(vehicle);
	}

	private Employee saveEmployee(UUID organizationId, String code) {
		Organization organization = organizationRepository.findById(organizationId).orElseThrow();
		Employee employee = new Employee();
		employee.setOrganization(organization);
		employee.setCode(code);
		employee.setFirstName("Jan");
		employee.setLastName("Kowalski");
		employee.setRole(EmployeeRole.PRACOWNIK);
		return employeeRepository.saveAndFlush(employee);
	}
}
