package pl.m2manager.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pl.m2manager.inventory.entity.EquipmentCondition;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateEquipmentRequest(
		@NotBlank @Size(max = 50) String code,
		@NotBlank @Size(max = 255) String name,
		@NotBlank @Size(max = 100) String category,
		@Size(max = 255) String manufacturer,
		@Size(max = 255) String model,
		@Size(max = 100) String serialNumber,
		@NotNull Integer quantity,
		@NotNull EquipmentCondition conditionStatus,
		@Size(max = 255) String location,
		UUID employeeId,
		LocalDate purchaseDate,
		BigDecimal purchaseValue,
		boolean active,
		String notes
) {
}
