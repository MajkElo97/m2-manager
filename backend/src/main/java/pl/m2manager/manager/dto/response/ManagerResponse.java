package pl.m2manager.manager.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ManagerResponse(
		UUID id,
		String code,
		String name,
		String phone,
		String email,
		String address,
		String notes,
		boolean active,
		int supervisorCount,
		Instant createdAt,
		Instant updatedAt
) {
}
