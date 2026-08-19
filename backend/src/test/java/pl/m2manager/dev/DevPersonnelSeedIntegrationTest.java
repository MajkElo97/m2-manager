package pl.m2manager.dev;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.m2manager.building.entity.Building;
import pl.m2manager.building.repository.BuildingRepository;
import pl.m2manager.contact.repository.ContactRepository;
import pl.m2manager.employee.entity.EmployeeRole;
import pl.m2manager.employee.entity.EmploymentType;
import pl.m2manager.employee.entity.RemunerationUnit;
import pl.m2manager.employee.repository.EmployeeRepository;
import pl.m2manager.manager.repository.ManagerRepository;
import pl.m2manager.supervisor.repository.SupervisorRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
@Transactional(readOnly = true)
class DevPersonnelSeedIntegrationTest {

	static final UUID DEV_ORGANIZATION_ID = UUID.fromString("a0000000-0000-4000-8000-000000000001");

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
	private ManagerRepository managerRepository;

	@Autowired
	private SupervisorRepository supervisorRepository;

	@Autowired
	private EmployeeRepository employeeRepository;

	@Autowired
	private ContactRepository contactRepository;

	@Autowired
	private BuildingRepository buildingRepository;

	@Test
	void devSeed_managersSupervisorsEmployeesContacts() {
		assertThat(managerRepository.findAllByOrganizationIdAndFilters(DEV_ORGANIZATION_ID, null, null)).hasSize(2);
		assertThat(supervisorRepository.findAllByOrganizationIdAndFilters(DEV_ORGANIZATION_ID, null, null, null)).hasSize(2);
		assertThat(employeeRepository.findAllByOrganizationIdAndFilters(DEV_ORGANIZATION_ID, null, null, null, null, null)).hasSize(6);
		assertThat(contactRepository.findAllByOrganizationIdAndBuildingId(DEV_ORGANIZATION_ID, null)).hasSize(4);
	}

	@Test
	void devSeed_employeeRemunerationAndRoles() {
		var e0001 = employeeRepository.findByOrganizationIdAndCode(DEV_ORGANIZATION_ID, "E0001").orElseThrow();
		assertThat(e0001.getRemunerationAmount()).isEqualByComparingTo(new BigDecimal("20.00"));
		assertThat(e0001.getRemunerationUnit()).isEqualTo(RemunerationUnit.HOURLY);
		assertThat(e0001.getRemunerationNet()).isTrue();
		assertThat(e0001.getEmploymentStartDate()).isEqualTo(LocalDate.of(2025, 5, 1));

		var e0003 = employeeRepository.findByOrganizationIdAndCode(DEV_ORGANIZATION_ID, "E0003").orElseThrow();
		assertThat(e0003.getRole()).isEqualTo(EmployeeRole.ADMIN);
		assertThat(e0003.getEmploymentType()).isNull();
		assertThat(e0003.getRemunerationAmount()).isNull();

		var e0004 = employeeRepository.findByOrganizationIdAndCode(DEV_ORGANIZATION_ID, "E0004").orElseThrow();
		assertThat(e0004.getFirstName()).isEqualTo("M2 Group");
		assertThat(e0004.getLastName()).isNull();
	}

	@Test
	void devSeed_buildingPersonnelForeignKeys() {
		Building pusta64 = requireBuilding("PUSTA64");
		assertThat(pusta64.getManager()).isNotNull();
		assertThat(pusta64.getManager().getCode()).isEqualTo("ZA0001");
		assertThat(pusta64.getSupervisor()).isNotNull();
		assertThat(pusta64.getSupervisor().getCode()).isEqualTo("OP0001");
		assertThat(pusta64.getEmployee()).isNotNull();
		assertThat(pusta64.getEmployee().getCode()).isEqualTo("E0001");

		Building kasprzaka6 = requireBuilding("KASPRZAKA6");
		assertThat(kasprzaka6.getManager().getCode()).isEqualTo("ZA0002");
		assertThat(kasprzaka6.getSupervisor().getCode()).isEqualTo("OP0002");
		assertThat(kasprzaka6.getEmployee().getCode()).isEqualTo("E0002");

		Building pusta62 = requireBuilding("PUSTA62");
		assertThat(pusta62.getEmployee().getCode()).isEqualTo("E0003");
	}

	@Test
	void devSeed_supervisorManagerRelationship() {
		var op0001 = supervisorRepository.findByOrganizationIdAndCode(DEV_ORGANIZATION_ID, "OP0001").orElseThrow();
		var za0001 = managerRepository.findByOrganizationIdAndCode(DEV_ORGANIZATION_ID, "ZA0001").orElseThrow();
		assertThat(op0001.getManagerId()).isEqualTo(za0001.getId());
	}

	@Test
	void devSeed_employeeEmploymentType() {
		var e0002 = employeeRepository.findByOrganizationIdAndCode(DEV_ORGANIZATION_ID, "E0002").orElseThrow();
		assertThat(e0002.getEmploymentType()).isEqualTo(EmploymentType.ZLECENIE);
		assertThat(e0002.getEmploymentStartDate()).isEqualTo(LocalDate.of(2026, 4, 10));
	}

	private Building requireBuilding(String code) {
		return buildingRepository.findByOrganizationIdAndCode(DEV_ORGANIZATION_ID, code).orElseThrow();
	}
}
