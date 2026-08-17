package pl.m2manager.role.dto;

import java.util.UUID;

public record RoleResponse(
		UUID id,
		String name,
		String description,
		boolean systemRole,
		boolean active
) {
}
