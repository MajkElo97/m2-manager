package pl.m2manager.employee.repository;

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
import pl.m2manager.employee.entity.Employee;
import pl.m2manager.employee.entity.EmployeeRole;
import pl.m2manager.employee.entity.EmploymentType;
import pl.m2manager.employee.entity.RemunerationUnit;
import pl.m2manager.organization.entity.Organization;
import pl.m2manager.organization.repository.OrganizationRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class EmployeeRepositoryTest {

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
	private EmployeeRepository employeeRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void flywayV18_createsEmployeesTable() {
		Integer count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'employees'",
				Integer.class
		);
		assertThat(count).isEqualTo(1);
	}

	@Test
	void create_persistsEmployee() {
		Organization organization = saveOrganization("Org Employee Persist");
		Employee employee = saveEmployee(organization, "E0001", "Jan", "Kowalski");

		assertThat(employee.getId()).isNotNull();
		assertThat(employeeRepository.findByIdAndOrganizationId(employee.getId(), organization.getId())).isPresent();
	}

	@Test
	void findByOrganization_returnsTenantEmployees() {
		Organization organization = saveOrganization("Org Employee List");
		saveEmployee(organization, "E0001", "Jan", "Kowalski");
		saveEmployee(organization, "E0002", "Anna", "Nowak", false);

		assertThat(employeeRepository.findAllByOrganizationIdAndFilters(organization.getId(), null, null, null, null, null))
				.hasSize(2);
		assertThat(employeeRepository.findAllByOrganizationIdAndFilters(organization.getId(), null, null, null, null, true))
				.hasSize(1);
	}

	@Test
	void filters_byPositionRoleAndEmploymentType() {
		Organization organization = saveOrganization("Org Employee Filters");
		saveEmployee(organization, "E0001", "Jan", "Kowalski", EmployeeRole.PRACOWNIK, EmploymentType.ZLECENIE, "Sprzątanie");
		saveEmployee(organization, "E0002", "Anna", "Nowak", EmployeeRole.ADMIN, null, "Szef");

		assertThat(employeeRepository.findAllByOrganizationIdAndFilters(
				organization.getId(), null, "Sprzątanie", null, null, null
		)).hasSize(1);
		assertThat(employeeRepository.findAllByOrganizationIdAndFilters(
				organization.getId(), null, null, EmployeeRole.ADMIN, null, null
		)).hasSize(1);
		assertThat(employeeRepository.findAllByOrganizationIdAndFilters(
				organization.getId(), null, null, null, EmploymentType.ZLECENIE, null
		)).hasSize(1);
	}

	@Test
	void duplicateCode_rejectedWithinOrganization() {
		Organization organization = saveOrganization("Org Employee Duplicate");
		saveEmployee(organization, "DUP", "Jan", "Kowalski");

		Employee duplicate = new Employee();
		duplicate.setOrganization(organization);
		duplicate.setCode("DUP");
		duplicate.setFirstName("Duplicate");
		duplicate.setRole(EmployeeRole.PRACOWNIK);
		duplicate.setActive(true);

		assertThatThrownBy(() -> employeeRepository.saveAndFlush(duplicate))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void sameCode_allowedInDifferentOrganizations() {
		Organization orgA = saveOrganization("Org Employee A");
		Organization orgB = saveOrganization("Org Employee B");
		saveEmployee(orgA, "SHARED", "Jan", "Kowalski");
		Employee employeeB = saveEmployee(orgB, "SHARED", "Anna", "Nowak");

		assertThat(employeeB.getId()).isNotNull();
	}

	@Test
	void crossTenantFind_cannotAccessAnotherOrganization() {
		Organization orgA = saveOrganization("Org Employee Cross A");
		Organization orgB = saveOrganization("Org Employee Cross B");
		Employee employeeB = saveEmployee(orgB, "CROSS", "Jan", "Kowalski");

		assertThat(employeeRepository.findByIdAndOrganizationId(employeeB.getId(), orgA.getId())).isEmpty();
	}

	@Test
	void search_matchesNamePhoneAndEmail() {
		Organization organization = saveOrganization("Org Employee Search");
		Employee employee = saveEmployee(organization, "E0001", "Jadwiga", "Śliwa");
		employee.setPhone("509481378");
		employee.setEmail("jadwiga@gmail.com");
		employee.setGoogleEmail("jadwiga@gmail.com");
		employeeRepository.saveAndFlush(employee);

		assertThat(employeeRepository.findAllByOrganizationIdAndFilters(organization.getId(), "jadwiga", null, null, null, null))
				.hasSize(1);
		assertThat(employeeRepository.findAllByOrganizationIdAndFilters(organization.getId(), "509481", null, null, null, null))
				.hasSize(1);
		assertThat(employeeRepository.findAllByOrganizationIdAndFilters(organization.getId(), "śliwa", null, null, null, null))
				.hasSize(1);
	}

	@Test
	void remuneration_persistsCorrectly() {
		Organization organization = saveOrganization("Org Employee Remuneration");
		Employee employee = new Employee();
		employee.setOrganization(organization);
		employee.setCode("REM");
		employee.setFirstName("Jan");
		employee.setLastName("Kowalski");
		employee.setRole(EmployeeRole.PRACOWNIK);
		employee.setEmploymentType(EmploymentType.ZLECENIE);
		employee.setEmploymentStartDate(LocalDate.of(2025, 5, 1));
		employee.setRemunerationAmount(new BigDecimal("20.00"));
		employee.setRemunerationUnit(RemunerationUnit.HOURLY);
		employee.setRemunerationNet(true);
		employee.setCalendarColor("#F97316");
		employee.setActive(true);
		employeeRepository.saveAndFlush(employee);

		Employee reloaded = employeeRepository.findById(employee.getId()).orElseThrow();
		assertThat(reloaded.getRemunerationAmount()).isEqualByComparingTo("20.00");
		assertThat(reloaded.getRemunerationUnit()).isEqualTo(RemunerationUnit.HOURLY);
		assertThat(reloaded.getRemunerationNet()).isTrue();
		assertThat(reloaded.getCalendarColor()).isEqualTo("#F97316");
	}

	private Organization saveOrganization(String name) {
		Organization organization = new Organization();
		organization.setName(name);
		organization.setSlug(name.toLowerCase().replace(' ', '-') + "-" + UUID.randomUUID().toString().substring(0, 8));
		organization.setTimezone("Europe/Warsaw");
		return organizationRepository.saveAndFlush(organization);
	}

	private Employee saveEmployee(Organization organization, String code, String firstName, String lastName) {
		return saveEmployee(organization, code, firstName, lastName, true);
	}

	private Employee saveEmployee(
			Organization organization,
			String code,
			String firstName,
			String lastName,
			boolean active
	) {
		return saveEmployee(organization, code, firstName, lastName, EmployeeRole.PRACOWNIK, EmploymentType.ZLECENIE, "Sprzątanie", active);
	}

	private Employee saveEmployee(
			Organization organization,
			String code,
			String firstName,
			String lastName,
			EmployeeRole role,
			EmploymentType employmentType,
			String position
	) {
		return saveEmployee(organization, code, firstName, lastName, role, employmentType, position, true);
	}

	private Employee saveEmployee(
			Organization organization,
			String code,
			String firstName,
			String lastName,
			EmployeeRole role,
			EmploymentType employmentType,
			String position,
			boolean active
	) {
		Employee employee = new Employee();
		employee.setOrganization(organization);
		employee.setCode(code);
		employee.setFirstName(firstName);
		employee.setLastName(lastName);
		employee.setRole(role);
		employee.setEmploymentType(employmentType);
		employee.setPosition(position);
		employee.setActive(active);
		return employeeRepository.saveAndFlush(employee);
	}
}
