package pl.m2manager.finance.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.m2manager.common.exception.GlobalExceptionHandler;
import pl.m2manager.finance.dto.response.FinanceSummaryResponse;
import pl.m2manager.finance.dto.response.FinancialCategoryResponse;
import pl.m2manager.finance.dto.response.FinancialTransactionResponse;
import pl.m2manager.finance.entity.PaymentStatus;
import pl.m2manager.finance.entity.TransactionStatus;
import pl.m2manager.finance.entity.TransactionType;
import pl.m2manager.finance.service.FinanceSummaryService;
import pl.m2manager.finance.service.FinancialCategoryService;
import pl.m2manager.finance.service.FinancialTransactionService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({ FinancialTransactionController.class, FinancialCategoryController.class, FinanceSummaryController.class })
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class FinanceControllerTest {

	private static final UUID TRANSACTION_ID = UUID.fromString("f7100000-0000-4000-8000-000000000001");
	private static final UUID CATEGORY_ID = UUID.fromString("f7000000-0000-4000-8000-000000000001");
	private static final Instant CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");
	private static final Instant UPDATED_AT = Instant.parse("2026-01-02T00:00:00Z");

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private FinancialTransactionService transactionService;

	@MockitoBean
	private FinancialCategoryService categoryService;

	@MockitoBean
	private FinanceSummaryService summaryService;

	@Test
	void transactionsList_returns200() throws Exception {
		when(transactionService.getAll(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
				.thenReturn(List.of(sampleTransactionResponse()));

		mockMvc.perform(get("/api/finance/transactions"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].code").value("DEMO-FN-001"));
	}

	@Test
	void transactionCreate_returns201() throws Exception {
		when(transactionService.create(any())).thenReturn(sampleTransactionResponse());

		mockMvc.perform(post("/api/finance/transactions")
						.contentType(MediaType.APPLICATION_JSON)
						.content(validTransactionPayload()))
				.andExpect(status().isCreated());
	}

	@Test
	void transactionCancel_returns204() throws Exception {
		mockMvc.perform(delete("/api/finance/transactions/{id}", TRANSACTION_ID))
				.andExpect(status().isNoContent());

		verify(transactionService).cancel(TRANSACTION_ID);
	}

	@Test
	void categoriesList_returns200() throws Exception {
		when(categoryService.getAll(null, null, null)).thenReturn(List.of(sampleCategoryResponse()));

		mockMvc.perform(get("/api/finance/categories"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].code").value("USLUGI"));
	}

	@Test
	void summary_returns200() throws Exception {
		when(summaryService.getSummary(any(), any())).thenReturn(new FinanceSummaryResponse(
				new BigDecimal("1000.00"),
				new BigDecimal("1230.00"),
				new BigDecimal("400.00"),
				new BigDecimal("492.00"),
				new BigDecimal("600.00"),
				new BigDecimal("2460.00"),
				new BigDecimal("615.00"),
				new BigDecimal("984.00"),
				new BigDecimal("300.00")
		));

		mockMvc.perform(get("/api/finance/summary"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.operatingResultNet").value(600.00));
	}

	private FinancialTransactionResponse sampleTransactionResponse() {
		return new FinancialTransactionResponse(
				TRANSACTION_ID,
				"DEMO-FN-001",
				LocalDate.parse("2026-08-01"),
				TransactionType.INCOME,
				new BigDecimal("1500.00"),
				new BigDecimal("23.00"),
				new BigDecimal("345.00"),
				new BigDecimal("1845.00"),
				CATEGORY_ID,
				"USLUGI",
				"Usługi",
				"Demo",
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				"FV/DEMO/001",
				LocalDate.parse("2026-08-15"),
				LocalDate.parse("2026-08-14"),
				PaymentStatus.PAID,
				TransactionStatus.ACTIVE,
				null,
				CREATED_AT,
				UPDATED_AT
		);
	}

	private FinancialCategoryResponse sampleCategoryResponse() {
		return new FinancialCategoryResponse(
				CATEGORY_ID,
				"USLUGI",
				"Usługi",
				TransactionType.INCOME,
				true,
				CREATED_AT,
				UPDATED_AT
		);
	}

	private String validTransactionPayload() {
		return """
				{
				  "code": "FN0001",
				  "transactionDate": "2026-08-01",
				  "type": "INCOME",
				  "netAmount": 1500.00,
				  "vatRate": 23.00,
				  "categoryId": "f7000000-0000-4000-8000-000000000001",
				  "paymentStatus": "PAID"
				}
				""";
	}
}
