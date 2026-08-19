package pl.m2manager.contact.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ContactResponse(
		UUID id,
		UUID buildingId,
		String buildingCode,
		String buildingName,
		String firstName,
		String lastName,
		String functionTitle,
		String phone,
		String email,
		String notes,
		boolean active,
		Instant createdAt,
		Instant updatedAt
) {
}
