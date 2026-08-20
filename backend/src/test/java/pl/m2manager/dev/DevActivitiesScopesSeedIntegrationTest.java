package pl.m2manager.dev;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.m2manager.activity.repository.ActivityRepository;
import pl.m2manager.scope.repository.ActivityScopeRepository;
import pl.m2manager.tenant.TenantContext;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class DevActivitiesScopesSeedIntegrationTest {

	static final UUID DEV_ORGANIZATION_ID = UUID.fromString("a0000000-0000-4000-8000-000000000001");

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
	private ActivityRepository activityRepository;

	@Autowired
	private ActivityScopeRepository scopeRepository;

	@MockitoBean
	private TenantContext tenantContext;

	@Test
	void devSeed_containsTwentyThreeActivities() {
		assertThat(activityRepository.findAllVisibleByOrganizationIdAndFilters(
				DEV_ORGANIZATION_ID, null, null, null, null
		)).hasSize(23);
		assertThat(activityRepository.findAll()).hasSize(23);
	}

	@Test
	void devSeed_containsThirtySixScopes() {
		when(tenantContext.getCurrentOrganizationId()).thenReturn(DEV_ORGANIZATION_ID);
		assertThat(scopeRepository.findAllByOrganizationIdAndFilters(
				DEV_ORGANIZATION_ID, null, null, null, null, null
		)).hasSize(36);
	}

	@Test
	void devSeed_scopeRelationsAreCorrect() {
		when(tenantContext.getCurrentOrganizationId()).thenReturn(DEV_ORGANIZATION_ID);

		var zp0001 = scopeRepository.findByOrganizationIdAndCode(DEV_ORGANIZATION_ID, "ZP0001").orElseThrow();
		var cz0001 = activityRepository.findByCode("CZ0001").orElseThrow();

		assertThat(zp0001.getActivityId()).isEqualTo(cz0001.getId());
		assertThat(zp0001.getWeekdays()).isEqualTo("Wtorek");
		assertThat(zp0001.getPlanningType().name()).isEqualTo("WEEKLY");
	}

	@Test
	void devSeed_cz0020HasNullDuration() {
		var cz0020 = activityRepository.findByCode("CZ0020").orElseThrow();
		assertThat(cz0020.getDefaultPeriod()).isEqualTo("15");
		assertThat(cz0020.getDurationMinutes()).isNull();
		assertThat(cz0020.getPriority().name()).isEqualTo("HIGH");
		assertThat(cz0020.getOrganizationId()).isNull();
	}
}
