package pl.m2manager.role.service;

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
import pl.m2manager.organization.entity.Organization;
import pl.m2manager.organization.repository.OrganizationRepository;
import pl.m2manager.role.dto.CreateRoleRequest;
import pl.m2manager.role.dto.RoleResponse;
import pl.m2manager.role.dto.UpdateRoleRequest;
import pl.m2manager.role.entity.Role;
import pl.m2manager.role.repository.RoleRepository;
import pl.m2manager.tenant.TenantContext;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class RoleServiceTest {

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
	private RoleService roleService;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	private Organization organizationA;
	private Organization organizationB;

	@BeforeEach
	void setUp() {
		organizationA = saveOrganization("Org A");
		organizationB = saveOrganization("Org B");
		when(tenantContext.getCurrentOrganizationId()).thenReturn(organizationA.getId());
	}

	@Test
	void createRole_assignsCurrentOrganizationId() {
		RoleResponse created = roleService.createRole(new CreateRoleRequest("Custom Role", "Description"));

		Role persisted = roleRepository.findByIdAndOrganizationId(created.id(), organizationA.getId()).orElseThrow();
		assertThat(persisted.getOrganization().getId()).isEqualTo(organizationA.getId());
		assertThat(persisted.getName()).isEqualTo("Custom Role");
		assertThat(persisted.getDescription()).isEqualTo("Description");
	}

	@Test
	void findById_returnsRoleOnlyWithinCurrentOrganization() {
		Role roleInA = saveRole(organizationA, "Role A");
		Role roleInB = saveRole(organizationB, "Role B");

		assertThat(roleService.findById(roleInA.getId()).name()).isEqualTo("Role A");

		assertThatThrownBy(() -> roleService.findById(roleInB.getId()))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void createRole_sameNameAllowedInDifferentOrganizations() {
		saveRole(organizationB, "Shared Name");

		RoleResponse created = roleService.createRole(new CreateRoleRequest("Shared Name", null));

		assertThat(created.name()).isEqualTo("Shared Name");
		assertThat(roleRepository.findByOrganizationIdAndName(organizationA.getId(), "Shared Name")).isPresent();
		assertThat(roleRepository.findByOrganizationIdAndName(organizationB.getId(), "Shared Name")).isPresent();
	}

	@Test
	void createRole_duplicateNameInSameOrganization_rejected() {
		saveRole(organizationA, "Duplicate");

		assertThatThrownBy(() -> roleService.createRole(new CreateRoleRequest("Duplicate", null)))
				.isInstanceOf(BusinessConflictException.class);
	}

	@Test
	void updateRole_systemRoleNameChange_rejected() {
		Role systemRole = saveSystemRole(organizationA, "ADMIN");

		assertThatThrownBy(() -> roleService.updateRole(
				systemRole.getId(),
				new UpdateRoleRequest("Renamed Admin", null, null)
		)).isInstanceOf(BusinessConflictException.class);
	}

	@Test
	void deactivateRole_systemRole_rejected() {
		Role systemRole = saveSystemRole(organizationA, "KIEROWNIK");

		assertThatThrownBy(() -> roleService.deactivateRole(systemRole.getId()))
				.isInstanceOf(BusinessConflictException.class);
	}

	@Test
	void updateRole_systemRoleDeactivation_rejected() {
		Role systemRole = saveSystemRole(organizationA, "BIURO");

		assertThatThrownBy(() -> roleService.updateRole(
				systemRole.getId(),
				new UpdateRoleRequest(null, null, false)
		)).isInstanceOf(BusinessConflictException.class);
	}

	@Test
	void updateRole_nonSystemRole_canBeDeactivated() {
		Role role = saveRole(organizationA, "Temporary");

		RoleResponse updated = roleService.updateRole(role.getId(), new UpdateRoleRequest(null, null, false));

		assertThat(updated.active()).isFalse();
	}

	private Organization saveOrganization(String name) {
		Organization organization = new Organization();
		organization.setName(name);
		organization.setSlug(name.toLowerCase().replace(' ', '-') + "-" + UUID.randomUUID().toString().substring(0, 8));
		organization.setTimezone("Europe/Warsaw");
		return organizationRepository.saveAndFlush(organization);
	}

	private Role saveRole(Organization organization, String name) {
		Role role = new Role();
		role.setOrganization(organization);
		role.setName(name);
		return roleRepository.saveAndFlush(role);
	}

	private Role saveSystemRole(Organization organization, String name) {
		Role role = saveRole(organization, name);
		role.setSystemRole(true);
		return roleRepository.saveAndFlush(role);
	}
}
