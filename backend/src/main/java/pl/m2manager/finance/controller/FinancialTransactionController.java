package pl.m2manager.finance.controller;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
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
import pl.m2manager.finance.dto.request.CreateFinancialTransactionRequest;
import pl.m2manager.finance.dto.request.UpdateFinancialTransactionRequest;
import pl.m2manager.finance.dto.response.FinancialTransactionResponse;
import pl.m2manager.finance.entity.PaymentStatus;
import pl.m2manager.finance.entity.TransactionStatus;
import pl.m2manager.finance.entity.TransactionType;
import pl.m2manager.finance.service.FinancialTransactionService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/finance/transactions")
public class FinancialTransactionController {

	private final FinancialTransactionService transactionService;

	public FinancialTransactionController(FinancialTransactionService transactionService) {
		this.transactionService = transactionService;
	}

	@GetMapping
	@PreAuthorize("@authorizationService.hasPermission('FINANCES_VIEW')")
	public List<FinancialTransactionResponse> list(
			@RequestParam(required = false) String search,
			@RequestParam(required = false) TransactionType type,
			@RequestParam(required = false) UUID categoryId,
			@RequestParam(required = false) UUID buildingId,
			@RequestParam(required = false) UUID employeeId,
			@RequestParam(required = false) UUID vehicleId,
			@RequestParam(required = false) PaymentStatus paymentStatus,
			@RequestParam(required = false) TransactionStatus status,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
	) {
		return transactionService.getAll(
				search, type, categoryId, buildingId, employeeId, vehicleId,
				paymentStatus, status, dateFrom, dateTo
		);
	}

	@GetMapping("/{id}")
	@PreAuthorize("@authorizationService.hasPermission('FINANCES_VIEW')")
	public FinancialTransactionResponse getById(@PathVariable UUID id) {
		return transactionService.getById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("@authorizationService.hasPermission('FINANCES_CREATE')")
	public FinancialTransactionResponse create(@Valid @RequestBody CreateFinancialTransactionRequest request) {
		return transactionService.create(request);
	}

	@PutMapping("/{id}")
	@PreAuthorize("@authorizationService.hasPermission('FINANCES_EDIT')")
	public FinancialTransactionResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateFinancialTransactionRequest request) {
		return transactionService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("@authorizationService.hasPermission('FINANCES_DELETE')")
	public void cancel(@PathVariable UUID id) {
		transactionService.cancel(id);
	}
}
