package pl.m2manager.user.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserResponse(
		UUID id,
		String firstName,
		String lastName,
		String email,
		boolean active,
		List<UserRoleSummary> roles,
		UUID employeeId,
		String employeeCode,
		String employeeDisplayName,
		Instant createdAt,
		Instant updatedAt
) {
}
