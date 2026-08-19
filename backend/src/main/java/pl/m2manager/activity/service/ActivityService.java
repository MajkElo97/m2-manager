package pl.m2manager.activity.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.m2manager.activity.dto.request.CreateActivityRequest;
import pl.m2manager.activity.dto.request.UpdateActivityRequest;
import pl.m2manager.activity.dto.response.ActivityResponse;
import pl.m2manager.activity.entity.Activity;
import pl.m2manager.activity.entity.ActivityPlanningType;
import pl.m2manager.activity.mapper.ActivityMapper;
import pl.m2manager.activity.repository.ActivityRepository;
import pl.m2manager.common.exception.BusinessConflictException;
import pl.m2manager.common.exception.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ActivityService {

	private final ActivityRepository activityRepository;
	private final ActivityMapper activityMapper;

	public ActivityService(ActivityRepository activityRepository, ActivityMapper activityMapper) {
		this.activityRepository = activityRepository;
		this.activityMapper = activityMapper;
	}

	public List<ActivityResponse> getAll(String search, String category, ActivityPlanningType planningType, Boolean active) {
		return activityRepository.findAllByFilters(normalizeSearch(search), normalize(category), planningType, active).stream()
				.map(activityMapper::toResponse)
				.toList();
	}

	public ActivityResponse getById(UUID activityId) {
		return activityMapper.toResponse(requireActivity(activityId));
	}

	@Transactional
	public ActivityResponse create(CreateActivityRequest request) {
		assertUniqueCode(request.code(), null);

		Activity activity = new Activity();
		activityMapper.applyCreate(activity, request);

		try {
			return activityMapper.toResponse(activityRepository.saveAndFlush(activity));
		}
		catch (DataIntegrityViolationException ex) {
			throw new BusinessConflictException("Activity code already exists");
		}
	}

	@Transactional
	public ActivityResponse update(UUID activityId, UpdateActivityRequest request) {
		Activity activity = requireActivity(activityId);
		assertUniqueCode(request.code(), activityId);
		activityMapper.applyUpdate(activity, request);

		try {
			return activityMapper.toResponse(activityRepository.save(activity));
		}
		catch (DataIntegrityViolationException ex) {
			throw new BusinessConflictException("Activity code already exists");
		}
	}

	@Transactional
	public void deactivate(UUID activityId) {
		Activity activity = requireActivity(activityId);
		if (!activity.isActive()) {
			return;
		}
		activity.setActive(false);
		activityRepository.save(activity);
	}

	public Activity requireActiveActivity(UUID activityId) {
		Activity activity = requireActivity(activityId);
		if (!activity.isActive()) {
			throw new BusinessConflictException("Activity is not active");
		}
		return activity;
	}

	private Activity requireActivity(UUID activityId) {
		return activityRepository.findById(activityId)
				.orElseThrow(() -> new ResourceNotFoundException("Activity", activityId));
	}

	private void assertUniqueCode(String code, UUID excludeActivityId) {
		activityRepository.findByCode(code).ifPresent(existing -> {
			if (excludeActivityId == null || !existing.getId().equals(excludeActivityId)) {
				throw new BusinessConflictException("Activity code already exists");
			}
		});
	}

	private String normalizeSearch(String search) {
		if (search == null) {
			return null;
		}
		String trimmed = search.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private String normalize(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
