package pl.m2manager.finance.dto.response;

import java.math.BigDecimal;

public record FinanceSummaryResponse(
		BigDecimal incomeNet,
		BigDecimal incomeGross,
		BigDecimal expenseNet,
		BigDecimal expenseGross,
		BigDecimal operatingResultNet,
		BigDecimal receivables,
		BigDecimal liabilities,
		BigDecimal overdueReceivables,
		BigDecimal overdueLiabilities
) {
}
