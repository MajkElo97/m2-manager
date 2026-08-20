package pl.m2manager.finance.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.m2manager.finance.entity.FinancialCategory;
import pl.m2manager.finance.entity.FinancialTransaction;
import pl.m2manager.finance.entity.PaymentStatus;
import pl.m2manager.finance.entity.TransactionStatus;
import pl.m2manager.finance.entity.TransactionType;
import pl.m2manager.organization.entity.Organization;
import pl.m2manager.organization.repository.OrganizationRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class FinanceRepositoryTest {

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

	@Autowired
	private FinancialCategoryRepository categoryRepository;

	@Autowired
	private FinancialTransactionRepository transactionRepository;

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void flywayV34V35_createsFinanceTables() {
		assertThat(jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'financial_categories'",
				Integer.class
		)).isEqualTo(1);
		assertThat(jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'financial_transactions'",
				Integer.class
		)).isEqualTo(1);
	}

	@Test
	void transactions_findAllByFilters_searchByCode() {
		Organization organization = saveOrganization("Org Finance Repo Search");
		FinancialCategory category = saveCategory(organization.getId(), "CAT001", TransactionType.INCOME);
		saveTransaction(organization.getId(), "FN-SEARCH-1", category.getId(), "Unique Contractor");
		saveTransaction(organization.getId(), "FN-OTHER-1", category.getId(), "Other");

		assertThat(transactionRepository.findAllByOrganizationIdAndFilters(
				organization.getId(), "unique", null, null, null, null, null, null, null, null, null
		)).extracting(FinancialTransaction::getCode).containsExactly("FN-SEARCH-1");
	}

	@Test
	void summary_sumsExcludeCancelled() {
		Organization organization = saveOrganization("Org Finance Repo Summary");
		FinancialCategory income = saveCategory(organization.getId(), "CAT002", TransactionType.INCOME);
		saveTransactionWithAmounts(organization.getId(), "FN-ACTIVE", income.getId(), "100.00", "123.00", TransactionStatus.ACTIVE);
		saveTransactionWithAmounts(organization.getId(), "FN-CANCEL", income.getId(), "999.00", "999.00", TransactionStatus.CANCELLED);

		assertThat(transactionRepository.sumNetByTypeAndDateRange(
				organization.getId(), TransactionType.INCOME,
				LocalDate.of(1900, 1, 1), LocalDate.of(2100, 12, 31)
		)).isEqualByComparingTo("100.00");
	}

	private Organization saveOrganization(String name) {
		Organization organization = new Organization();
		organization.setName(name);
		organization.setSlug(name.toLowerCase().replace(' ', '-') + "-" + UUID.randomUUID().toString().substring(0, 8));
		organization.setTimezone("Europe/Warsaw");
		return organizationRepository.saveAndFlush(organization);
	}

	private FinancialCategory saveCategory(UUID organizationId, String code, TransactionType type) {
		FinancialCategory category = new FinancialCategory();
		category.setOrganizationId(organizationId);
		category.setCode(code);
		category.setName(code);
		category.setType(type);
		category.setActive(true);
		return categoryRepository.saveAndFlush(category);
	}

	private FinancialTransaction saveTransaction(UUID organizationId, String code, UUID categoryId, String contractorName) {
		FinancialTransaction transaction = new FinancialTransaction();
		transaction.setOrganizationId(organizationId);
		transaction.setCode(code);
		transaction.setTransactionDate(LocalDate.parse("2026-08-01"));
		transaction.setType(TransactionType.INCOME);
		transaction.setNetAmount(new BigDecimal("100.00"));
		transaction.setGrossAmount(new BigDecimal("100.00"));
		transaction.setCategoryId(categoryId);
		transaction.setContractorName(contractorName);
		transaction.setPaymentStatus(PaymentStatus.NOT_APPLICABLE);
		transaction.setStatus(TransactionStatus.ACTIVE);
		return transactionRepository.saveAndFlush(transaction);
	}

	private void saveTransactionWithAmounts(
			UUID organizationId,
			String code,
			UUID categoryId,
			String net,
			String gross,
			TransactionStatus status
	) {
		FinancialTransaction transaction = new FinancialTransaction();
		transaction.setOrganizationId(organizationId);
		transaction.setCode(code);
		transaction.setTransactionDate(LocalDate.parse("2026-08-01"));
		transaction.setType(TransactionType.INCOME);
		transaction.setNetAmount(new BigDecimal(net));
		transaction.setGrossAmount(new BigDecimal(gross));
		transaction.setCategoryId(categoryId);
		transaction.setPaymentStatus(PaymentStatus.PAID);
		transaction.setStatus(status);
		transactionRepository.saveAndFlush(transaction);
	}
}
