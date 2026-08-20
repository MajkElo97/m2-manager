package pl.m2manager.finance.dto.response;

import pl.m2manager.finance.entity.TransactionType;

import java.time.Instant;
import java.util.UUID;

public record FinancialCategoryResponse(
		UUID id,
		String code,
		String name,
		TransactionType type,
		boolean active,
		Instant createdAt,
		Instant updatedAt
) {
}
