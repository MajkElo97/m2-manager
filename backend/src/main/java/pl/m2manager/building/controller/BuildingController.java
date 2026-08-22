package pl.m2manager.building.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
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
import pl.m2manager.building.dto.request.CreateBuildingRequest;
import pl.m2manager.building.dto.request.UpdateBuildingRequest;
import pl.m2manager.building.dto.response.BuildingResponse;
import pl.m2manager.building.entity.BuildingStatus;
import pl.m2manager.building.service.BuildingPermanentDeleteService;
import pl.m2manager.building.service.BuildingService;
import pl.m2manager.security.jwt.JwtAuthenticatedPrincipal;
import pl.m2manager.security.jwt.JwtAuthenticationToken;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/buildings")
public class BuildingController {

	private final BuildingService buildingService;
	private final BuildingPermanentDeleteService buildingPermanentDeleteService;

	public BuildingController(
			BuildingService buildingService,
			BuildingPermanentDeleteService buildingPermanentDeleteService
	) {
		this.buildingService = buildingService;
		this.buildingPermanentDeleteService = buildingPermanentDeleteService;
	}

	@GetMapping
	@PreAuthorize("@authorizationService.hasPermission('BUILDINGS_VIEW')")
	public List<BuildingResponse> list(
			@RequestParam(required = false) BuildingStatus status,
			@RequestParam(required = false) String search
	) {
		return buildingService.getAll(status, search);
	}

	@GetMapping("/{id}")
	@PreAuthorize("@authorizationService.hasPermission('BUILDINGS_VIEW')")
	public BuildingResponse getById(@PathVariable UUID id) {
		return buildingService.getById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("@authorizationService.hasPermission('BUILDINGS_CREATE')")
	public BuildingResponse create(@Valid @RequestBody CreateBuildingRequest request) {
		return buildingService.create(request);
	}

	@PutMapping("/{id}")
	@PreAuthorize("@authorizationService.hasPermission('BUILDINGS_EDIT')")
	public BuildingResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateBuildingRequest request) {
		return buildingService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("@authorizationService.hasPermission('BUILDINGS_DELETE')")
	public void deactivate(@PathVariable UUID id) {
		buildingService.deactivate(id);
	}

	@DeleteMapping("/{id}/permanent")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void permanentDelete(@PathVariable UUID id) {
		JwtAuthenticatedPrincipal principal = requirePrincipal();
		buildingPermanentDeleteService.permanentDelete(id, principal.userId());
	}

	private JwtAuthenticatedPrincipal requirePrincipal() {
		var authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
			return jwtAuthenticationToken.getPrincipal();
		}
		throw new IllegalStateException("Authenticated JWT principal required");
	}
}
