package pl.m2manager.finance.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.m2manager.finance.entity.FinancialCategory;
import pl.m2manager.finance.entity.FinancialTransaction;
import pl.m2manager.finance.entity.PaymentStatus;
import pl.m2manager.finance.entity.TransactionStatus;
import pl.m2manager.finance.entity.TransactionType;
import pl.m2manager.finance.repository.FinancialCategoryRepository;
import pl.m2manager.finance.repository.FinancialTransactionRepository;
import pl.m2manager.organization.entity.Organization;
import pl.m2manager.organization.repository.OrganizationRepository;
import pl.m2manager.tenant.TenantContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class FinanceSummaryServiceTest {

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
			.withDatabaseName("m2manager_test")
			.withUsername("m2manager")
			.withPassword("m2manager_test");

	@DynamicPropertySource
	static void configureDataSource(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
	}

	@MockitoBean
	private TenantContext tenantContext;

	@Autowired
	private FinanceSummaryService summaryService;

	@Autowired
	private FinancialCategoryRepository categoryRepository;

	@Autowired
	private FinancialTransactionRepository transactionRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	private Organization organization;
	private FinancialCategory incomeCategory;
	private FinancialCategory expenseCategory;

	@BeforeEach
	void setUp() {
		organization = saveOrganization("Org Finance Summary");
		when(tenantContext.getCurrentOrganizationId()).thenReturn(organization.getId());
		incomeCategory = saveCategory("INC-SUM", TransactionType.INCOME);
		expenseCategory = saveCategory("EXP-SUM", TransactionType.EXPENSE);
	}

	@Test
	void getSummary_calculatesIncomeExpenseAndResult() {
		saveTransaction("SUM-INC-1", TransactionType.INCOME, incomeCategory.getId(), "1000.00", "1230.00", PaymentStatus.PAID, TransactionStatus.ACTIVE, LocalDate.parse("2026-08-05"), null);
		saveTransaction("SUM-EXP-1", TransactionType.EXPENSE, expenseCategory.getId(), "400.00", "492.00", PaymentStatus.PAID, TransactionStatus.ACTIVE, LocalDate.parse("2026-08-06"), null);
		saveTransaction("SUM-CAN", TransactionType.INCOME, incomeCategory.getId(), "999.00", "999.00", PaymentStatus.PAID, TransactionStatus.CANCELLED, LocalDate.parse("2026-08-07"), null);

		var summary = summaryService.getSummary(LocalDate.parse("2026-08-01"), LocalDate.parse("2026-08-31"));

		assertThat(summary.incomeNet()).isEqualByComparingTo("1000.00");
		assertThat(summary.incomeGross()).isEqualByComparingTo("1230.00");
		assertThat(summary.expenseNet()).isEqualByComparingTo("400.00");
		assertThat(summary.expenseGross()).isEqualByComparingTo("492.00");
		assertThat(summary.operatingResultNet()).isEqualByComparingTo("600.00");
	}

	@Test
	void getSummary_calculatesReceivablesLiabilitiesAndOverdue() {
		saveTransaction("SUM-REC", TransactionType.INCOME, incomeCategory.getId(), "2000.00", "2460.00", PaymentStatus.TO_PAY, TransactionStatus.ACTIVE, LocalDate.parse("2026-07-01"), LocalDate.parse("2026-07-10"));
		saveTransaction("SUM-LIA", TransactionType.EXPENSE, expenseCategory.getId(), "500.00", "615.00", PaymentStatus.TO_PAY, TransactionStatus.ACTIVE, LocalDate.parse("2026-07-05"), LocalDate.parse("2026-07-15"));

		var summary = summaryService.getSummary(null, null);

		assertThat(summary.receivables()).isEqualByComparingTo("2460.00");
		assertThat(summary.liabilities()).isEqualByComparingTo("615.00");
		assertThat(summary.overdueReceivables()).isEqualByComparingTo("2460.00");
		assertThat(summary.overdueLiabilities()).isEqualByComparingTo("615.00");
	}

	private Organization saveOrganization(String name) {
		Organization organization = new Organization();
		organization.setName(name);
		organization.setSlug(name.toLowerCase().replace(' ', '-') + "-" + UUID.randomUUID().toString().substring(0, 8));
		organization.setTimezone("Europe/Warsaw");
		return organizationRepository.saveAndFlush(organization);
	}

	private FinancialCategory saveCategory(String code, TransactionType type) {
		FinancialCategory category = new FinancialCategory();
		category.setOrganizationId(organization.getId());
		category.setCode(code);
		category.setName(code);
		category.setType(type);
		category.setActive(true);
		return categoryRepository.saveAndFlush(category);
	}

	private void saveTransaction(
			String code,
			TransactionType type,
			UUID categoryId,
			String net,
			String gross,
			PaymentStatus paymentStatus,
			TransactionStatus status,
			LocalDate transactionDate,
			LocalDate dueDate
	) {
		FinancialTransaction transaction = new FinancialTransaction();
		transaction.setOrganizationId(organization.getId());
		transaction.setCode(code);
		transaction.setTransactionDate(transactionDate);
		transaction.setType(type);
		transaction.setNetAmount(new BigDecimal(net));
		transaction.setGrossAmount(new BigDecimal(gross));
		transaction.setCategoryId(categoryId);
		transaction.setPaymentStatus(paymentStatus);
		transaction.setStatus(status);
		transaction.setDueDate(dueDate);
		transactionRepository.saveAndFlush(transaction);
	}
}
