package pl.m2manager.manager.controller;

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
import pl.m2manager.manager.dto.request.CreateManagerRequest;
import pl.m2manager.manager.dto.request.UpdateManagerRequest;
import pl.m2manager.manager.dto.response.ManagerResponse;
import pl.m2manager.manager.service.ManagerService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/managers")
public class ManagerController {

	private final ManagerService managerService;

	public ManagerController(ManagerService managerService) {
		this.managerService = managerService;
	}

	@GetMapping
	@PreAuthorize("@authorizationService.hasPermission('MANAGERS_VIEW')")
	public List<ManagerResponse> list(
			@RequestParam(required = false) String search,
			@RequestParam(required = false) Boolean active
	) {
		return managerService.getAll(search, active);
	}

	@GetMapping("/{id}")
	@PreAuthorize("@authorizationService.hasPermission('MANAGERS_VIEW')")
	public ManagerResponse getById(@PathVariable UUID id) {
		return managerService.getById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("@authorizationService.hasPermission('MANAGERS_CREATE')")
	public ManagerResponse create(@Valid @RequestBody CreateManagerRequest request) {
		return managerService.create(request);
	}

	@PutMapping("/{id}")
	@PreAuthorize("@authorizationService.hasPermission('MANAGERS_EDIT')")
	public ManagerResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateManagerRequest request) {
		return managerService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("@authorizationService.hasPermission('MANAGERS_DELETE')")
	public void deactivate(@PathVariable UUID id) {
		managerService.deactivate(id);
	}
}
