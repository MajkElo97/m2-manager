package pl.m2manager.manager.service;

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
import pl.m2manager.manager.dto.request.CreateManagerRequest;
import pl.m2manager.manager.dto.request.UpdateManagerRequest;
import pl.m2manager.manager.entity.Manager;
import pl.m2manager.manager.repository.ManagerRepository;
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
class ManagerServiceTest {

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
	private ManagerService managerService;

	@Autowired
	private ManagerRepository managerRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	private Organization organizationA;
	private Organization organizationB;

	@BeforeEach
	void setUp() {
		organizationA = saveOrganization("Org Manager Service A");
		organizationB = saveOrganization("Org Manager Service B");
		when(tenantContext.getCurrentOrganizationId()).thenReturn(organizationA.getId());
	}

	@Test
	void create_usesCurrentTenantContext() {
		var created = managerService.create(sampleCreateRequest("ZA0101", "Manager One"));

		Manager persisted = managerRepository.findByIdAndOrganizationId(created.id(), organizationA.getId()).orElseThrow();
		assertThat(persisted.getOrganizationId()).isEqualTo(organizationA.getId());
		assertThat(persisted.isActive()).isTrue();
	}

	@Test
	void create_duplicateCode_throwsBusinessConflict() {
		saveManager(organizationA.getId(), "ZA0102", "Manager A");

		assertThatThrownBy(() -> managerService.create(sampleCreateRequest("ZA0102", "Manager B")))
				.isInstanceOf(BusinessConflictException.class);
	}

	@Test
	void create_duplicateName_throwsBusinessConflict() {
		saveManager(organizationA.getId(), "ZA0103", "Same Name");

		assertThatThrownBy(() -> managerService.create(sampleCreateRequest("ZA0104", "Same Name")))
				.isInstanceOf(BusinessConflictException.class);
	}

	@Test
	void getById_tenantIsolation() {
		Manager managerA = saveManager(organizationA.getId(), "ZA0105", "Manager A");
		Manager managerB = saveManager(organizationB.getId(), "ZA0106", "Manager B");

		assertThat(managerService.getById(managerA.getId()).code()).isEqualTo("ZA0105");

		assertThatThrownBy(() -> managerService.getById(managerB.getId()))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void getAll_returnsOnlyCurrentTenantManagers() {
		saveManager(organizationA.getId(), "ZA0107", "Manager A");
		saveManager(organizationB.getId(), "ZA0108", "Manager B");

		assertThat(managerService.getAll(null, null))
				.extracting(response -> response.code())
				.containsExactly("ZA0107");
	}

	@Test
	void update_tenantIsolation() {
		Manager managerB = saveManager(organizationB.getId(), "ZA0109", "Manager B");

		assertThatThrownBy(() -> managerService.update(managerB.getId(), sampleUpdateRequest("ZA0109", "Updated")))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void deactivate_setsActiveFalse() {
		Manager manager = saveManager(organizationA.getId(), "ZA0110", "To Deactivate");

		managerService.deactivate(manager.getId());

		Manager reloaded = managerRepository.findById(manager.getId()).orElseThrow();
		assertThat(reloaded.isActive()).isFalse();
	}

	@Test
	void deactivate_tenantIsolation() {
		Manager managerB = saveManager(organizationB.getId(), "ZA0111", "Manager B");

		assertThatThrownBy(() -> managerService.deactivate(managerB.getId()))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void deactivate_alreadyInactive_isIdempotent() {
		Manager manager = saveManager(organizationA.getId(), "ZA0112", "Already Inactive");
		manager.setActive(false);
		managerRepository.saveAndFlush(manager);

		managerService.deactivate(manager.getId());

		Manager reloaded = managerRepository.findById(manager.getId()).orElseThrow();
		assertThat(reloaded.isActive()).isFalse();
	}

	private CreateManagerRequest sampleCreateRequest(String code, String name) {
		return new CreateManagerRequest(code, name, "123456789", "manager@example.com", "Address", null);
	}

	private UpdateManagerRequest sampleUpdateRequest(String code, String name) {
		return new UpdateManagerRequest(code, name, "123456789", "manager@example.com", "Address", null);
	}

	private Organization saveOrganization(String name) {
		Organization organization = new Organization();
		organization.setName(name);
		organization.setSlug(name.toLowerCase().replace(' ', '-') + "-" + UUID.randomUUID().toString().substring(0, 8));
		organization.setTimezone("Europe/Warsaw");
		return organizationRepository.saveAndFlush(organization);
	}

	private Manager saveManager(UUID organizationId, String code, String name) {
		Manager manager = new Manager();
		manager.setOrganizationId(organizationId);
		manager.setCode(code);
		manager.setName(name);
		return managerRepository.saveAndFlush(manager);
	}
}
