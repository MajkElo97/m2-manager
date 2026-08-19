package pl.m2manager.activity.mapper;

import org.springframework.stereotype.Component;
import pl.m2manager.activity.dto.request.CreateActivityRequest;
import pl.m2manager.activity.dto.request.UpdateActivityRequest;
import pl.m2manager.activity.dto.response.ActivityResponse;
import pl.m2manager.activity.entity.Activity;

@Component
public class ActivityMapper {

	public ActivityResponse toResponse(Activity activity) {
		return new ActivityResponse(
				activity.getId(),
				activity.getCode(),
				activity.getName(),
				activity.getCategory(),
				activity.getPlanningType(),
				activity.getDefaultPeriod(),
				activity.getDurationMinutes(),
				activity.getPriority(),
				activity.isActive(),
				activity.getCreatedAt(),
				activity.getUpdatedAt()
		);
	}

	public void applyCreate(Activity activity, CreateActivityRequest request) {
		activity.setCode(request.code());
		activity.setName(request.name());
		activity.setCategory(request.category());
		activity.setPlanningType(request.planningType());
		activity.setDefaultPeriod(request.defaultPeriod());
		activity.setDurationMinutes(request.durationMinutes());
		activity.setPriority(request.priority());
		activity.setActive(true);
	}

	public void applyUpdate(Activity activity, UpdateActivityRequest request) {
		activity.setCode(request.code());
		activity.setName(request.name());
		activity.setCategory(request.category());
		activity.setPlanningType(request.planningType());
		activity.setDefaultPeriod(request.defaultPeriod());
		activity.setDurationMinutes(request.durationMinutes());
		activity.setPriority(request.priority());
		activity.setActive(request.active());
	}
}
