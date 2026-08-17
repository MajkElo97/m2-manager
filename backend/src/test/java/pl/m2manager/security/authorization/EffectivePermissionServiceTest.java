package pl.m2manager.security.authorization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.m2manager.organization.entity.Organization;
import pl.m2manager.organization.repository.OrganizationRepository;
import pl.m2manager.permission.repository.PermissionRepository;
import pl.m2manager.role.entity.Role;
import pl.m2manager.role.entity.RolePermission;
import pl.m2manager.role.repository.RolePermissionRepository;
import pl.m2manager.role.repository.RoleRepository;
import pl.m2manager.user.entity.User;
import pl.m2manager.user.entity.UserRole;
import pl.m2manager.user.repository.UserRepository;
import pl.m2manager.user.repository.UserRoleRepository;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class EffectivePermissionServiceTest {

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
	private EffectivePermissionService effectivePermissionService;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private RolePermissionRepository rolePermissionRepository;

	@Autowired
	private PermissionRepository permissionRepository;

	@Autowired
	private UserRoleRepository userRoleRepository;

	private Organization organizationA;
	private Organization organizationB;
	private User userA;
	private User userB;
	private Role kierownikRole;

	@BeforeEach
	void setUp() {
		organizationA = saveOrganization("Org A EP");
		organizationB = saveOrganization("Org B EP");
		userA = saveUser(organizationA, "user-a@example.com");
		userB = saveUser(organizationB, "user-b@example.com");
		kierownikRole = saveRole(organizationA, "KIEROWNIK");
	}

	@Test
	void resolvePermissionCodes_returnsAssignedPermissions() {
		assignPermissions(kierownikRole,
				"BUILDINGS_VIEW", "BUILDINGS_EDIT", "SCHEDULE_VIEW", "SCHEDULE_EDIT");
		assignRole(userA, kierownikRole);

		Set<String> codes = effectivePermissionService.resolvePermissionCodes(userA.getId(), organizationA.getId());

		assertThat(codes).containsExactlyInAnyOrder(
				"BUILDINGS_VIEW", "BUILDINGS_EDIT", "SCHEDULE_VIEW", "SCHEDULE_EDIT"
		);
	}

	@Test
	void hasEffectivePermission_partialModulePermissions() {
		assignPermissions(kierownikRole,
				"BUILDINGS_VIEW", "BUILDINGS_EDIT", "SCHEDULE_VIEW", "SCHEDULE_EDIT");
		assignRole(userA, kierownikRole);

		assertThat(effectivePermissionService.hasEffectivePermission(userA.getId(), organizationA.getId(), "BUILDINGS_VIEW"))
				.isTrue();
		assertThat(effectivePermissionService.hasEffectivePermission(userA.getId(), organizationA.getId(), "BUILDINGS_EDIT"))
				.isTrue();
		assertThat(effectivePermissionService.hasEffectivePermission(userA.getId(), organizationA.getId(), "BUILDINGS_CREATE"))
				.isFalse();
		assertThat(effectivePermissionService.hasEffectivePermission(userA.getId(), organizationA.getId(), "BUILDINGS_DELETE"))
				.isFalse();
		assertThat(effectivePermissionService.hasEffectivePermission(userA.getId(), organizationA.getId(), "BUILDINGS_ADMIN"))
				.isFalse();

		assertThat(effectivePermissionService.hasEffectivePermission(userA.getId(), organizationA.getId(), "SCHEDULE_VIEW"))
				.isTrue();
		assertThat(effectivePermissionService.hasEffectivePermission(userA.getId(), organizationA.getId(), "SCHEDULE_EDIT"))
				.isTrue();
		assertThat(effectivePermissionService.hasEffectivePermission(userA.getId(), organizationA.getId(), "SCHEDULE_CREATE"))
				.isFalse();
		assertThat(effectivePermissionService.hasEffectivePermission(userA.getId(), organizationA.getId(), "SCHEDULE_DELETE"))
				.isFalse();
	}

	@Test
	void hasEffectivePermission_adminOverrideGrantsAllModuleActions() {
		assignPermissions(kierownikRole, "BUILDINGS_ADMIN");
		assignRole(userA, kierownikRole);

		assertThat(effectivePermissionService.hasEffectivePermission(userA.getId(), organizationA.getId(), "BUILDINGS_VIEW"))
				.isTrue();
		assertThat(effectivePermissionService.hasEffectivePermission(userA.getId(), organizationA.getId(), "BUILDINGS_CREATE"))
				.isTrue();
		assertThat(effectivePermissionService.hasEffectivePermission(userA.getId(), organizationA.getId(), "BUILDINGS_EDIT"))
				.isTrue();
		assertThat(effectivePermissionService.hasEffectivePermission(userA.getId(), organizationA.getId(), "BUILDINGS_DELETE"))
				.isTrue();
		assertThat(effectivePermissionService.hasEffectivePermission(userA.getId(), organizationA.getId(), "BUILDINGS_ADMIN"))
				.isTrue();
	}

	@Test
	void tenantIsolation_userADoesNotReceiveOrganizationBPermissions() {
		Role roleA = saveRole(organizationA, "Role A");
		assignPermissions(roleA, "BUILDINGS_VIEW");
		assignRole(userA, roleA);

		Role roleB = saveRole(organizationB, "Role B");
		assignPermissions(roleB, "BUILDINGS_ADMIN");
		assignRole(userB, roleB);

		Set<String> userACodes = effectivePermissionService.resolvePermissionCodes(userA.getId(), organizationA.getId());
		Set<String> userBCodes = effectivePermissionService.resolvePermissionCodes(userB.getId(), organizationB.getId());

		assertThat(userACodes).containsExactly("BUILDINGS_VIEW");
		assertThat(userACodes).doesNotContain("BUILDINGS_ADMIN");
		assertThat(userBCodes).containsExactly("BUILDINGS_ADMIN");
	}

	@Test
	void tenantIsolation_userACannotObtainRoleBPermissionsViaWrongOrganizationContext() {
		Role roleB = saveRole(organizationB, "Role B");
		assignPermissions(roleB, "BUILDINGS_ADMIN");
		assignRole(userB, roleB);

		Set<String> codes = effectivePermissionService.resolvePermissionCodes(userA.getId(), organizationB.getId());

		assertThat(codes).isEmpty();
	}

	private Organization saveOrganization(String name) {
		Organization organization = new Organization();
		organization.setName(name);
		organization.setSlug(name.toLowerCase().replace(' ', '-') + "-" + UUID.randomUUID().toString().substring(0, 8));
		organization.setTimezone("Europe/Warsaw");
		return organizationRepository.saveAndFlush(organization);
	}

	private User saveUser(Organization organization, String email) {
		User user = new User();
		user.setOrganization(organization);
		user.setEmail(email);
		user.setPasswordHash("hash");
		return userRepository.saveAndFlush(user);
	}

	private Role saveRole(Organization organization, String name) {
		Role role = new Role();
		role.setOrganization(organization);
		role.setName(name);
		return roleRepository.saveAndFlush(role);
	}

	private void assignPermissions(Role role, String... permissionCodes) {
		for (String code : permissionCodes) {
			var permission = permissionRepository.findByCode(code).orElseThrow();
			rolePermissionRepository.saveAndFlush(new RolePermission(role.getId(), permission.getId()));
		}
	}

	private void assignRole(User user, Role role) {
		userRoleRepository.saveAndFlush(new UserRole(user.getOrganization().getId(), user.getId(), role.getId()));
	}
}
