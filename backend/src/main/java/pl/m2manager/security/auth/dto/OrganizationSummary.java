package pl.m2manager.security.auth.dto;

import java.util.UUID;

public record OrganizationSummary(
		UUID id,
		String name,
		String slug
) {
}
