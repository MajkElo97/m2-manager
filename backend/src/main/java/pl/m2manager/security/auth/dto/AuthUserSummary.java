package pl.m2manager.security.auth.dto;

import java.util.UUID;

public record AuthUserSummary(
		UUID id,
		String name,
		String email
) {
}
