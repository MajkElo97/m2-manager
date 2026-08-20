package pl.m2manager.finance.controller;

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
import pl.m2manager.finance.dto.request.CreateFinancialCategoryRequest;
import pl.m2manager.finance.dto.request.UpdateFinancialCategoryRequest;
import pl.m2manager.finance.dto.response.FinancialCategoryResponse;
import pl.m2manager.finance.entity.TransactionType;
import pl.m2manager.finance.service.FinancialCategoryService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/finance/categories")
public class FinancialCategoryController {

	private final FinancialCategoryService categoryService;

	public FinancialCategoryController(FinancialCategoryService categoryService) {
		this.categoryService = categoryService;
	}

	@GetMapping
	@PreAuthorize("@authorizationService.hasPermission('FINANCES_VIEW')")
	public List<FinancialCategoryResponse> list(
			@RequestParam(required = false) String search,
			@RequestParam(required = false) TransactionType type,
			@RequestParam(required = false) Boolean active
	) {
		return categoryService.getAll(search, type, active);
	}

	@GetMapping("/{id}")
	@PreAuthorize("@authorizationService.hasPermission('FINANCES_VIEW')")
	public FinancialCategoryResponse getById(@PathVariable UUID id) {
		return categoryService.getById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("@authorizationService.hasPermission('FINANCES_CREATE')")
	public FinancialCategoryResponse create(@Valid @RequestBody CreateFinancialCategoryRequest request) {
		return categoryService.create(request);
	}

	@PutMapping("/{id}")
	@PreAuthorize("@authorizationService.hasPermission('FINANCES_EDIT')")
	public FinancialCategoryResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateFinancialCategoryRequest request) {
		return categoryService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("@authorizationService.hasPermission('FINANCES_DELETE')")
	public void deactivate(@PathVariable UUID id) {
		categoryService.deactivate(id);
	}
}
