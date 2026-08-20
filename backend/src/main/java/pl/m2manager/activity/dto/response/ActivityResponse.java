package pl.m2manager.activity.dto.response;

import pl.m2manager.activity.entity.ActivityPlanningType;
import pl.m2manager.activity.entity.ActivityPriority;

import java.time.Instant;
import java.util.UUID;

public record ActivityResponse(
		UUID id,
		String code,
		String name,
		String category,
		ActivityPlanningType planningType,
		String defaultPeriod,
		Integer durationMinutes,
		ActivityPriority priority,
		boolean active,
		boolean system,
		boolean manageable,
		Instant createdAt,
		Instant updatedAt
) {
}
