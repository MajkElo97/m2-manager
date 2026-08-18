package pl.m2manager.dev;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.m2manager.organization.repository.OrganizationRepository;
import pl.m2manager.permission.repository.PermissionRepository;
import pl.m2manager.role.repository.RolePermissionRepository;
import pl.m2manager.role.repository.RoleRepository;
import pl.m2manager.security.auth.AuthenticationService;
import pl.m2manager.security.auth.dto.AuthenticationResult;
import pl.m2manager.user.entity.User;
import pl.m2manager.user.entity.UserRoleId;
import pl.m2manager.user.repository.UserRepository;
import pl.m2manager.user.repository.UserRoleRepository;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class DevAdminUserSeedIntegrationTest {

	static final UUID DEV_ORGANIZATION_ID = UUID.fromString("a0000000-0000-4000-8000-000000000001");
	static final UUID DEV_USER_ID = UUID.fromString("b0000000-0000-4000-8000-000000000001");
	static final UUID DEV_ROLE_ID = UUID.fromString("b0000000-0000-4000-8000-000000000002");
	static final String DEV_ORGANIZATION_SLUG = "m2-manager-dev";
	static final String DEV_USER_EMAIL = "admin@m2manager.local";
	static final String DEV_USER_PASSWORD = "Admin123!";

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
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private UserRoleRepository userRoleRepository;

	@Autowired
	private RolePermissionRepository rolePermissionRepository;

	@Autowired
	private PermissionRepository permissionRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private AuthenticationService authenticationService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void devAdminUser_existsInDevOrganization() {
		User user = userRepository.findByIdAndOrganizationId(DEV_USER_ID, DEV_ORGANIZATION_ID).orElseThrow();

		assertThat(user.getEmail()).isEqualTo(DEV_USER_EMAIL);
		assertThat(user.getFirstName()).isEqualTo("Admin");
		assertThat(user.getLastName()).isEqualTo("M2");
		assertThat(user.isActive()).isTrue();
		assertThat(organizationRepository.findBySlug(DEV_ORGANIZATION_SLUG)).isPresent();
	}

	@Test
	void devAdminUser_passwordAuthenticates() {
		User user = userRepository.findByIdAndOrganizationId(DEV_USER_ID, DEV_ORGANIZATION_ID).orElseThrow();

		assertThat(passwordEncoder.matches(DEV_USER_PASSWORD, user.getPasswordHash())).isTrue();

		AuthenticationResult result = authenticationService.authenticate(
				DEV_ORGANIZATION_SLUG,
				DEV_USER_EMAIL,
				DEV_USER_PASSWORD
		);

		assertThat(result.userId()).isEqualTo(DEV_USER_ID);
		assertThat(result.organizationId()).isEqualTo(DEV_ORGANIZATION_ID);
		assertThat(result.email()).isEqualTo(DEV_USER_EMAIL);
	}

	@Test
	void devAdminUser_hasSuperAdminRoleAssigned() {
		var role = roleRepository.findByIdAndOrganizationId(DEV_ROLE_ID, DEV_ORGANIZATION_ID).orElseThrow();

		assertThat(role.getName()).isEqualTo("SUPER_ADMIN");
		assertThat(role.isSystemRole()).isTrue();
		assertThat(role.isActive()).isTrue();
		assertThat(userRoleRepository.findById(new UserRoleId(DEV_USER_ID, DEV_ROLE_ID)))
				.isPresent();
	}

	@Test
	void devSuperAdminRole_hasAllPermissionsAssigned() {
		long assignedPermissions = rolePermissionRepository.findPermissionsByRoleIdAndOrganizationId(
				DEV_ROLE_ID,
				DEV_ORGANIZATION_ID
		).size();

		assertThat(permissionRepository.count()).isEqualTo(85);
		assertThat(assignedPermissions).isEqualTo(85);
	}

	@Test
	void devAdminUserSeed_isIdempotent() {
		int permissionsBefore = rolePermissionRepository.findPermissionsByRoleIdAndOrganizationId(
				DEV_ROLE_ID,
				DEV_ORGANIZATION_ID
		).size();

		jdbcTemplate.execute("""
				INSERT INTO users (
				    id,
				    organization_id,
				    email,
				    password_hash,
				    first_name,
				    last_name,
				    active
				)
				VALUES (
				    'b0000000-0000-4000-8000-000000000001',
				    'a0000000-0000-4000-8000-000000000001',
				    'admin@m2manager.local',
				    '$2a$12$Jt8iSmQesv59..E7KFXyI.pn/M9HZkEQD0j1uBZMvM.bV9ni4jeju',
				    'Admin',
				    'M2',
				    TRUE
				)
				ON CONFLICT (id) DO NOTHING
				""");

		jdbcTemplate.execute("""
				INSERT INTO roles (
				    id,
				    organization_id,
				    name,
				    description,
				    system_role,
				    active
				)
				VALUES (
				    'b0000000-0000-4000-8000-000000000002',
				    'a0000000-0000-4000-8000-000000000001',
				    'SUPER_ADMIN',
				    'Development super administrator',
				    TRUE,
				    TRUE
				)
				ON CONFLICT (id) DO NOTHING
				""");

		jdbcTemplate.execute("""
				INSERT INTO user_roles (organization_id, user_id, role_id)
				VALUES (
				    'a0000000-0000-4000-8000-000000000001',
				    'b0000000-0000-4000-8000-000000000001',
				    'b0000000-0000-4000-8000-000000000002'
				)
				ON CONFLICT (user_id, role_id) DO NOTHING
				""");

		jdbcTemplate.execute("""
				INSERT INTO role_permissions (role_id, permission_id)
				SELECT 'b0000000-0000-4000-8000-000000000002', p.id
				FROM permissions p
				ON CONFLICT (role_id, permission_id) DO NOTHING
				""");

		assertThat(userRepository.findByIdAndOrganizationId(DEV_USER_ID, DEV_ORGANIZATION_ID)).isPresent();
		assertThat(roleRepository.findByIdAndOrganizationId(DEV_ROLE_ID, DEV_ORGANIZATION_ID)).isPresent();
		assertThat(userRoleRepository.findById(new UserRoleId(DEV_USER_ID, DEV_ROLE_ID))).isPresent();
		assertThat(rolePermissionRepository.findPermissionsByRoleIdAndOrganizationId(
				DEV_ROLE_ID,
				DEV_ORGANIZATION_ID
		)).hasSize(permissionsBefore);
		assertThat(permissionsBefore).isEqualTo(85);
	}
}
