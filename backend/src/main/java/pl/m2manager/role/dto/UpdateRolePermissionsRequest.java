package pl.m2manager.role.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateRolePermissionsRequest(
		@NotNull List<String> permissionCodes
) {
}
