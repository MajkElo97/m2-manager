package pl.m2manager.contact.repository;

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
import pl.m2manager.contact.entity.Contact;
import pl.m2manager.organization.entity.Organization;
import pl.m2manager.organization.repository.OrganizationRepository;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class ContactRepositoryTest {

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
	private ContactRepository contactRepository;

	@Autowired
	private BuildingRepository buildingRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void flywayV19_createsContactsTable() {
		Integer count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'contacts'",
				Integer.class
		);
		assertThat(count).isEqualTo(1);
	}

	@Test
	void create_persistsContact() {
		Organization organization = saveOrganization("Org Contact Persist");
		Building building = saveBuilding(organization, "BLD1");
		Contact contact = saveContact(organization.getId(), building.getId());

		assertThat(contact.getId()).isNotNull();
		assertThat(contactRepository.findByIdAndOrganizationId(contact.getId(), organization.getId())).isPresent();
	}

	@Test
	void findAllByBuildingId_filtersContacts() {
		Organization organization = saveOrganization("Org Contact Filter");
		Building buildingA = saveBuilding(organization, "BLDA");
		Building buildingB = saveBuilding(organization, "BLDB");
		saveContact(organization.getId(), buildingA.getId());
		saveContact(organization.getId(), buildingB.getId());

		assertThat(contactRepository.findAllByOrganizationIdAndBuildingId(organization.getId(), buildingA.getId()))
				.hasSize(1);
		assertThat(contactRepository.findAllByOrganizationIdAndBuildingId(organization.getId(), null))
				.hasSize(2);
	}

	@Test
	void nullableNames_persist() {
		Organization organization = saveOrganization("Org Contact Nullable");
		Building building = saveBuilding(organization, "BLD2");
		Contact contact = new Contact();
		contact.setOrganizationId(organization.getId());
		contact.setBuildingId(building.getId());
		contact.setFunctionTitle("Członek zarządu");
		contactRepository.saveAndFlush(contact);

		Contact reloaded = contactRepository.findById(contact.getId()).orElseThrow();
		assertThat(reloaded.getFirstName()).isNull();
		assertThat(reloaded.getLastName()).isNull();
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

	private Contact saveContact(UUID organizationId, UUID buildingId) {
		Contact contact = new Contact();
		contact.setOrganizationId(organizationId);
		contact.setBuildingId(buildingId);
		contact.setFirstName("Jan");
		contact.setLastName("Kowalski");
		contact.setFunctionTitle("Członek zarządu");
		return contactRepository.saveAndFlush(contact);
	}
}
