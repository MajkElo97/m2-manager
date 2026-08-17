package pl.m2manager.organization.dto.response;

import java.time.Instant;
import java.util.UUID;

public record OrganizationResponse(
		UUID id,
		String name,
		String nip,
		String email,
		String phone,
		boolean active,
		String timezone,
		Instant createdAt,
		Instant updatedAt
) {
}
