package pl.m2manager.supervisor.service;

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
import pl.m2manager.manager.entity.Manager;
import pl.m2manager.manager.repository.ManagerRepository;
import pl.m2manager.organization.entity.Organization;
import pl.m2manager.organization.repository.OrganizationRepository;
import pl.m2manager.supervisor.dto.request.CreateSupervisorRequest;
import pl.m2manager.supervisor.dto.request.UpdateSupervisorRequest;
import pl.m2manager.supervisor.entity.Supervisor;
import pl.m2manager.supervisor.repository.SupervisorRepository;
import pl.m2manager.tenant.TenantContext;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class SupervisorServiceTest {

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
	private SupervisorService supervisorService;

	@Autowired
	private SupervisorRepository supervisorRepository;

	@Autowired
	private ManagerRepository managerRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	private Organization organizationA;
	private Organization organizationB;

	@BeforeEach
	void setUp() {
		organizationA = saveOrganization("Supervisor Org A");
		organizationB = saveOrganization("Supervisor Org B");
		when(tenantContext.getCurrentOrganizationId()).thenReturn(organizationA.getId());
	}

	@Test
	void create_usesCurrentTenantContext() {
		Manager manager = saveManager(organizationA, "MGR1");
		var created = supervisorService.create(sampleCreateRequest(manager.getId(), "OP0001"));

		Supervisor persisted = supervisorRepository.findByIdAndOrganizationId(created.id(), organizationA.getId()).orElseThrow();
		assertThat(persisted.getOrganizationId()).isEqualTo(organizationA.getId());
		assertThat(created.managerCode()).isEqualTo("MGR1");
	}

	@Test
	void create_managerMustBelongToTenant() {
		Manager managerB = saveManager(organizationB, "MGR2");

		assertThatThrownBy(() -> supervisorService.create(sampleCreateRequest(managerB.getId(), "OP0002")))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void create_duplicateCode_throwsBusinessConflict() {
		Manager manager = saveManager(organizationA, "MGR3");
		saveSupervisor(organizationA.getId(), manager.getId(), "OP0003");

		assertThatThrownBy(() -> supervisorService.create(sampleCreateRequest(manager.getId(), "OP0003")))
				.isInstanceOf(BusinessConflictException.class);
	}

	@Test
	void getById_tenantIsolation() {
		Manager managerA = saveManager(organizationA, "MGR4");
		Manager managerB = saveManager(organizationB, "MGR5");
		Supervisor supervisorA = saveSupervisor(organizationA.getId(), managerA.getId(), "OP0004");
		Supervisor supervisorB = saveSupervisor(organizationB.getId(), managerB.getId(), "OP0005");

		assertThat(supervisorService.getById(supervisorA.getId()).code()).isEqualTo("OP0004");

		assertThatThrownBy(() -> supervisorService.getById(supervisorB.getId()))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void update_managerMustBelongToTenant() {
		Manager managerA = saveManager(organizationA, "MGR6");
		Manager managerB = saveManager(organizationB, "MGR7");
		Supervisor supervisor = saveSupervisor(organizationA.getId(), managerA.getId(), "OP0006");

		assertThatThrownBy(() -> supervisorService.update(supervisor.getId(), sampleUpdateRequest(managerB.getId(), "OP0006")))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void getAll_filtersByManagerId() {
		Manager managerA = saveManager(organizationA, "MGR8");
		Manager managerB = saveManager(organizationA, "MGR9");
		saveSupervisor(organizationA.getId(), managerA.getId(), "OP0007");
		saveSupervisor(organizationA.getId(), managerB.getId(), "OP0008");

		assertThat(supervisorService.getAll(managerA.getId(), null, null))
				.extracting(response -> response.code())
				.containsExactly("OP0007");
	}

	private CreateSupervisorRequest sampleCreateRequest(UUID managerId, String code) {
		return new CreateSupervisorRequest(managerId, code, "Jan", "Kowalski", "123456789", "jan@example.com", null);
	}

	private UpdateSupervisorRequest sampleUpdateRequest(UUID managerId, String code) {
		return new UpdateSupervisorRequest(managerId, code, "Jan", "Kowalski", "123456789", "jan@example.com", null, true);
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
		Supervisor supervisor = new Supervisor();
		supervisor.setOrganizationId(organizationId);
		supervisor.setManagerId(managerId);
		supervisor.setCode(code);
		supervisor.setFirstName("Jan");
		supervisor.setLastName("Kowalski");
		return supervisorRepository.saveAndFlush(supervisor);
	}
}
