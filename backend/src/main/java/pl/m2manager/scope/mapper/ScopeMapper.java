package pl.m2manager.scope.mapper;

import org.springframework.stereotype.Component;
import pl.m2manager.scope.dto.request.CreateScopeRequest;
import pl.m2manager.scope.dto.request.UpdateScopeRequest;
import pl.m2manager.scope.dto.response.ScopeResponse;
import pl.m2manager.scope.entity.ActivityScope;
import pl.m2manager.scope.entity.ScopeStatus;

@Component
public class ScopeMapper {

	public ScopeResponse toResponse(ActivityScope scope) {
		return new ScopeResponse(
				scope.getId(),
				scope.getCode(),
				scope.getBuildingId(),
				scope.getActivityId(),
				scope.getPlanningType(),
				scope.getFrequency(),
				scope.getWeekdays(),
				scope.getNotes(),
				scope.getStatus(),
				scope.getCreatedAt(),
				scope.getUpdatedAt()
		);
	}

	public void applyCreate(ActivityScope scope, CreateScopeRequest request) {
		scope.setCode(request.code());
		scope.setBuildingId(request.buildingId());
		scope.setActivityId(request.activityId());
		scope.setPlanningType(request.planningType());
		scope.setFrequency(request.frequency());
		scope.setWeekdays(request.weekdays());
		scope.setNotes(request.notes());
		scope.setStatus(ScopeStatus.ACTIVE);
	}

	public void applyUpdate(ActivityScope scope, UpdateScopeRequest request) {
		scope.setCode(request.code());
		scope.setBuildingId(request.buildingId());
		scope.setActivityId(request.activityId());
		scope.setPlanningType(request.planningType());
		scope.setFrequency(request.frequency());
		scope.setWeekdays(request.weekdays());
		scope.setNotes(request.notes());
		scope.setStatus(request.status());
	}
}
