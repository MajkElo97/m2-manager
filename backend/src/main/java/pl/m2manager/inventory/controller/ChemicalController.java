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
import pl.m2manager.inventory.dto.request.CreateChemicalRequest;
import pl.m2manager.inventory.dto.request.UpdateChemicalRequest;
import pl.m2manager.inventory.dto.response.ChemicalResponse;
import pl.m2manager.inventory.service.ChemicalService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory/chemicals")
public class ChemicalController {

	private final ChemicalService chemicalService;

	public ChemicalController(ChemicalService chemicalService) {
		this.chemicalService = chemicalService;
	}

	@GetMapping
	@PreAuthorize("@authorizationService.hasPermission('WAREHOUSE_VIEW')")
	public List<ChemicalResponse> list(
			@RequestParam(required = false) String search,
			@RequestParam(required = false) String category,
			@RequestParam(required = false) Boolean active,
			@RequestParam(required = false) Boolean lowStock
	) {
		return chemicalService.getAll(search, category, active, lowStock);
	}

	@GetMapping("/{id}")
	@PreAuthorize("@authorizationService.hasPermission('WAREHOUSE_VIEW')")
	public ChemicalResponse getById(@PathVariable UUID id) {
		return chemicalService.getById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("@authorizationService.hasPermission('WAREHOUSE_CREATE')")
	public ChemicalResponse create(@Valid @RequestBody CreateChemicalRequest request) {
		return chemicalService.create(request);
	}

	@PutMapping("/{id}")
	@PreAuthorize("@authorizationService.hasPermission('WAREHOUSE_EDIT')")
	public ChemicalResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateChemicalRequest request) {
		return chemicalService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("@authorizationService.hasPermission('WAREHOUSE_DELETE')")
	public void deactivate(@PathVariable UUID id) {
		chemicalService.deactivate(id);
	}
}
