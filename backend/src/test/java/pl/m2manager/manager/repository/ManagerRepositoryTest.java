package pl.m2manager.manager.repository;

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
import pl.m2manager.organization.entity.Organization;
import pl.m2manager.organization.repository.OrganizationRepository;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class ManagerRepositoryTest {

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
	private OrganizationRepository organizationRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void flywayV16_createsManagersTable() {
		Integer count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'managers'",
				Integer.class
		);
		assertThat(count).isEqualTo(1);
	}

	@Test
	void create_persistsManager() {
		Organization organization = saveOrganization("Org Manager Persist");
		Manager manager = saveManager(organization.getId(), "ZA0001", "Manager One");

		assertThat(manager.getId()).isNotNull();
		assertThat(managerRepository.findByIdAndOrganizationId(manager.getId(), organization.getId())).isPresent();
	}

	@Test
	void findAllByFilters_searchByName() {
		Organization organization = saveOrganization("Org Manager Search");
		saveManager(organization.getId(), "ZA0002", "Kozera Nieruchomości");
		saveManager(organization.getId(), "ZA0003", "Other Manager");

		assertThat(managerRepository.findAllByOrganizationIdAndFilters(organization.getId(), "kozera", null))
				.extracting(Manager::getName)
				.containsExactly("Kozera Nieruchomości");
	}

	@Test
	void findAllByFilters_activeFilter() {
		Organization organization = saveOrganization("Org Manager Active");
		Manager active = saveManager(organization.getId(), "ZA0004", "Active Manager");
		Manager inactive = newManager(organization.getId(), "ZA0005", "Inactive Manager");
		inactive.setActive(false);
		managerRepository.saveAndFlush(inactive);

		assertThat(managerRepository.findAllByOrganizationIdAndFilters(organization.getId(), null, true))
				.extracting(Manager::getId)
				.containsExactly(active.getId());
	}

	@Test
	void duplicateCode_rejectedWithinOrganization() {
		Organization organization = saveOrganization("Org Manager Code Dup");
		saveManager(organization.getId(), "ZA0006", "Manager A");

		Manager duplicate = newManager(organization.getId(), "ZA0006", "Manager B");
		assertThatThrownBy(() -> managerRepository.saveAndFlush(duplicate))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void duplicateName_rejectedWithinOrganization() {
		Organization organization = saveOrganization("Org Manager Name Dup");
		saveManager(organization.getId(), "ZA0007", "Same Name");

		Manager duplicate = newManager(organization.getId(), "ZA0008", "Same Name");
		assertThatThrownBy(() -> managerRepository.saveAndFlush(duplicate))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void sameCode_allowedInDifferentOrganizations() {
		Organization orgA = saveOrganization("Org Manager Code Cross A");
		Organization orgB = saveOrganization("Org Manager Code Cross B");
		saveManager(orgA.getId(), "ZA0009", "Manager A");
		Manager otherOrg = saveManager(orgB.getId(), "ZA0009", "Manager B");

		assertThat(otherOrg.getId()).isNotNull();
	}

	private Organization saveOrganization(String name) {
		Organization organization = new Organization();
		organization.setName(name);
		organization.setSlug(name.toLowerCase().replace(' ', '-') + "-" + UUID.randomUUID().toString().substring(0, 8));
		organization.setTimezone("Europe/Warsaw");
		return organizationRepository.saveAndFlush(organization);
	}

	private Manager saveManager(UUID organizationId, String code, String name) {
		return managerRepository.saveAndFlush(newManager(organizationId, code, name));
	}

	private Manager newManager(UUID organizationId, String code, String name) {
		Manager manager = new Manager();
		manager.setOrganizationId(organizationId);
		manager.setCode(code);
		manager.setName(name);
		return manager;
	}
}
