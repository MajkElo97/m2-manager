package pl.m2manager.inventory.dto.response;

import pl.m2manager.inventory.entity.EquipmentCondition;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record EquipmentResponse(
		UUID id,
		String code,
		String name,
		String category,
		String manufacturer,
		String model,
		String serialNumber,
		Integer quantity,
		EquipmentCondition conditionStatus,
		String location,
		UUID employeeId,
		String employeeCode,
		String employeeName,
		LocalDate purchaseDate,
		BigDecimal purchaseValue,
		boolean active,
		String notes,
		Instant createdAt,
		Instant updatedAt
) {
}
