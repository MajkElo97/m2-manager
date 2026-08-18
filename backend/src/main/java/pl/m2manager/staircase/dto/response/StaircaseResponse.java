package pl.m2manager.staircase.dto.response;

import java.time.Instant;
import java.util.UUID;

public record StaircaseResponse(
		UUID id,
		UUID buildingId,
		String code,
		String designation,
		String intercomCode,
		boolean keyRequired,
		boolean elevator,
		Integer floors,
		String notes,
		Instant createdAt,
		Instant updatedAt
) {
}
