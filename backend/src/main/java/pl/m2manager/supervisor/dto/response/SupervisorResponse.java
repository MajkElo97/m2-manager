package pl.m2manager.supervisor.dto.response;

import java.time.Instant;
import java.util.UUID;

public record SupervisorResponse(
		UUID id,
		UUID managerId,
		String managerCode,
		String managerName,
		String code,
		String firstName,
		String lastName,
		String phone,
		String email,
		String notes,
		boolean active,
		Instant createdAt,
		Instant updatedAt
) {
}
