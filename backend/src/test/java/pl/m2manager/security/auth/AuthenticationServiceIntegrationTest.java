package pl.m2manager.security.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.m2manager.organization.entity.Organization;
import pl.m2manager.organization.repository.OrganizationRepository;
import pl.m2manager.security.auth.dto.AuthenticationResult;
import pl.m2manager.user.entity.User;
import pl.m2manager.user.repository.UserRepository;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class AuthenticationServiceIntegrationTest {

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
	private AuthenticationService authenticationService;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private Organization organizationA;
	private Organization organizationB;
	private User userA;
	private User userB;
	private String slugA;
	private String slugB;

	@BeforeEach
	void setUp() {
		slugA = "org-a-" + UUID.randomUUID().toString().substring(0, 8);
		slugB = "org-b-" + UUID.randomUUID().toString().substring(0, 8);
		organizationA = saveOrganization("Organization A", slugA);
		organizationB = saveOrganization("Organization B", slugB);
		userA = saveUser(organizationA, "john@example.com", "passwordA");
		userB = saveUser(organizationB, "john@example.com", "passwordB");
	}

	@Test
	void authenticate_orgAWithCorrectPasswordAuthenticatesOrganizationA() {
		AuthenticationResult result = authenticationService.authenticate(slugA, "john@example.com", "passwordA");

		assertThat(result.userId()).isEqualTo(userA.getId());
		assertThat(result.organizationId()).isEqualTo(organizationA.getId());
		assertThat(result.email()).isEqualTo("john@example.com");
	}

	@Test
	void authenticate_orgBWithCorrectPasswordAuthenticatesOrganizationB() {
		AuthenticationResult result = authenticationService.authenticate(slugB, "john@example.com", "passwordB");

		assertThat(result.userId()).isEqualTo(userB.getId());
		assertThat(result.organizationId()).isEqualTo(organizationB.getId());
		assertThat(result.email()).isEqualTo("john@example.com");
	}

	@Test
	void authenticate_orgAWithOrganizationBPasswordFails() {
		assertThatThrownBy(() -> authenticationService.authenticate(slugA, "john@example.com", "passwordB"))
				.isInstanceOf(BadCredentialsException.class)
				.hasMessage(M2UserDetailsService.INVALID_CREDENTIALS_MESSAGE);
	}

	@Test
	void authenticate_orgBWithOrganizationAPasswordFails() {
		assertThatThrownBy(() -> authenticationService.authenticate(slugB, "john@example.com", "passwordA"))
				.isInstanceOf(BadCredentialsException.class)
				.hasMessage(M2UserDetailsService.INVALID_CREDENTIALS_MESSAGE);
	}

	private Organization saveOrganization(String name, String slug) {
		Organization organization = new Organization();
		organization.setName(name);
		organization.setSlug(slug);
		organization.setTimezone("Europe/Warsaw");
		return organizationRepository.saveAndFlush(organization);
	}

	private User saveUser(Organization organization, String email, String rawPassword) {
		User user = new User();
		user.setOrganization(organization);
		user.setEmail(email);
		user.setPasswordHash(passwordEncoder.encode(rawPassword));
		return userRepository.saveAndFlush(user);
	}
}
