package pl.m2manager.inventory.service;

import org.junit.jupiter.api.BeforeEach;
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
import pl.m2manager.common.exception.ResourceNotFoundException;
import pl.m2manager.inventory.dto.request.CreateChemicalRequest;
import pl.m2manager.inventory.dto.request.CreateEquipmentRequest;
import pl.m2manager.inventory.entity.Chemical;
import pl.m2manager.inventory.entity.ChemicalUnit;
import pl.m2manager.inventory.entity.Equipment;
import pl.m2manager.inventory.entity.EquipmentCondition;
import pl.m2manager.inventory.repository.ChemicalRepository;
import pl.m2manager.inventory.repository.EquipmentRepository;
import pl.m2manager.organization.entity.Organization;
import pl.m2manager.organization.repository.OrganizationRepository;
import pl.m2manager.tenant.TenantContext;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class InventoryServiceTest {

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
	private EquipmentService equipmentService;

	@Autowired
	private ChemicalService chemicalService;

	@Autowired
	private EquipmentRepository equipmentRepository;

	@Autowired
	private ChemicalRepository chemicalRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	private Organization organizationA;
	private Organization organizationB;

	@BeforeEach
	void setUp() {
		organizationA = saveOrganization("Org Inventory Service A");
		organizationB = saveOrganization("Org Inventory Service B");
		when(tenantContext.getCurrentOrganizationId()).thenReturn(organizationA.getId());
	}

	@Test
	void equipmentCreate_usesCurrentTenantContext() {
		var created = equipmentService.create(new CreateEquipmentRequest(
				"EQ0001",
				"Demo equipment",
				"Category",
				null,
				null,
				null,
				1,
				EquipmentCondition.GOOD,
				null,
				null,
				null,
				null,
				true,
				null
		));

		Equipment persisted = equipmentRepository.findByIdAndOrganizationId(created.id(), organizationA.getId()).orElseThrow();
		assertThat(persisted.getOrganizationId()).isEqualTo(organizationA.getId());
		assertThat(persisted.isActive()).isTrue();
	}

	@Test
	void equipmentGetById_tenantIsolation() {
		Equipment equipmentB = saveEquipment(organizationB.getId(), "EQ0002");

		assertThatThrownBy(() -> equipmentService.getById(equipmentB.getId()))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void equipmentDeactivate_setsActiveFalse() {
		Equipment equipment = saveEquipment(organizationA.getId(), "EQ0003");

		equipmentService.deactivate(equipment.getId());

		Equipment reloaded = equipmentRepository.findById(equipment.getId()).orElseThrow();
		assertThat(reloaded.isActive()).isFalse();
	}

	@Test
	void chemicalCreate_usesCurrentTenantContext() {
		var created = chemicalService.create(new CreateChemicalRequest(
				"CH0001",
				"Demo chemical",
				"Category",
				new BigDecimal("10.000"),
				ChemicalUnit.LITER,
				new BigDecimal("5.000"),
				null,
				true,
				null
		));

		Chemical persisted = chemicalRepository.findByIdAndOrganizationId(created.id(), organizationA.getId()).orElseThrow();
		assertThat(persisted.getOrganizationId()).isEqualTo(organizationA.getId());
	}

	@Test
	void chemicalGetById_tenantIsolation() {
		Chemical chemicalB = saveChemical(organizationB.getId(), "CH0002");

		assertThatThrownBy(() -> chemicalService.getById(chemicalB.getId()))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void chemicalLowStock_computedInResponse() {
		var created = chemicalService.create(new CreateChemicalRequest(
				"CH0003",
				"Low stock",
				"Category",
				new BigDecimal("2.000"),
				ChemicalUnit.LITER,
				new BigDecimal("5.000"),
				null,
				true,
				null
		));

		assertThat(created.lowStock()).isTrue();
	}

	@Test
	void chemicalDeactivate_setsActiveFalse() {
		Chemical chemical = saveChemical(organizationA.getId(), "CH0004");

		chemicalService.deactivate(chemical.getId());

		Chemical reloaded = chemicalRepository.findById(chemical.getId()).orElseThrow();
		assertThat(reloaded.isActive()).isFalse();
	}

	private Organization saveOrganization(String name) {
		Organization organization = new Organization();
		organization.setName(name);
		organization.setSlug(name.toLowerCase().replace(' ', '-') + "-" + UUID.randomUUID().toString().substring(0, 8));
		organization.setTimezone("Europe/Warsaw");
		return organizationRepository.saveAndFlush(organization);
	}

	private Equipment saveEquipment(UUID organizationId, String code) {
		Equipment equipment = new Equipment();
		equipment.setOrganizationId(organizationId);
		equipment.setCode(code);
		equipment.setName("Name");
		equipment.setCategory("Category");
		equipment.setQuantity(1);
		equipment.setConditionStatus(EquipmentCondition.GOOD);
		equipment.setActive(true);
		return equipmentRepository.saveAndFlush(equipment);
	}

	private Chemical saveChemical(UUID organizationId, String code) {
		Chemical chemical = new Chemical();
		chemical.setOrganizationId(organizationId);
		chemical.setCode(code);
		chemical.setName("Name");
		chemical.setCategory("Category");
		chemical.setQuantity(new BigDecimal("10.000"));
		chemical.setUnit(ChemicalUnit.LITER);
		chemical.setActive(true);
		return chemicalRepository.saveAndFlush(chemical);
	}
}
