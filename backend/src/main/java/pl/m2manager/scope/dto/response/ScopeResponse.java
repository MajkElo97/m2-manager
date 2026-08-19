package pl.m2manager.scope.dto.response;

import pl.m2manager.scope.entity.ScopePlanningType;
import pl.m2manager.scope.entity.ScopeStatus;

import java.time.Instant;
import java.util.UUID;

public record ScopeResponse(
		UUID id,
		String code,
		UUID buildingId,
		UUID activityId,
		ScopePlanningType planningType,
		Integer frequency,
		String weekdays,
		String notes,
		ScopeStatus status,
		Instant createdAt,
		Instant updatedAt
) {
}
