package pl.m2manager.employee.service;

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
import pl.m2manager.employee.dto.request.CreateEmployeeRequest;
import pl.m2manager.employee.dto.request.UpdateEmployeeRequest;
import pl.m2manager.employee.entity.Employee;
import pl.m2manager.employee.entity.EmployeeRole;
import pl.m2manager.employee.entity.EmploymentType;
import pl.m2manager.employee.entity.RemunerationUnit;
import pl.m2manager.employee.repository.EmployeeRepository;
import pl.m2manager.organization.entity.Organization;
import pl.m2manager.organization.repository.OrganizationRepository;
import pl.m2manager.tenant.TenantContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class EmployeeServiceTest {

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
	private EmployeeService employeeService;

	@Autowired
	private EmployeeRepository employeeRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	private Organization organizationA;
	private Organization organizationB;

	@BeforeEach
	void setUp() {
		organizationA = saveOrganization("Org Employee Service A");
		organizationB = saveOrganization("Org Employee Service B");
		when(tenantContext.getCurrentOrganizationId()).thenReturn(organizationA.getId());
	}

	@Test
	void create_usesCurrentTenantContext() {
		var created = employeeService.create(sampleCreateRequest("NEWCODE"));

		Employee persisted = employeeRepository.findByIdAndOrganizationId(created.id(), organizationA.getId()).orElseThrow();
		assertThat(persisted.getOrganization().getId()).isEqualTo(organizationA.getId());
	}

	@Test
	void create_persistsRemuneration() {
		var request = new CreateEmployeeRequest(
				"REM1",
				"Jan",
				"Kowalski",
				"509481378",
				"jan@example.com",
				"jan@example.com",
				"Sprzątanie",
				EmployeeRole.PRACOWNIK,
				EmploymentType.ZLECENIE,
				LocalDate.of(2025, 5, 1),
				new BigDecimal("20.00"),
				RemunerationUnit.HOURLY,
				true,
				"#F97316",
				null
		);

		var created = employeeService.create(request);

		assertThat(created.remunerationAmount()).isEqualByComparingTo("20.00");
		assertThat(created.remunerationUnit()).isEqualTo(RemunerationUnit.HOURLY);
		assertThat(created.remunerationNet()).isTrue();
		assertThat(created.calendarColor()).isEqualTo("#F97316");
	}

	@Test
	void create_duplicateCode_throwsBusinessConflict() {
		saveEmployee(organizationA, "DUPLICATE");

		assertThatThrownBy(() -> employeeService.create(sampleCreateRequest("DUPLICATE")))
				.isInstanceOf(BusinessConflictException.class);
	}

	@Test
	void getById_tenantIsolation() {
		Employee employeeA = saveEmployee(organizationA, "A1");
		Employee employeeB = saveEmployee(organizationB, "B1");

		assertThat(employeeService.getById(employeeA.getId()).code()).isEqualTo("A1");

		assertThatThrownBy(() -> employeeService.getById(employeeB.getId()))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void update_tenantIsolation() {
		Employee employeeB = saveEmployee(organizationB, "UPDATEB");

		assertThatThrownBy(() -> employeeService.update(
				employeeB.getId(),
				sampleUpdateRequest("UPDATEB", true)
		)).isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void deactivate_tenantIsolation() {
		Employee employeeB = saveEmployee(organizationB, "DELETEB");

		assertThatThrownBy(() -> employeeService.deactivate(employeeB.getId()))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void deactivate_alreadyInactive_isIdempotent() {
		Employee employee = saveEmployee(organizationA, "INACTIVE", false);

		employeeService.deactivate(employee.getId());

		Employee reloaded = employeeRepository.findById(employee.getId()).orElseThrow();
		assertThat(reloaded.isActive()).isFalse();
	}

	private CreateEmployeeRequest sampleCreateRequest(String code) {
		return new CreateEmployeeRequest(
				code,
				"Jan",
				"Kowalski",
				null,
				null,
				null,
				"Sprzątanie",
				EmployeeRole.PRACOWNIK,
				EmploymentType.ZLECENIE,
				LocalDate.of(2025, 5, 1),
				null,
				null,
				null,
				"#F97316",
				null
		);
	}

	private UpdateEmployeeRequest sampleUpdateRequest(String code, boolean active) {
		return new UpdateEmployeeRequest(
				code,
				"Updated",
				"Name",
				null,
				null,
				null,
				"Sprzątanie",
				EmployeeRole.PRACOWNIK,
				EmploymentType.ZLECENIE,
				LocalDate.of(2025, 5, 1),
				null,
				null,
				null,
				"#F97316",
				null,
				active
		);
	}

	private Organization saveOrganization(String name) {
		Organization organization = new Organization();
		organization.setName(name);
		organization.setSlug(name.toLowerCase().replace(' ', '-') + "-" + UUID.randomUUID().toString().substring(0, 8));
		organization.setTimezone("Europe/Warsaw");
		return organizationRepository.saveAndFlush(organization);
	}

	private Employee saveEmployee(Organization organization, String code) {
		return saveEmployee(organization, code, true);
	}

	private Employee saveEmployee(Organization organization, String code, boolean active) {
		Employee employee = new Employee();
		employee.setOrganization(organization);
		employee.setCode(code);
		employee.setFirstName(code);
		employee.setLastName("Test");
		employee.setRole(EmployeeRole.PRACOWNIK);
		employee.setActive(active);
		return employeeRepository.saveAndFlush(employee);
	}
}
