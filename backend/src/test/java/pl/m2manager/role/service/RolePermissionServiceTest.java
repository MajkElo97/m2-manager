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
import pl.m2manager.permission.dto.PermissionResponse;
import pl.m2manager.role.entity.Role;
import pl.m2manager.role.repository.RoleRepository;
import pl.m2manager.tenant.TenantContext;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class RolePermissionServiceTest {

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
	private RolePermissionService rolePermissionService;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private RoleRepository roleRepository;

	private Organization organizationA;
	private Organization organizationB;
	private Role roleA;

	@BeforeEach
	void setUp() {
		organizationA = saveOrganization("Org A RP");
		organizationB = saveOrganization("Org B RP");
		roleA = saveRole(organizationA, "Manager");
		saveRole(organizationB, "Admin B");
		when(tenantContext.getCurrentOrganizationId()).thenReturn(organizationA.getId());
	}

	@Test
	void assignPermission_addsPermissionToRole() {
		rolePermissionService.assignPermission(roleA.getId(), "BUILDINGS_VIEW");

		List<PermissionResponse> permissions = rolePermissionService.listPermissions(roleA.getId());
		assertThat(permissions).extracting(PermissionResponse::code).containsExactly("BUILDINGS_VIEW");
	}

	@Test
	void removePermission_removesPermissionFromRole() {
		rolePermissionService.assignPermission(roleA.getId(), "BUILDINGS_VIEW");
		rolePermissionService.removePermission(roleA.getId(), "BUILDINGS_VIEW");

		assertThat(rolePermissionService.listPermissions(roleA.getId())).isEmpty();
	}

	@Test
	void assignPermission_crossOrganizationRole_rejected() {
		Role roleB = roleRepository.findByOrganizationId(organizationB.getId()).getFirst();

		assertThatThrownBy(() -> rolePermissionService.assignPermission(roleB.getId(), "BUILDINGS_VIEW"))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void assignAdmin_doesNotCreateChildPermissions() {
		rolePermissionService.assignPermission(roleA.getId(), "BUILDINGS_ADMIN");

		List<PermissionResponse> permissions = rolePermissionService.listPermissions(roleA.getId());
		assertThat(permissions).hasSize(1);
		assertThat(permissions.getFirst().code()).isEqualTo("BUILDINGS_ADMIN");
	}

	@Test
	void replacePermissions_systemRole_rejected() {
		Role systemRole = saveSystemRole(organizationA, "SYS");

		assertThatThrownBy(() -> rolePermissionService.replacePermissions(
				systemRole.getId(),
				List.of("BUILDINGS_VIEW")
		)).isInstanceOf(BusinessConflictException.class);
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
