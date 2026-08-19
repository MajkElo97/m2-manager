package pl.m2manager.supervisor.repository;

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
import pl.m2manager.manager.entity.Manager;
import pl.m2manager.manager.repository.ManagerRepository;
import pl.m2manager.organization.entity.Organization;
import pl.m2manager.organization.repository.OrganizationRepository;
import pl.m2manager.supervisor.entity.Supervisor;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class SupervisorRepositoryTest {

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
	private SupervisorRepository supervisorRepository;

	@Autowired
	private ManagerRepository managerRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void flywayV17_createsSupervisorsTable() {
		Integer count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'supervisors'",
				Integer.class
		);
		assertThat(count).isEqualTo(1);
	}

	@Test
	void create_persistsSupervisor() {
		Organization organization = saveOrganization("Org Supervisor Persist");
		Manager manager = saveManager(organization, "MGR1");
		Supervisor supervisor = saveSupervisor(organization.getId(), manager.getId(), "OP0001");

		assertThat(supervisor.getId()).isNotNull();
		assertThat(supervisorRepository.findByIdAndOrganizationId(supervisor.getId(), organization.getId())).isPresent();
	}

	@Test
	void findAllByFilters_filtersByManagerAndActive() {
		Organization organization = saveOrganization("Org Supervisor Filters");
		Manager managerA = saveManager(organization, "MGR-A");
		Manager managerB = saveManager(organization, "MGR-B");
		saveSupervisor(organization.getId(), managerA.getId(), "OP0002");
		saveSupervisor(organization.getId(), managerB.getId(), "OP0003");
		Supervisor inactive = saveSupervisor(organization.getId(), managerA.getId(), "OP0004");
		inactive.setActive(false);
		supervisorRepository.saveAndFlush(inactive);

		assertThat(supervisorRepository.findAllByOrganizationIdAndFilters(
				organization.getId(), managerA.getId(), true, null
		)).hasSize(1);

		assertThat(supervisorRepository.findAllByOrganizationIdAndFilters(
				organization.getId(), null, false, null
		)).hasSize(1);
	}

	@Test
	void findAllByFilters_searchMatchesName() {
		Organization organization = saveOrganization("Org Supervisor Search");
		Manager manager = saveManager(organization, "MGR-S");
		saveSupervisor(organization.getId(), manager.getId(), "OP0005", "Anna", "Nowak");

		assertThat(supervisorRepository.findAllByOrganizationIdAndFilters(
				organization.getId(), null, null, "nowak"
		)).hasSize(1);
	}

	@Test
	void duplicateCode_rejectedWithinOrganization() {
		Organization organization = saveOrganization("Org Supervisor Code Dup");
		Manager manager = saveManager(organization, "MGR-D");
		saveSupervisor(organization.getId(), manager.getId(), "OP0006");

		Supervisor duplicate = newSupervisor(organization.getId(), manager.getId(), "OP0006");
		assertThatThrownBy(() -> supervisorRepository.saveAndFlush(duplicate))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	private Organization saveOrganization(String name) {
		Organization organization = new Organization();
		organization.setName(name);
		organization.setSlug(name.toLowerCase().replace(' ', '-') + "-" + UUID.randomUUID().toString().substring(0, 8));
		organization.setTimezone("Europe/Warsaw");
		return organizationRepository.saveAndFlush(organization);
	}

	private Manager saveManager(Organization organization, String code) {
		Manager manager = new Manager();
		manager.setOrganizationId(organization.getId());
		manager.setCode(code);
		manager.setName(code);
		return managerRepository.saveAndFlush(manager);
	}

	private Supervisor saveSupervisor(UUID organizationId, UUID managerId, String code) {
		return saveSupervisor(organizationId, managerId, code, "Jan", "Kowalski");
	}

	private Supervisor saveSupervisor(UUID organizationId, UUID managerId, String code, String firstName, String lastName) {
		Supervisor supervisor = newSupervisor(organizationId, managerId, code);
		supervisor.setFirstName(firstName);
		supervisor.setLastName(lastName);
		return supervisorRepository.saveAndFlush(supervisor);
	}

	private Supervisor newSupervisor(UUID organizationId, UUID managerId, String code) {
		Supervisor supervisor = new Supervisor();
		supervisor.setOrganizationId(organizationId);
		supervisor.setManagerId(managerId);
		supervisor.setCode(code);
		supervisor.setFirstName("Jan");
		supervisor.setLastName("Kowalski");
		return supervisor;
	}
}
