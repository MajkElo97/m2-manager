package pl.m2manager.dev;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.m2manager.role.repository.RolePermissionRepository;
import pl.m2manager.role.repository.RoleRepository;
import pl.m2manager.security.authorization.EffectivePermissionService;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
@Transactional(readOnly = true)
class DevBusinessRolesSeedIntegrationTest {

	static final UUID DEV_ORGANIZATION_ID = UUID.fromString("a0000000-0000-4000-8000-000000000001");
	static final UUID SUPER_ADMIN_USER_ID = UUID.fromString("b0000000-0000-4000-8000-000000000001");
	static final UUID ADMIN_ROLE_ID = UUID.fromString("b0000000-0000-4000-8000-000000000010");
	static final UUID BIURO_ROLE_ID = UUID.fromString("b0000000-0000-4000-8000-000000000011");
	static final UUID KOORDYNATOR_ROLE_ID = UUID.fromString("b0000000-0000-4000-8000-000000000012");
	static final UUID PRACOWNIK_ROLE_ID = UUID.fromString("b0000000-0000-4000-8000-000000000013");

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
	private RoleRepository roleRepository;

	@Autowired
	private RolePermissionRepository rolePermissionRepository;

	@Autowired
	private EffectivePermissionService effectivePermissionService;

	@Test
	void devSeed_containsBusinessRoles() {
		assertThat(roleRepository.findByOrganizationId(DEV_ORGANIZATION_ID)).hasSize(5);
	}

	@Test
	void adminRole_hasEditPermissionsForImplementedModules() {
		var permissions = rolePermissionRepository.findPermissionsByRoleIdAndOrganizationId(
				ADMIN_ROLE_ID,
				DEV_ORGANIZATION_ID
		);
		assertThat(permissions).extracting(p -> p.getCode())
				.contains("BUILDINGS_VIEW", "BUILDINGS_CREATE", "BUILDINGS_EDIT", "BUILDINGS_DELETE")
				.contains("EMPLOYEES_VIEW", "EMPLOYEES_CREATE")
				.contains("USERS_VIEW", "ROLES_VIEW");
	}

	@Test
	void biuroRole_hasDashboardBuildingsEmployeesEdit() {
		var permissions = rolePermissionRepository.findPermissionsByRoleIdAndOrganizationId(
				BIURO_ROLE_ID,
				DEV_ORGANIZATION_ID
		);
		assertThat(permissions).extracting(p -> p.getCode())
				.contains("DASHBOARD_VIEW", "BUILDINGS_EDIT", "EMPLOYEES_DELETE")
				.doesNotContain("MANAGERS_VIEW", "ROLES_VIEW");
	}

	@Test
	void koordynatorRole_hasReadOnlyDashboardBuildingsEmployees() {
		var permissions = rolePermissionRepository.findPermissionsByRoleIdAndOrganizationId(
				KOORDYNATOR_ROLE_ID,
				DEV_ORGANIZATION_ID
		);
		assertThat(permissions).extracting(p -> p.getCode())
				.containsExactlyInAnyOrder("DASHBOARD_VIEW", "BUILDINGS_VIEW", "EMPLOYEES_VIEW");
	}

	@Test
	void pracownikRole_hasDashboardViewOnly() {
		var permissions = rolePermissionRepository.findPermissionsByRoleIdAndOrganizationId(
				PRACOWNIK_ROLE_ID,
				DEV_ORGANIZATION_ID
		);
		assertThat(permissions).extracting(p -> p.getCode())
				.containsExactly("DASHBOARD_VIEW");
	}

	@Test
	void superAdmin_keepsFullAccess() {
		Set<String> permissions = effectivePermissionService.resolvePermissionCodes(
				SUPER_ADMIN_USER_ID,
				DEV_ORGANIZATION_ID
		);
		assertThat(permissions).contains("BUILDINGS_VIEW", "USERS_VIEW", "ROLES_VIEW", "SETTINGS_VIEW");
	}
}
