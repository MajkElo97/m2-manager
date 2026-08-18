package pl.m2manager.staircase.controller;

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
import pl.m2manager.staircase.dto.request.CreateStaircaseRequest;
import pl.m2manager.staircase.dto.request.UpdateStaircaseRequest;
import pl.m2manager.staircase.dto.response.StaircaseResponse;
import pl.m2manager.staircase.service.StaircaseService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/staircases")
public class StaircaseController {

	private final StaircaseService staircaseService;

	public StaircaseController(StaircaseService staircaseService) {
		this.staircaseService = staircaseService;
	}

	@GetMapping
	@PreAuthorize("@authorizationService.hasPermission('BUILDINGS_VIEW')")
	public List<StaircaseResponse> list(@RequestParam UUID buildingId) {
		return staircaseService.getAll(buildingId);
	}

	@GetMapping("/{id}")
	@PreAuthorize("@authorizationService.hasPermission('BUILDINGS_VIEW')")
	public StaircaseResponse getById(@PathVariable UUID id) {
		return staircaseService.getById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("@authorizationService.hasPermission('BUILDINGS_EDIT')")
	public StaircaseResponse create(@Valid @RequestBody CreateStaircaseRequest request) {
		return staircaseService.create(request);
	}

	@PutMapping("/{id}")
	@PreAuthorize("@authorizationService.hasPermission('BUILDINGS_EDIT')")
	public StaircaseResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateStaircaseRequest request) {
		return staircaseService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("@authorizationService.hasPermission('BUILDINGS_EDIT')")
	public void delete(@PathVariable UUID id) {
		staircaseService.delete(id);
	}
}
