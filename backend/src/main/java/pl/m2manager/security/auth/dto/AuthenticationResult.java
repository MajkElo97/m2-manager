package pl.m2manager.security.auth.dto;

import java.util.UUID;

public record AuthenticationResult(
		UUID userId,
		UUID organizationId,
		String email
) {
}
