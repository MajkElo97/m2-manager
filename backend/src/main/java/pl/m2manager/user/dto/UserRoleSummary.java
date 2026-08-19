package pl.m2manager.user.dto;

import java.util.UUID;

public record UserRoleSummary(
		UUID id,
		String name,
		boolean systemRole
) {
}
