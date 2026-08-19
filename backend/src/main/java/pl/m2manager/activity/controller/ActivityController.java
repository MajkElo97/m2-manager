package pl.m2manager.activity.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.m2manager.activity.dto.request.CreateActivityRequest;
import pl.m2manager.activity.dto.request.UpdateActivityRequest;
import pl.m2manager.activity.dto.response.ActivityResponse;
import pl.m2manager.activity.entity.ActivityPlanningType;
import pl.m2manager.activity.service.ActivityService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

	private final ActivityService activityService;

	public ActivityController(ActivityService activityService) {
		this.activityService = activityService;
	}

	@GetMapping
	@PreAuthorize("@authorizationService.hasPermission('ACTIVITIES_VIEW')")
	public List<ActivityResponse> list(
			@RequestParam(required = false) String search,
			@RequestParam(required = false) String category,
			@RequestParam(required = false) ActivityPlanningType planningType,
			@RequestParam(required = false) Boolean active
	) {
		return activityService.getAll(search, category, planningType, active);
	}

	@GetMapping("/{id}")
	@PreAuthorize("@authorizationService.hasPermission('ACTIVITIES_VIEW')")
	public ActivityResponse getById(@PathVariable UUID id) {
		return activityService.getById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("@authorizationService.hasPermission('ACTIVITIES_CREATE')")
	public ActivityResponse create(@Valid @RequestBody CreateActivityRequest request) {
		return activityService.create(request);
	}

	@PutMapping("/{id}")
	@PreAuthorize("@authorizationService.hasPermission('ACTIVITIES_EDIT')")
	public ActivityResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateActivityRequest request) {
		return activityService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("@authorizationService.hasPermission('ACTIVITIES_DELETE')")
	public void deactivate(@PathVariable UUID id) {
		activityService.deactivate(id);
	}
}
