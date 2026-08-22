package pl.m2manager.organization.dto.response;

public record ResetAdminPasswordResponse(
		String adminEmail,
		String temporaryPassword
) {
}
