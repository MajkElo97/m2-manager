package pl.m2manager.finance.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.m2manager.finance.dto.response.FinanceSummaryResponse;
import pl.m2manager.finance.entity.TransactionType;
import pl.m2manager.finance.repository.FinancialTransactionRepository;
import pl.m2manager.tenant.TenantContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class FinanceSummaryService {

	private final FinancialTransactionRepository transactionRepository;
	private final TenantContext tenantContext;

	public FinanceSummaryService(
			FinancialTransactionRepository transactionRepository,
			TenantContext tenantContext
	) {
		this.transactionRepository = transactionRepository;
		this.tenantContext = tenantContext;
	}

	public FinanceSummaryResponse getSummary(LocalDate dateFrom, LocalDate dateTo) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		LocalDate today = LocalDate.now();
		LocalDate effectiveFrom = dateFrom != null ? dateFrom : LocalDate.of(1900, 1, 1);
		LocalDate effectiveTo = dateTo != null ? dateTo : LocalDate.of(2100, 12, 31);

		BigDecimal incomeNet = transactionRepository.sumNetByTypeAndDateRange(
				organizationId, TransactionType.INCOME, effectiveFrom, effectiveTo);
		BigDecimal incomeGross = transactionRepository.sumGrossByTypeAndDateRange(
				organizationId, TransactionType.INCOME, effectiveFrom, effectiveTo);
		BigDecimal expenseNet = transactionRepository.sumNetByTypeAndDateRange(
				organizationId, TransactionType.EXPENSE, effectiveFrom, effectiveTo);
		BigDecimal expenseGross = transactionRepository.sumGrossByTypeAndDateRange(
				organizationId, TransactionType.EXPENSE, effectiveFrom, effectiveTo);

		BigDecimal operatingResultNet = incomeNet.subtract(expenseNet);
		BigDecimal receivables = transactionRepository.sumOutstandingGross(organizationId, TransactionType.INCOME);
		BigDecimal liabilities = transactionRepository.sumOutstandingGross(organizationId, TransactionType.EXPENSE);
		BigDecimal overdueReceivables = transactionRepository.sumOverdueGross(organizationId, TransactionType.INCOME, today);
		BigDecimal overdueLiabilities = transactionRepository.sumOverdueGross(organizationId, TransactionType.EXPENSE, today);

		return new FinanceSummaryResponse(
				incomeNet,
				incomeGross,
				expenseNet,
				expenseGross,
				operatingResultNet,
				receivables,
				liabilities,
				overdueReceivables,
				overdueLiabilities
		);
	}
}
