package pl.m2manager.role.dto;

import jakarta.validation.constraints.Size;

public record UpdateRoleRequest(
		@Size(max = 100) String name,
		String description,
		Boolean active
) {
}
