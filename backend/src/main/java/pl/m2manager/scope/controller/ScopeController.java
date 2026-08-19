package pl.m2manager.scope.controller;

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
import pl.m2manager.scope.dto.request.CreateScopeRequest;
import pl.m2manager.scope.dto.request.UpdateScopeRequest;
import pl.m2manager.scope.dto.response.ScopeResponse;
import pl.m2manager.scope.entity.ScopePlanningType;
import pl.m2manager.scope.entity.ScopeStatus;
import pl.m2manager.scope.service.ScopeService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/scopes")
public class ScopeController {

	private final ScopeService scopeService;

	public ScopeController(ScopeService scopeService) {
		this.scopeService = scopeService;
	}

	@GetMapping
	@PreAuthorize("@authorizationService.canListScopes(#buildingId)")
	public List<ScopeResponse> list(
			@RequestParam(required = false) UUID buildingId,
			@RequestParam(required = false) UUID activityId,
			@RequestParam(required = false) ScopePlanningType planningType,
			@RequestParam(required = false) ScopeStatus status,
			@RequestParam(required = false) String search
	) {
		return scopeService.getAll(buildingId, activityId, planningType, status, search);
	}

	@GetMapping("/{id}")
	@PreAuthorize("@authorizationService.hasPermission('SCOPES_VIEW')")
	public ScopeResponse getById(@PathVariable UUID id) {
		return scopeService.getById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("@authorizationService.hasPermission('SCOPES_CREATE')")
	public ScopeResponse create(@Valid @RequestBody CreateScopeRequest request) {
		return scopeService.create(request);
	}

	@PutMapping("/{id}")
	@PreAuthorize("@authorizationService.hasPermission('SCOPES_EDIT')")
	public ScopeResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateScopeRequest request) {
		return scopeService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("@authorizationService.hasPermission('SCOPES_DELETE')")
	public void deactivate(@PathVariable UUID id) {
		scopeService.deactivate(id);
	}
}
