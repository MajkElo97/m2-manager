package pl.m2manager.security.jwt;

import org.junit.jupiter.api.BeforeEach;
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
import pl.m2manager.user.entity.User;
import pl.m2manager.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class RefreshTokenRepositoryTest {

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
	private RefreshTokenRepository refreshTokenRepository;

	@Autowired
	private RefreshTokenHasher refreshTokenHasher;

	@Autowired
	private RefreshTokenService refreshTokenService;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private Organization organizationA;
	private Organization organizationB;
	private User userA;

	@BeforeEach
	void setUp() {
		organizationA = saveOrganization("Organization A", "org-a-" + UUID.randomUUID().toString().substring(0, 8));
		organizationB = saveOrganization("Organization B", "org-b-" + UUID.randomUUID().toString().substring(0, 8));
		userA = saveUser(organizationA, "user-a@example.com");
	}

	@Test
	void flywayV6_createsRefreshTokensTable() {
		long beforeCount = refreshTokenRepository.count();
		IssuedRefreshToken issued = refreshTokenService.issueNewFamily(userA.getId(), organizationA.getId());

		assertThat(refreshTokenRepository.count()).isEqualTo(beforeCount + 1);
		assertThat(refreshTokenRepository.findByTokenHash(refreshTokenService.hashRawToken(issued.rawToken()))).isPresent();
	}

	@Test
	void tokenHash_isUnique() {
		persistToken(userA.getId(), organizationA.getId(), "hash-one", UUID.randomUUID());

		RefreshToken duplicate = new RefreshToken();
		duplicate.setUserId(userA.getId());
		duplicate.setOrganizationId(organizationA.getId());
		duplicate.setTokenHash("hash-one");
		duplicate.setFamilyId(UUID.randomUUID());
		duplicate.setCreatedAt(Instant.now());
		duplicate.setExpiresAt(Instant.now().plusSeconds(3600));

		assertThatThrownBy(() -> refreshTokenRepository.saveAndFlush(duplicate))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void crossTenantUserOrganizationCombination_isRejected() {
		RefreshToken crossTenant = new RefreshToken();
		crossTenant.setUserId(userA.getId());
		crossTenant.setOrganizationId(organizationB.getId());
		crossTenant.setTokenHash(refreshTokenHasher.hash("cross-tenant-token"));
		crossTenant.setFamilyId(UUID.randomUUID());
		crossTenant.setCreatedAt(Instant.now());
		crossTenant.setExpiresAt(Instant.now().plusSeconds(3600));

		assertThatThrownBy(() -> refreshTokenRepository.saveAndFlush(crossTenant))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	private RefreshToken persistToken(UUID userId, UUID organizationId, String tokenHash, UUID familyId) {
		RefreshToken token = new RefreshToken();
		token.setUserId(userId);
		token.setOrganizationId(organizationId);
		token.setTokenHash(tokenHash);
		token.setFamilyId(familyId);
		token.setCreatedAt(Instant.now());
		token.setExpiresAt(Instant.now().plusSeconds(3600));
		return refreshTokenRepository.saveAndFlush(token);
	}

	private Organization saveOrganization(String name, String slug) {
		Organization organization = new Organization();
		organization.setName(name);
		organization.setSlug(slug);
		organization.setTimezone("Europe/Warsaw");
		return organizationRepository.saveAndFlush(organization);
	}

	private User saveUser(Organization organization, String email) {
		User user = new User();
		user.setOrganization(organization);
		user.setEmail(email);
		user.setPasswordHash(passwordEncoder.encode("password"));
		return userRepository.saveAndFlush(user);
	}
}
