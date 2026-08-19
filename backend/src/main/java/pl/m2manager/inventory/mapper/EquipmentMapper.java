package pl.m2manager.inventory.mapper;

import org.springframework.stereotype.Component;
import pl.m2manager.employee.entity.Employee;
import pl.m2manager.inventory.dto.request.CreateEquipmentRequest;
import pl.m2manager.inventory.dto.request.UpdateEquipmentRequest;
import pl.m2manager.inventory.dto.response.EquipmentResponse;
import pl.m2manager.inventory.entity.Equipment;

import java.util.UUID;

@Component
public class EquipmentMapper {

	public EquipmentResponse toResponse(Equipment equipment, Employee employee) {
		return new EquipmentResponse(
				equipment.getId(),
				equipment.getCode(),
				equipment.getName(),
				equipment.getCategory(),
				equipment.getManufacturer(),
				equipment.getModel(),
				equipment.getSerialNumber(),
				equipment.getQuantity(),
				equipment.getConditionStatus(),
				equipment.getLocation(),
				equipment.getEmployeeId(),
				employee != null ? employee.getCode() : null,
				employee != null ? formatEmployeeName(employee) : null,
				equipment.getPurchaseDate(),
				equipment.getPurchaseValue(),
				equipment.isActive(),
				equipment.getNotes(),
				equipment.getCreatedAt(),
				equipment.getUpdatedAt()
		);
	}

	public void applyCreate(Equipment equipment, CreateEquipmentRequest request, UUID employeeId) {
		equipment.setCode(request.code());
		equipment.setName(request.name());
		equipment.setCategory(request.category());
		equipment.setManufacturer(request.manufacturer());
		equipment.setModel(request.model());
		equipment.setSerialNumber(request.serialNumber());
		equipment.setQuantity(request.quantity());
		equipment.setConditionStatus(request.conditionStatus());
		equipment.setLocation(request.location());
		equipment.setEmployeeId(employeeId);
		equipment.setPurchaseDate(request.purchaseDate());
		equipment.setPurchaseValue(request.purchaseValue());
		equipment.setActive(request.active());
		equipment.setNotes(request.notes());
	}

	public void applyUpdate(Equipment equipment, UpdateEquipmentRequest request, UUID employeeId) {
		equipment.setCode(request.code());
		equipment.setName(request.name());
		equipment.setCategory(request.category());
		equipment.setManufacturer(request.manufacturer());
		equipment.setModel(request.model());
		equipment.setSerialNumber(request.serialNumber());
		equipment.setQuantity(request.quantity());
		equipment.setConditionStatus(request.conditionStatus());
		equipment.setLocation(request.location());
		equipment.setEmployeeId(employeeId);
		equipment.setPurchaseDate(request.purchaseDate());
		equipment.setPurchaseValue(request.purchaseValue());
		equipment.setActive(request.active());
		equipment.setNotes(request.notes());
	}

	private String formatEmployeeName(Employee employee) {
		String lastName = employee.getLastName();
		if (lastName == null || lastName.isBlank()) {
			return employee.getFirstName();
		}
		return employee.getFirstName() + " " + lastName;
	}
}
