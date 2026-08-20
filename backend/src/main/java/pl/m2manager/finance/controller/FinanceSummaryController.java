package pl.m2manager.finance.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.m2manager.finance.dto.response.FinanceSummaryResponse;
import pl.m2manager.finance.service.FinanceSummaryService;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/finance/summary")
public class FinanceSummaryController {

	private final FinanceSummaryService summaryService;

	public FinanceSummaryController(FinanceSummaryService summaryService) {
		this.summaryService = summaryService;
	}

	@GetMapping
	@PreAuthorize("@authorizationService.hasPermission('FINANCES_VIEW')")
	public FinanceSummaryResponse getSummary(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
	) {
		return summaryService.getSummary(dateFrom, dateTo);
	}
}
