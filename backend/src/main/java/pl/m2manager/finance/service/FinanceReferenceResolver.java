package pl.m2manager.finance.service;

import org.springframework.stereotype.Component;
import pl.m2manager.building.entity.Building;
import pl.m2manager.building.repository.BuildingRepository;
import pl.m2manager.common.exception.BusinessConflictException;
import pl.m2manager.employee.entity.Employee;
import pl.m2manager.employee.repository.EmployeeRepository;
import pl.m2manager.finance.entity.FinancialCategory;
import pl.m2manager.finance.entity.TransactionType;
import pl.m2manager.finance.repository.FinancialCategoryRepository;
import pl.m2manager.fleet.entity.Vehicle;
import pl.m2manager.fleet.repository.VehicleRepository;
import pl.m2manager.inventory.entity.Chemical;
import pl.m2manager.inventory.entity.Equipment;
import pl.m2manager.inventory.repository.ChemicalRepository;
import pl.m2manager.inventory.repository.EquipmentRepository;

import java.util.UUID;

@Component
public class FinanceReferenceResolver {

	private final FinancialCategoryRepository categoryRepository;
	private final BuildingRepository buildingRepository;
	private final EmployeeRepository employeeRepository;
	private final VehicleRepository vehicleRepository;
	private final EquipmentRepository equipmentRepository;
	private final ChemicalRepository chemicalRepository;

	public FinanceReferenceResolver(
			FinancialCategoryRepository categoryRepository,
			BuildingRepository buildingRepository,
			EmployeeRepository employeeRepository,
			VehicleRepository vehicleRepository,
			EquipmentRepository equipmentRepository,
			ChemicalRepository chemicalRepository
	) {
		this.categoryRepository = categoryRepository;
		this.buildingRepository = buildingRepository;
		this.employeeRepository = employeeRepository;
		this.vehicleRepository = vehicleRepository;
		this.equipmentRepository = equipmentRepository;
		this.chemicalRepository = chemicalRepository;
	}

	public FinancialCategory resolveCategory(UUID organizationId, UUID categoryId, TransactionType transactionType) {
		FinancialCategory category = categoryRepository.findByIdAndOrganizationId(categoryId, organizationId)
				.orElseThrow(() -> new BusinessConflictException("Category not found in organization"));
		if (category.getType() != transactionType) {
			throw new BusinessConflictException("Category type does not match transaction type");
		}
		if (!category.isActive()) {
			throw new BusinessConflictException("Category is inactive");
		}
		return category;
	}

	public Building resolveBuilding(UUID organizationId, UUID buildingId) {
		if (buildingId == null) {
			return null;
		}
		return buildingRepository.findByIdAndOrganizationId(buildingId, organizationId)
				.orElseThrow(() -> new BusinessConflictException("Building not found in organization"));
	}

	public Employee resolveEmployee(UUID organizationId, UUID employeeId) {
		if (employeeId == null) {
			return null;
		}
		return employeeRepository.findByIdAndOrganizationId(employeeId, organizationId)
				.orElseThrow(() -> new BusinessConflictException("Employee not found in organization"));
	}

	public Vehicle resolveVehicle(UUID organizationId, UUID vehicleId) {
		if (vehicleId == null) {
			return null;
		}
		return vehicleRepository.findByIdAndOrganizationId(vehicleId, organizationId)
				.orElseThrow(() -> new BusinessConflictException("Vehicle not found in organization"));
	}

	public Equipment resolveEquipment(UUID organizationId, UUID equipmentId) {
		if (equipmentId == null) {
			return null;
		}
		return equipmentRepository.findByIdAndOrganizationId(equipmentId, organizationId)
				.orElseThrow(() -> new BusinessConflictException("Equipment not found in organization"));
	}

	public Chemical resolveChemical(UUID organizationId, UUID chemicalId) {
		if (chemicalId == null) {
			return null;
		}
		return chemicalRepository.findByIdAndOrganizationId(chemicalId, organizationId)
				.orElseThrow(() -> new BusinessConflictException("Chemical not found in organization"));
	}
}
