package pl.m2manager.organization.dto.response;

import java.util.UUID;

public record CreateOrganizationResponse(
		UUID id,
		String name,
		String slug,
		String adminEmail,
		String temporaryPassword
) {
}
