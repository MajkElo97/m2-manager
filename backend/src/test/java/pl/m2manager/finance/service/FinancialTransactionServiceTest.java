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
import pl.m2manager.common.exception.BusinessConflictException;
import pl.m2manager.common.exception.ResourceNotFoundException;
import pl.m2manager.finance.dto.request.CreateFinancialCategoryRequest;
import pl.m2manager.finance.dto.request.CreateFinancialTransactionRequest;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class FinancialTransactionServiceTest {

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
	private FinancialTransactionService transactionService;

	@Autowired
	private FinancialCategoryRepository categoryRepository;

	@Autowired
	private FinancialTransactionRepository transactionRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	private Organization organizationA;
	private Organization organizationB;
	private FinancialCategory incomeCategoryA;
	private FinancialCategory expenseCategoryA;

	@BeforeEach
	void setUp() {
		organizationA = saveOrganization("Org Finance Tx A");
		organizationB = saveOrganization("Org Finance Tx B");
		when(tenantContext.getCurrentOrganizationId()).thenReturn(organizationA.getId());
		incomeCategoryA = saveCategory(organizationA.getId(), "INC001", "Income Cat", TransactionType.INCOME);
		expenseCategoryA = saveCategory(organizationA.getId(), "EXP001", "Expense Cat", TransactionType.EXPENSE);
	}

	@Test
	void create_calculatesVatAndGross() {
		var created = transactionService.create(sampleCreateRequest("FN0001", TransactionType.INCOME, incomeCategoryA.getId(), "1000.00", "23.00"));

		assertThat(created.netAmount()).isEqualByComparingTo("1000.00");
		assertThat(created.vatAmount()).isEqualByComparingTo("230.00");
		assertThat(created.grossAmount()).isEqualByComparingTo("1230.00");
	}

	@Test
	void create_categoryTypeMismatch_throwsBusinessConflict() {
		assertThatThrownBy(() -> transactionService.create(
				sampleCreateRequest("FN0002", TransactionType.INCOME, expenseCategoryA.getId(), "100.00", null)
		)).isInstanceOf(BusinessConflictException.class);
	}

	@Test
	void getById_tenantIsolation() {
		FinancialTransaction transactionB = saveTransaction(organizationB.getId(), "FN0003", TransactionType.EXPENSE, saveCategory(organizationB.getId(), "EXP002", "B Exp", TransactionType.EXPENSE).getId());

		assertThatThrownBy(() -> transactionService.getById(transactionB.getId()))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void cancel_setsStatusCancelled() {
		var created = transactionService.create(sampleCreateRequest("FN0004", TransactionType.INCOME, incomeCategoryA.getId(), "500.00", null));

		transactionService.cancel(created.id());

		FinancialTransaction reloaded = transactionRepository.findById(created.id()).orElseThrow();
		assertThat(reloaded.getStatus()).isEqualTo(TransactionStatus.CANCELLED);
	}

	private CreateFinancialTransactionRequest sampleCreateRequest(
			String code,
			TransactionType type,
			UUID categoryId,
			String netAmount,
			String vatRate
	) {
		return new CreateFinancialTransactionRequest(
				code,
				LocalDate.parse("2026-08-01"),
				type,
				new BigDecimal(netAmount),
				vatRate != null ? new BigDecimal(vatRate) : null,
				categoryId,
				"Contractor",
				null,
				null,
				null,
				null,
				null,
				null,
				"Description",
				"DOC/001",
				null,
				null,
				PaymentStatus.NOT_APPLICABLE,
				null
		);
	}

	private Organization saveOrganization(String name) {
		Organization organization = new Organization();
		organization.setName(name);
		organization.setSlug(name.toLowerCase().replace(' ', '-') + "-" + UUID.randomUUID().toString().substring(0, 8));
		organization.setTimezone("Europe/Warsaw");
		return organizationRepository.saveAndFlush(organization);
	}

	private FinancialCategory saveCategory(UUID organizationId, String code, String name, TransactionType type) {
		FinancialCategory category = new FinancialCategory();
		category.setOrganizationId(organizationId);
		category.setCode(code);
		category.setName(name);
		category.setType(type);
		category.setActive(true);
		return categoryRepository.saveAndFlush(category);
	}

	private FinancialTransaction saveTransaction(UUID organizationId, String code, TransactionType type, UUID categoryId) {
		FinancialTransaction transaction = new FinancialTransaction();
		transaction.setOrganizationId(organizationId);
		transaction.setCode(code);
		transaction.setTransactionDate(LocalDate.parse("2026-08-01"));
		transaction.setType(type);
		transaction.setNetAmount(new BigDecimal("100.00"));
		transaction.setGrossAmount(new BigDecimal("100.00"));
		transaction.setCategoryId(categoryId);
		transaction.setPaymentStatus(PaymentStatus.NOT_APPLICABLE);
		transaction.setStatus(TransactionStatus.ACTIVE);
		return transactionRepository.saveAndFlush(transaction);
	}
}
