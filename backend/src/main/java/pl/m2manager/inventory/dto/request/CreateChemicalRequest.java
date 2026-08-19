package pl.m2manager.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pl.m2manager.inventory.entity.ChemicalUnit;

import java.math.BigDecimal;

public record CreateChemicalRequest(
		@NotBlank @Size(max = 50) String code,
		@NotBlank @Size(max = 255) String name,
		@NotBlank @Size(max = 100) String category,
		@NotNull BigDecimal quantity,
		@NotNull ChemicalUnit unit,
		BigDecimal minimumStock,
		@Size(max = 255) String location,
		boolean active,
		String notes
) {
}
