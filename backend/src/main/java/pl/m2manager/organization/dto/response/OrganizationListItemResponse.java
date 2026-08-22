package pl.m2manager.organization.dto.response;

import java.time.Instant;
import java.util.UUID;

public record OrganizationListItemResponse(
		UUID id,
		String name,
		String slug,
		String adminName,
		String adminEmail,
		boolean active,
		Instant createdAt
) {
}
