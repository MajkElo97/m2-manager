package pl.m2manager.security.jwt;

import java.util.UUID;

public record JwtAuthenticatedPrincipal(
		UUID userId,
		UUID organizationId,
		String email
) {
}
