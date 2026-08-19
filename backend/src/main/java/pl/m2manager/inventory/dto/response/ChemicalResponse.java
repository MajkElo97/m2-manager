package pl.m2manager.inventory.dto.response;

import pl.m2manager.inventory.entity.ChemicalUnit;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ChemicalResponse(
		UUID id,
		String code,
		String name,
		String category,
		BigDecimal quantity,
		ChemicalUnit unit,
		BigDecimal minimumStock,
		boolean lowStock,
		String location,
		boolean active,
		String notes,
		Instant createdAt,
		Instant updatedAt
) {
}
