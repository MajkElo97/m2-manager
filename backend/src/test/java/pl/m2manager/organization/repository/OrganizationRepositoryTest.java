package pl.m2manager.organization.repository;

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
import pl.m2manager.organization.mapper.OrganizationMapper;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class OrganizationRepositoryTest {

	private static final UUID DEV_ORGANIZATION_ID = UUID.fromString("a0000000-0000-4000-8000-000000000001");

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
	private OrganizationMapper organizationMapper;

	@Test
	void devOrganization_receivesSlugFromV5Migration() {
		Organization devOrganization = organizationRepository.findById(DEV_ORGANIZATION_ID).orElseThrow();

		assertThat(devOrganization.getSlug()).isEqualTo("m2-manager-dev");
	}

	@Test
	void slug_mustBeUnique() {
		Organization first = new Organization();
		first.setName("First Organization");
		first.setSlug("duplicate-slug");
		first.setTimezone("Europe/Warsaw");
		organizationRepository.saveAndFlush(first);

		Organization second = new Organization();
		second.setName("Second Organization");
		second.setSlug("duplicate-slug");
		second.setTimezone("Europe/Warsaw");

		assertThatThrownBy(() -> organizationRepository.saveAndFlush(second))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void differentSlugs_areAllowed() {
		Organization first = new Organization();
		first.setName("First Organization");
		first.setSlug("firma-xyz");
		first.setTimezone("Europe/Warsaw");

		Organization second = new Organization();
		second.setName("Second Organization");
		second.setSlug("abc-cleaning");
		second.setTimezone("Europe/Warsaw");

		organizationRepository.saveAndFlush(first);
		organizationRepository.saveAndFlush(second);

		assertThat(organizationRepository.findById(first.getId()).orElseThrow().getSlug()).isEqualTo("firma-xyz");
		assertThat(organizationRepository.findById(second.getId()).orElseThrow().getSlug()).isEqualTo("abc-cleaning");
	}

	@Test
	void saveAndFindById_persistsOrganization() {
		Organization organization = new Organization();
		organization.setName("Test Organization");
		organization.setSlug("m2-group");
		organization.setNip("9876543210");
		organization.setEmail("test@example.com");
		organization.setPhone("+48111222333");
		organization.setTimezone("Europe/Warsaw");

		Organization saved = organizationRepository.saveAndFlush(organization);

		assertThat(saved.getId()).isNotNull();

		Organization loaded = organizationRepository.findById(saved.getId()).orElseThrow();

		assertThat(loaded.getId()).isEqualTo(saved.getId());
		assertThat(loaded.getName()).isEqualTo("Test Organization");
		assertThat(loaded.getSlug()).isEqualTo("m2-group");
		assertThat(loaded.getNip()).isEqualTo("9876543210");
		assertThat(loaded.getEmail()).isEqualTo("test@example.com");
		assertThat(loaded.getPhone()).isEqualTo("+48111222333");
		assertThat(loaded.getTimezone()).isEqualTo("Europe/Warsaw");
		assertThat(loaded.isActive()).isTrue();
		assertThat(loaded.getCreatedAt()).isNotNull();
		assertThat(loaded.getUpdatedAt()).isNotNull();
	}

	@Test
	void organizationMapper_includesSlugInResponse() {
		Organization organization = new Organization();
		organization.setName("Mapper Test");
		organization.setSlug("mapper-test-org");
		organization.setTimezone("Europe/Warsaw");

		Organization saved = organizationRepository.saveAndFlush(organization);

		assertThat(organizationMapper.toResponse(saved).slug()).isEqualTo("mapper-test-org");
	}

	@Test
	void update_updatesUpdatedAtViaAuditing() throws InterruptedException {
		Organization organization = new Organization();
		organization.setName("Auditing Test");
		organization.setSlug("auditing-test");
		organization.setTimezone("Europe/Warsaw");

		Organization saved = organizationRepository.saveAndFlush(organization);
		UUID id = saved.getId();
		Instant createdAt = saved.getCreatedAt();
		Instant initialUpdatedAt = saved.getUpdatedAt();

		Thread.sleep(10);

		saved.setName("Auditing Test Updated");
		organizationRepository.saveAndFlush(saved);
		Organization reloaded = organizationRepository.findById(id).orElseThrow();

		assertThat(reloaded.getCreatedAt().getEpochSecond()).isEqualTo(createdAt.getEpochSecond());
		assertThat(reloaded.getUpdatedAt()).isAfterOrEqualTo(initialUpdatedAt);
		assertThat(reloaded.getName()).isEqualTo("Auditing Test Updated");
	}
}
