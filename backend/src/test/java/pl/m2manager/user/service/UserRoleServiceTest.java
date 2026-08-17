package pl.m2manager.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.m2manager.common.exception.ResourceNotFoundException;
import pl.m2manager.organization.entity.Organization;
import pl.m2manager.organization.repository.OrganizationRepository;
import pl.m2manager.role.dto.RoleResponse;
import pl.m2manager.role.entity.Role;
import pl.m2manager.role.repository.RoleRepository;
import pl.m2manager.tenant.TenantContext;
import pl.m2manager.user.entity.User;
import pl.m2manager.user.entity.UserRole;
import pl.m2manager.user.repository.UserRepository;
import pl.m2manager.user.repository.UserRoleRepository;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class UserRoleServiceTest {

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
	private UserRoleService userRoleService;

	@Autowired
	private UserRoleRepository userRoleRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private UserRepository userRepository;

	private Organization organizationA;
	private User userA;
	private Role roleA;
	private Role roleB;

	@BeforeEach
	void setUp() {
		Organization organizationB = saveOrganization("Org B UR");
		organizationA = saveOrganization("Org A UR");
		userA = saveUser(organizationA, "user-a@example.com");
		saveUser(organizationB, "user-b@example.com");
		roleA = saveRole(organizationA, "KIEROWNIK");
		roleB = saveRole(organizationB, "ADMIN");
		when(tenantContext.getCurrentOrganizationId()).thenReturn(organizationA.getId());
	}

	@Test
	void assignRole_addsRoleToUserInSameOrganization() {
		userRoleService.assignRole(userA.getId(), roleA.getId());

		assertThat(userRoleService.listRoles(userA.getId())).extracting(RoleResponse::name).containsExactly("KIEROWNIK");
	}

	@Test
	void removeRole_removesRoleFromUser() {
		userRoleService.assignRole(userA.getId(), roleA.getId());
		userRoleService.removeRole(userA.getId(), roleA.getId());

		assertThat(userRoleService.listRoles(userA.getId())).isEmpty();
	}

	@Test
	void assignRole_crossOrganizationRole_rejected() {
		assertThatThrownBy(() -> userRoleService.assignRole(userA.getId(), roleB.getId()))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void assignRole_crossTenantAssignment_rejectedByDatabase() {
		UserRole crossTenantAssignment = new UserRole(organizationA.getId(), userA.getId(), roleB.getId());

		assertThatThrownBy(() -> userRoleRepository.saveAndFlush(crossTenantAssignment))
				.isInstanceOf(DataIntegrityViolationException.class);
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
}
