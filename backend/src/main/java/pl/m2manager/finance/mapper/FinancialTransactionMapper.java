package pl.m2manager.finance.mapper;

import org.springframework.stereotype.Component;
import pl.m2manager.building.entity.Building;
import pl.m2manager.employee.entity.Employee;
import pl.m2manager.finance.dto.response.FinancialTransactionResponse;
import pl.m2manager.finance.entity.FinancialCategory;
import pl.m2manager.finance.entity.FinancialTransaction;
import pl.m2manager.fleet.entity.Vehicle;
import pl.m2manager.inventory.entity.Chemical;
import pl.m2manager.inventory.entity.Equipment;

@Component
public class FinancialTransactionMapper {

	public FinancialTransactionResponse toResponse(
			FinancialTransaction transaction,
			FinancialCategory category,
			Building building,
			Employee employee,
			Vehicle vehicle,
			Equipment equipment,
			Chemical chemical
	) {
		return new FinancialTransactionResponse(
				transaction.getId(),
				transaction.getCode(),
				transaction.getTransactionDate(),
				transaction.getType(),
				transaction.getNetAmount(),
				transaction.getVatRate(),
				transaction.getVatAmount(),
				transaction.getGrossAmount(),
				transaction.getCategoryId(),
				category != null ? category.getCode() : null,
				category != null ? category.getName() : null,
				transaction.getContractorName(),
				transaction.getContractorNip(),
				transaction.getBuildingId(),
				building != null ? building.getCode() : null,
				building != null ? building.getName() : null,
				transaction.getEmployeeId(),
				employee != null ? employee.getCode() : null,
				employee != null ? formatEmployeeName(employee) : null,
				transaction.getVehicleId(),
				vehicle != null ? vehicle.getCode() : null,
				vehicle != null ? vehicle.getRegistrationNumber() : null,
				transaction.getEquipmentId(),
				equipment != null ? equipment.getCode() : null,
				equipment != null ? equipment.getName() : null,
				transaction.getChemicalId(),
				chemical != null ? chemical.getCode() : null,
				chemical != null ? chemical.getName() : null,
				transaction.getDescription(),
				transaction.getDocumentNumber(),
				transaction.getDueDate(),
				transaction.getPaymentDate(),
				transaction.getPaymentStatus(),
				transaction.getStatus(),
				transaction.getNotes(),
				transaction.getCreatedAt(),
				transaction.getUpdatedAt()
		);
	}

	private String formatEmployeeName(Employee employee) {
		String lastName = employee.getLastName();
		if (lastName == null || lastName.isBlank()) {
			return employee.getFirstName();
		}
		return employee.getFirstName() + " " + lastName;
	}
}
