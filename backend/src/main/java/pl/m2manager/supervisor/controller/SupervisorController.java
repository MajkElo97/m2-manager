package pl.m2manager.supervisor.controller;

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
import pl.m2manager.supervisor.dto.request.CreateSupervisorRequest;
import pl.m2manager.supervisor.dto.request.UpdateSupervisorRequest;
import pl.m2manager.supervisor.dto.response.SupervisorResponse;
import pl.m2manager.supervisor.service.SupervisorService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/supervisors")
public class SupervisorController {

	private final SupervisorService supervisorService;

	public SupervisorController(SupervisorService supervisorService) {
		this.supervisorService = supervisorService;
	}

	@GetMapping
	@PreAuthorize("@authorizationService.hasPermission('SUPERVISORS_VIEW')")
	public List<SupervisorResponse> list(
			@RequestParam(required = false) String search,
			@RequestParam(required = false) UUID managerId,
			@RequestParam(required = false) Boolean active
	) {
		return supervisorService.getAll(managerId, active, search);
	}

	@GetMapping("/{id}")
	@PreAuthorize("@authorizationService.hasPermission('SUPERVISORS_VIEW')")
	public SupervisorResponse getById(@PathVariable UUID id) {
		return supervisorService.getById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("@authorizationService.hasPermission('SUPERVISORS_CREATE')")
	public SupervisorResponse create(@Valid @RequestBody CreateSupervisorRequest request) {
		return supervisorService.create(request);
	}

	@PutMapping("/{id}")
	@PreAuthorize("@authorizationService.hasPermission('SUPERVISORS_EDIT')")
	public SupervisorResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateSupervisorRequest request) {
		return supervisorService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("@authorizationService.hasPermission('SUPERVISORS_DELETE')")
	public void deactivate(@PathVariable UUID id) {
		supervisorService.deactivate(id);
	}
}
