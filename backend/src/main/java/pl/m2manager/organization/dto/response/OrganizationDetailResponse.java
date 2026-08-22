package pl.m2manager.organization.dto.response;

import java.time.Instant;
import java.util.UUID;

public record OrganizationDetailResponse(
		UUID id,
		String name,
		String slug,
		String adminName,
		String adminEmail,
		UUID adminUserId,
		boolean active,
		Instant createdAt,
		Instant updatedAt
) {
}
