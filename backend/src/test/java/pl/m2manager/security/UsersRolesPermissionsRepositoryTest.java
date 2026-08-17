package pl.m2manager.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.m2manager.organization.entity.Organization;
import pl.m2manager.organization.repository.OrganizationRepository;
import pl.m2manager.permission.entity.Permission;
import pl.m2manager.permission.repository.PermissionRepository;
import pl.m2manager.role.entity.Role;
import pl.m2manager.role.repository.RoleRepository;
import pl.m2manager.user.entity.User;
import pl.m2manager.user.entity.UserRole;
import pl.m2manager.user.repository.UserRepository;
import pl.m2manager.user.repository.UserRoleRepository;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class UsersRolesPermissionsRepositoryTest {

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
	private OrganizationRepository organizationRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PermissionRepository permissionRepository;

	@Autowired
	private UserRoleRepository userRoleRepository;

	@Test
	void flywayV4_createsPermissionCatalog() {
		assertThat(permissionRepository.count()).isEqualTo(85);
		assertThat(permissionRepository.findByCode("BUILDINGS_VIEW")).isPresent();
		assertThat(permissionRepository.findByCode("SETTINGS_ADMIN")).isPresent();
	}

	@Test
	void permissionCode_isUnique() {
		Permission duplicate = new Permission();
		duplicate.setId(UUID.fromString("c0000000-0000-4000-8000-999999999999"));
		duplicate.setCode("BUILDINGS_VIEW");
		duplicate.setModule("BUILDINGS");
		duplicate.setAction("VIEW");
		duplicate.setDescription("Duplicate");

		assertThatThrownBy(() -> permissionRepository.saveAndFlush(duplicate))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void user_organizationForeignKeyWorks() {
		Organization organization = saveOrganization("Org User FK");
		User user = saveUser(organization, "user@example.com");

		assertThat(user.getId()).isNotNull();
		assertThat(userRepository.findByIdAndOrganizationId(user.getId(), organization.getId())).isPresent();
	}

	@Test
	void role_organizationForeignKeyWorks() {
		Organization organization = saveOrganization("Org Role FK");
		Role role = saveRole(organization, "ADMIN");

		assertThat(role.getId()).isNotNull();
		assertThat(roleRepository.findByIdAndOrganizationId(role.getId(), organization.getId())).isPresent();
	}

	@Test
	void roleName_isUniqueWithinOrganization() {
		Organization organization = saveOrganization("Org Role Unique");
		saveRole(organization, "ADMIN");

		Role duplicate = new Role();
		duplicate.setOrganization(organization);
		duplicate.setName("ADMIN");

		assertThatThrownBy(() -> roleRepository.saveAndFlush(duplicate))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void roleName_isAllowedInDifferentOrganizations() {
		Organization orgA = saveOrganization("Org A Roles");
		Organization orgB = saveOrganization("Org B Roles");

		saveRole(orgA, "ADMIN");
		Role roleB = saveRole(orgB, "ADMIN");

		assertThat(roleB.getId()).isNotNull();
		assertThat(roleRepository.findByOrganizationIdAndName(orgA.getId(), "ADMIN")).isPresent();
		assertThat(roleRepository.findByOrganizationIdAndName(orgB.getId(), "ADMIN")).isPresent();
	}

	@Test
	void email_isAllowedInDifferentOrganizations() {
		Organization orgA = saveOrganization("Org A Users");
		Organization orgB = saveOrganization("Org B Users");

		saveUser(orgA, "shared@example.com");
		User userB = saveUser(orgB, "shared@example.com");

		assertThat(userB.getId()).isNotNull();
		assertThat(userRepository.findByOrganizationIdAndEmail(orgA.getId(), "shared@example.com")).isPresent();
		assertThat(userRepository.findByOrganizationIdAndEmail(orgB.getId(), "shared@example.com")).isPresent();
	}

	@Test
	void email_isNotAllowedTwiceInSameOrganization() {
		Organization organization = saveOrganization("Org Email Unique");
		saveUser(organization, "duplicate@example.com");

		User duplicate = new User();
		duplicate.setOrganization(organization);
		duplicate.setEmail("duplicate@example.com");
		duplicate.setPasswordHash("hash");

		assertThatThrownBy(() -> userRepository.saveAndFlush(duplicate))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void userRole_allowsAssignmentWithinSameOrganization() {
		Organization organization = saveOrganization("Org Same Tenant");
		User user = saveUser(organization, "same-tenant@example.com");
		Role role = saveRole(organization, "KIEROWNIK");

		UserRole userRole = new UserRole(organization.getId(), user.getId(), role.getId());
		userRoleRepository.saveAndFlush(userRole);

		assertThat(userRoleRepository.findById(userRole.getId())).isPresent();
	}

	@Test
	void userRole_rejectsCrossTenantAssignment() {
		Organization orgA = saveOrganization("Org Cross A");
		Organization orgB = saveOrganization("Org Cross B");
		User userA = saveUser(orgA, "user-a@example.com");
		Role roleB = saveRole(orgB, "ADMIN");

		UserRole crossTenantAssignment = new UserRole(orgA.getId(), userA.getId(), roleB.getId());

		assertThatThrownBy(() -> userRoleRepository.saveAndFlush(crossTenantAssignment))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	private Organization saveOrganization(String name) {
		Organization organization = new Organization();
		organization.setName(name);
		organization.setTimezone("Europe/Warsaw");
		return organizationRepository.saveAndFlush(organization);
	}

	private User saveUser(Organization organization, String email) {
		User user = new User();
		user.setOrganization(organization);
		user.setEmail(email);
		user.setPasswordHash("test-password-hash");
		return userRepository.saveAndFlush(user);
	}

	private Role saveRole(Organization organization, String name) {
		Role role = new Role();
		role.setOrganization(organization);
		role.setName(name);
		return roleRepository.saveAndFlush(role);
	}
}
