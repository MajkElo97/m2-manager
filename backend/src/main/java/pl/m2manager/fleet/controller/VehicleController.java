package pl.m2manager.fleet.controller;

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
import pl.m2manager.fleet.dto.request.CreateVehicleRequest;
import pl.m2manager.fleet.dto.request.UpdateVehicleRequest;
import pl.m2manager.fleet.dto.response.VehicleResponse;
import pl.m2manager.fleet.entity.VehicleStatus;
import pl.m2manager.fleet.entity.VehicleType;
import pl.m2manager.fleet.service.VehicleService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/fleet")
public class VehicleController {

	private final VehicleService vehicleService;

	public VehicleController(VehicleService vehicleService) {
		this.vehicleService = vehicleService;
	}

	@GetMapping
	@PreAuthorize("@authorizationService.hasPermission('FLEET_VIEW')")
	public List<VehicleResponse> list(
			@RequestParam(required = false) String search,
			@RequestParam(required = false) VehicleStatus status,
			@RequestParam(required = false) UUID employeeId,
			@RequestParam(required = false) VehicleType vehicleType
	) {
		return vehicleService.getAll(search, status, employeeId, vehicleType);
	}

	@GetMapping("/{id}")
	@PreAuthorize("@authorizationService.hasPermission('FLEET_VIEW')")
	public VehicleResponse getById(@PathVariable UUID id) {
		return vehicleService.getById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("@authorizationService.hasPermission('FLEET_CREATE')")
	public VehicleResponse create(@Valid @RequestBody CreateVehicleRequest request) {
		return vehicleService.create(request);
	}

	@PutMapping("/{id}")
	@PreAuthorize("@authorizationService.hasPermission('FLEET_EDIT')")
	public VehicleResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateVehicleRequest request) {
		return vehicleService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("@authorizationService.hasPermission('FLEET_DELETE')")
	public void deactivate(@PathVariable UUID id) {
		vehicleService.deactivate(id);
	}
}
