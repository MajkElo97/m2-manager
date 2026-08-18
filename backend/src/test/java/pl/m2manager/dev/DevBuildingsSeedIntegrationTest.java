package pl.m2manager.dev;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.m2manager.building.entity.Building;
import pl.m2manager.building.entity.BuildingStatus;
import pl.m2manager.building.repository.BuildingRepository;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class DevBuildingsSeedIntegrationTest {

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
	private BuildingRepository buildingRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void devSeed_containsNineBuildings() {
		assertThat(buildingRepository.findAllByOrganizationIdAndFilters(DEV_ORGANIZATION_ID, null, null)).hasSize(9);
	}

	@Test
	void devSeed_datesMatchBusinessFormat() {
		assertServiceStartDate("PUSTA64", LocalDate.of(2025, 1, 5));
		assertServiceStartDate("SKLODOWSKA37", LocalDate.of(2025, 1, 5));
		assertServiceStartDate("KASPRZAKA6", LocalDate.of(2025, 1, 9));
		assertServiceStartDate("3KAMIENICE", LocalDate.of(2026, 1, 8));
		assertServiceStartDate("CIESZKOWSKIEGO14", LocalDate.of(2026, 1, 1));
		assertServiceStartDate("NOWYMANHATTAN", LocalDate.of(2026, 1, 8));
		assertServiceStartDate("APARTAMENTYPRZYJEZIORZE", LocalDate.of(2026, 1, 1));

		Building pusta62 = requireBuilding("PUSTA62");
		assertThat(pusta62.getServiceStartDate()).isNull();
		assertThat(pusta62.getStatus()).isEqualTo(BuildingStatus.INACTIVE);

		Building szulc3 = requireBuilding("SZULC3");
		assertThat(szulc3.getContractSignedAt()).isEqualTo(LocalDate.of(2026, 7, 23));
		assertThat(szulc3.getServiceStartDate()).isEqualTo(LocalDate.of(2026, 10, 1));
		assertThat(szulc3.getStatus()).isEqualTo(BuildingStatus.INACTIVE);
	}

	@Test
	void devSeed_isIdempotent() {
		int countBefore = buildingRepository.findAllByOrganizationIdAndFilters(DEV_ORGANIZATION_ID, null, null).size();

		jdbcTemplate.execute("""
				INSERT INTO buildings (
				    id,
				    organization_id,
				    code,
				    name,
				    address,
				    city,
				    notice_period_months,
				    status
				)
				VALUES (
				    'd0000000-0000-4000-8000-000000000001',
				    'a0000000-0000-4000-8000-000000000001',
				    'PUSTA64',
				    'Pusta 64',
				    'ul. Pusta 64',
				    'Sosnowiec',
				    3,
				    'ACTIVE'
				)
				ON CONFLICT (id) DO NOTHING
				""");

		int countAfter = buildingRepository.findAllByOrganizationIdAndFilters(DEV_ORGANIZATION_ID, null, null).size();
		assertThat(countAfter).isEqualTo(countBefore);
		assertThat(countBefore).isEqualTo(9);
	}

	private void assertServiceStartDate(String code, LocalDate expectedDate) {
		Building building = requireBuilding(code);
		assertThat(building.getServiceStartDate()).isEqualTo(expectedDate);
		assertThat(building.getStatus()).isEqualTo(BuildingStatus.ACTIVE);
	}

	private Building requireBuilding(String code) {
		return buildingRepository.findByOrganizationIdAndCode(DEV_ORGANIZATION_ID, code).orElseThrow();
	}
}
