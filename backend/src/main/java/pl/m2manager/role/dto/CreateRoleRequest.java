package pl.m2manager.role.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRoleRequest(
		@NotBlank @Size(max = 100) String name,
		String description
) {
}
