package pl.m2manager.organization.repository;

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

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class OrganizationRepositoryTest {

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

	@Test
	void saveAndFindById_persistsOrganization() {
		Organization organization = new Organization();
		organization.setName("Test Organization");
		organization.setNip("9876543210");
		organization.setEmail("test@example.com");
		organization.setPhone("+48111222333");
		organization.setTimezone("Europe/Warsaw");

		Organization saved = organizationRepository.saveAndFlush(organization);

		assertThat(saved.getId()).isNotNull();

		Organization loaded = organizationRepository.findById(saved.getId()).orElseThrow();

		assertThat(loaded.getId()).isEqualTo(saved.getId());
		assertThat(loaded.getName()).isEqualTo("Test Organization");
		assertThat(loaded.getNip()).isEqualTo("9876543210");
		assertThat(loaded.getEmail()).isEqualTo("test@example.com");
		assertThat(loaded.getPhone()).isEqualTo("+48111222333");
		assertThat(loaded.getTimezone()).isEqualTo("Europe/Warsaw");
		assertThat(loaded.isActive()).isTrue();
		assertThat(loaded.getCreatedAt()).isNotNull();
		assertThat(loaded.getUpdatedAt()).isNotNull();
	}

	@Test
	void update_updatesUpdatedAtViaAuditing() throws InterruptedException {
		Organization organization = new Organization();
		organization.setName("Auditing Test");
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
