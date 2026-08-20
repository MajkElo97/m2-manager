package pl.m2manager.finance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pl.m2manager.finance.entity.TransactionType;

public record UpdateFinancialCategoryRequest(
		@NotBlank @Size(max = 50) String code,
		@NotBlank @Size(max = 255) String name,
		@NotNull TransactionType type,
		boolean active
) {
}
