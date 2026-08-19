package pl.m2manager.scope.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pl.m2manager.scope.entity.ScopePlanningType;
import pl.m2manager.scope.entity.ScopeStatus;

import java.util.UUID;

public record UpdateScopeRequest(
		@NotBlank @Size(max = 100) String code,
		@NotNull UUID buildingId,
		@NotNull UUID activityId,
		@NotNull ScopePlanningType planningType,
		@Min(0) Integer frequency,
		@Size(max = 255) String weekdays,
		String notes,
		@NotNull ScopeStatus status
) {
}
