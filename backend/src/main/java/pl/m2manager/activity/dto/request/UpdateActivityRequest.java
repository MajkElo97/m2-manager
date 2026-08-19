package pl.m2manager.activity.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pl.m2manager.activity.entity.ActivityPlanningType;
import pl.m2manager.activity.entity.ActivityPriority;

public record UpdateActivityRequest(
		@NotBlank @Size(max = 100) String code,
		@NotBlank @Size(max = 255) String name,
		@NotBlank @Size(max = 100) String category,
		@NotNull ActivityPlanningType planningType,
		@Size(max = 50) String defaultPeriod,
		@Min(0) Integer durationMinutes,
		@NotNull ActivityPriority priority,
		@NotNull Boolean active
) {
}
