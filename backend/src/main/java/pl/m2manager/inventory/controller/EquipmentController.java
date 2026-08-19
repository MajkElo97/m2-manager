package pl.m2manager.inventory.controller;

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
import pl.m2manager.inventory.dto.request.CreateEquipmentRequest;
import pl.m2manager.inventory.dto.request.UpdateEquipmentRequest;
import pl.m2manager.inventory.dto.response.EquipmentResponse;
import pl.m2manager.inventory.entity.EquipmentCondition;
import pl.m2manager.inventory.service.EquipmentService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory/equipment")
public class EquipmentController {

	private final EquipmentService equipmentService;

	public EquipmentController(EquipmentService equipmentService) {
		this.equipmentService = equipmentService;
	}

	@GetMapping
	@PreAuthorize("@authorizationService.hasPermission('WAREHOUSE_VIEW')")
	public List<EquipmentResponse> list(
			@RequestParam(required = false) String search,
			@RequestParam(required = false) String category,
			@RequestParam(required = false) UUID employeeId,
			@RequestParam(required = false) EquipmentCondition condition,
			@RequestParam(required = false) Boolean active
	) {
		return equipmentService.getAll(search, category, employeeId, condition, active);
	}

	@GetMapping("/{id}")
	@PreAuthorize("@authorizationService.hasPermission('WAREHOUSE_VIEW')")
	public EquipmentResponse getById(@PathVariable UUID id) {
		return equipmentService.getById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("@authorizationService.hasPermission('WAREHOUSE_CREATE')")
	public EquipmentResponse create(@Valid @RequestBody CreateEquipmentRequest request) {
		return equipmentService.create(request);
	}

	@PutMapping("/{id}")
	@PreAuthorize("@authorizationService.hasPermission('WAREHOUSE_EDIT')")
	public EquipmentResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateEquipmentRequest request) {
		return equipmentService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("@authorizationService.hasPermission('WAREHOUSE_DELETE')")
	public void deactivate(@PathVariable UUID id) {
		equipmentService.deactivate(id);
	}
}
