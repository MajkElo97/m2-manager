package pl.m2manager.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import pl.m2manager.role.entity.Role;
import pl.m2manager.role.repository.RoleRepository;
import pl.m2manager.tenant.TenantContext;
import pl.m2manager.user.dto.CreateUserRequest;
import pl.m2manager.user.dto.UpdateUserRequest;
import pl.m2manager.user.entity.User;
import pl.m2manager.user.repository.UserRepository;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class UserServiceTest {

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
	private UserService userService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private Organization organizationA;
	private Organization organizationB;
	private Role roleA;

	@BeforeEach
	void setUp() {
		organizationA = saveOrganization("Org User A");
		organizationB = saveOrganization("Org User B");
		roleA = saveRole(organizationA, "Operator");
		saveRole(organizationB, "Operator B");
		when(tenantContext.getCurrentOrganizationId()).thenReturn(organizationA.getId());
	}

	@Test
	void create_hashesPasswordAndAssignsRoles() {
		var created = userService.create(new CreateUserRequest(
				"Jan",
				"Kowalski",
				"jan.kowalski@example.com",
				"password123",
				List.of(roleA.getId()),
				null
		));

		User persisted = userRepository.findByIdAndOrganizationId(created.id(), organizationA.getId()).orElseThrow();
		assertThat(persisted.getOrganization().getId()).isEqualTo(organizationA.getId());
		assertThat(passwordEncoder.matches("password123", persisted.getPasswordHash())).isTrue();
		assertThat(created.roles()).hasSize(1);
		assertThat(created.roles().getFirst().name()).isEqualTo("Operator");
	}

	@Test
	void create_duplicateEmail_rejected() {
		userService.create(new CreateUserRequest(
				"Anna",
				"Nowak",
				"duplicate@example.com",
				"password123",
				List.of(roleA.getId()),
				null
		));

		assertThatThrownBy(() -> userService.create(new CreateUserRequest(
				"Piotr",
				"Nowak",
				"duplicate@example.com",
				"password123",
				List.of(roleA.getId()),
				null
		))).isInstanceOf(BusinessConflictException.class);
	}

	@Test
	void getById_tenantIsolation() {
		User userB = saveUser(organizationB, "other@example.com");

		assertThatThrownBy(() -> userService.getById(userB.getId()))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void update_canChangePassword() {
		var created = userService.create(new CreateUserRequest(
				"Ewa",
				"Test",
				"ewa@example.com",
				"oldpassword1",
				List.of(roleA.getId()),
				null
		));

		userService.update(created.id(), new UpdateUserRequest(
				"Ewa",
				"Test",
				"ewa@example.com",
				"newpassword1",
				List.of(roleA.getId()),
				null,
				true
		));

		User persisted = userRepository.findById(created.id()).orElseThrow();
		assertThat(passwordEncoder.matches("newpassword1", persisted.getPasswordHash())).isTrue();
		assertThat(passwordEncoder.matches("oldpassword1", persisted.getPasswordHash())).isFalse();
	}

	@Test
	void deactivate_setsInactive() {
		var created = userService.create(new CreateUserRequest(
				"Tomek",
				"Test",
				"tomek@example.com",
				"password123",
				List.of(roleA.getId()),
				null
		));

		userService.deactivate(created.id());

		User persisted = userRepository.findById(created.id()).orElseThrow();
		assertThat(persisted.isActive()).isFalse();
	}

	@Test
	void create_roleFromOtherOrganization_rejected() {
		Role roleB = roleRepository.findByOrganizationId(organizationB.getId()).getFirst();

		assertThatThrownBy(() -> userService.create(new CreateUserRequest(
				"Cross",
				"Tenant",
				"cross@example.com",
				"password123",
				List.of(roleB.getId()),
				null
		))).isInstanceOf(BusinessConflictException.class);
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

	private User saveUser(Organization organization, String email) {
		User user = new User();
		user.setOrganization(organization);
		user.setEmail(email);
		user.setFirstName("Test");
		user.setLastName("User");
		user.setPasswordHash(passwordEncoder.encode("password123"));
		return userRepository.saveAndFlush(user);
	}
}
