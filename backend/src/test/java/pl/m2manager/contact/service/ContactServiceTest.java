package pl.m2manager.contact.service;

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
import pl.m2manager.building.entity.Building;
import pl.m2manager.building.entity.BuildingStatus;
import pl.m2manager.building.repository.BuildingRepository;
import pl.m2manager.common.exception.ResourceNotFoundException;
import pl.m2manager.contact.dto.request.CreateContactRequest;
import pl.m2manager.contact.dto.request.UpdateContactRequest;
import pl.m2manager.contact.entity.Contact;
import pl.m2manager.contact.repository.ContactRepository;
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
class ContactServiceTest {

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
	private ContactService contactService;

	@Autowired
	private ContactRepository contactRepository;

	@Autowired
	private BuildingRepository buildingRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	private Organization organizationA;
	private Organization organizationB;

	@BeforeEach
	void setUp() {
		organizationA = saveOrganization("Contact Org A");
		organizationB = saveOrganization("Contact Org B");
		when(tenantContext.getCurrentOrganizationId()).thenReturn(organizationA.getId());
	}

	@Test
	void create_usesCurrentTenantContext() {
		Building building = saveBuilding(organizationA, "BLD1");
		var created = contactService.create(sampleCreateRequest(building.getId()));

		Contact persisted = contactRepository.findByIdAndOrganizationId(created.id(), organizationA.getId()).orElseThrow();
		assertThat(persisted.getOrganizationId()).isEqualTo(organizationA.getId());
		assertThat(created.buildingCode()).isEqualTo("BLD1");
	}

	@Test
	void create_buildingMustBelongToTenant() {
		Building buildingB = saveBuilding(organizationB, "BLD2");

		assertThatThrownBy(() -> contactService.create(sampleCreateRequest(buildingB.getId())))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void getById_tenantIsolation() {
		Building buildingA = saveBuilding(organizationA, "BLD3");
		Building buildingB = saveBuilding(organizationB, "BLD4");
		Contact contactA = saveContact(organizationA.getId(), buildingA.getId());
		Contact contactB = saveContact(organizationB.getId(), buildingB.getId());

		assertThat(contactService.getById(contactA.getId()).buildingCode()).isEqualTo("BLD3");

		assertThatThrownBy(() -> contactService.getById(contactB.getId()))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void update_buildingMustBelongToTenant() {
		Building buildingA = saveBuilding(organizationA, "BLD5");
		Building buildingB = saveBuilding(organizationB, "BLD6");
		Contact contact = saveContact(organizationA.getId(), buildingA.getId());

		assertThatThrownBy(() -> contactService.update(contact.getId(), sampleUpdateRequest(buildingB.getId())))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void getAll_withBuildingId_returnsOnlyBuildingContacts() {
		Building buildingA = saveBuilding(organizationA, "BLD7");
		Building buildingB = saveBuilding(organizationA, "BLD8");
		saveContact(organizationA.getId(), buildingA.getId());
		saveContact(organizationA.getId(), buildingB.getId());

		assertThat(contactService.getAll(buildingA.getId())).hasSize(1);
	}

	@Test
	void getAll_withForeignBuildingId_throwsNotFound() {
		Building buildingB = saveBuilding(organizationB, "BLD9");

		assertThatThrownBy(() -> contactService.getAll(buildingB.getId()))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	private CreateContactRequest sampleCreateRequest(UUID buildingId) {
		return new CreateContactRequest(buildingId, "Jan", "Kowalski", "Członek zarządu", "123456789", "jan@example.com", null);
	}

	private UpdateContactRequest sampleUpdateRequest(UUID buildingId) {
		return new UpdateContactRequest(buildingId, "Jan", "Kowalski", "Członek zarządu", "123456789", "jan@example.com", null, true);
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
