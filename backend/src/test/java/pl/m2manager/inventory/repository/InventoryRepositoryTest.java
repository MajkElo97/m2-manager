package pl.m2manager.inventory.repository;

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
import pl.m2manager.inventory.entity.Chemical;
import pl.m2manager.inventory.entity.ChemicalUnit;
import pl.m2manager.inventory.entity.Equipment;
import pl.m2manager.inventory.entity.EquipmentCondition;
import pl.m2manager.organization.entity.Organization;
import pl.m2manager.organization.repository.OrganizationRepository;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class InventoryRepositoryTest {

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
	private EquipmentRepository equipmentRepository;

	@Autowired
	private ChemicalRepository chemicalRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void flywayV29V30_createsInventoryTables() {
		assertThat(jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'equipment'",
				Integer.class
		)).isEqualTo(1);
		assertThat(jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'chemicals'",
				Integer.class
		)).isEqualTo(1);
	}

	@Test
	void equipment_findAllByFilters_searchByName() {
		Organization organization = saveOrganization("Org Equipment Search");
		saveEquipment(organization.getId(), "EQ0101", "Unique Name");
		saveEquipment(organization.getId(), "EQ0102", "Other Name");

		assertThat(equipmentRepository.findAllByOrganizationIdAndFilters(organization.getId(), "unique", null, null, null, null))
				.extracting(Equipment::getCode)
				.containsExactly("EQ0101");
	}

	@Test
	void chemicals_findAllByFilters_lowStockFilter() {
		Organization organization = saveOrganization("Org Chemical Low Stock");
		saveChemical(organization.getId(), "CH0101", new BigDecimal("2.000"), new BigDecimal("5.000"));
		saveChemical(organization.getId(), "CH0102", new BigDecimal("10.000"), new BigDecimal("5.000"));

		assertThat(chemicalRepository.findAllByOrganizationIdAndFilters(organization.getId(), null, null, null, true))
				.extracting(Chemical::getCode)
				.containsExactly("CH0101");
	}

	private Organization saveOrganization(String name) {
		Organization organization = new Organization();
		organization.setName(name);
		organization.setSlug(name.toLowerCase().replace(' ', '-') + "-" + UUID.randomUUID().toString().substring(0, 8));
		organization.setTimezone("Europe/Warsaw");
		return organizationRepository.saveAndFlush(organization);
	}

	private Equipment saveEquipment(UUID organizationId, String code, String name) {
		Equipment equipment = new Equipment();
		equipment.setOrganizationId(organizationId);
		equipment.setCode(code);
		equipment.setName(name);
		equipment.setCategory("Category");
		equipment.setQuantity(1);
		equipment.setConditionStatus(EquipmentCondition.GOOD);
		equipment.setActive(true);
		return equipmentRepository.saveAndFlush(equipment);
	}

	private Chemical saveChemical(UUID organizationId, String code, BigDecimal quantity, BigDecimal minimumStock) {
		Chemical chemical = new Chemical();
		chemical.setOrganizationId(organizationId);
		chemical.setCode(code);
		chemical.setName("Name");
		chemical.setCategory("Category");
		chemical.setQuantity(quantity);
		chemical.setUnit(ChemicalUnit.LITER);
		chemical.setMinimumStock(minimumStock);
		chemical.setActive(true);
		return chemicalRepository.saveAndFlush(chemical);
	}
}
