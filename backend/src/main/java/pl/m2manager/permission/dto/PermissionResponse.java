package pl.m2manager.permission.dto;

public record PermissionResponse(
		String code,
		String module,
		String action,
		String description
) {
}
