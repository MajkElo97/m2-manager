package pl.m2manager.finance.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class FinanceAmountCalculator {

	private FinanceAmountCalculator() {
	}

	public record Amounts(BigDecimal netAmount, BigDecimal vatRate, BigDecimal vatAmount, BigDecimal grossAmount) {
	}

	public static Amounts calculate(BigDecimal netAmount, BigDecimal vatRate) {
		if (netAmount == null || netAmount.compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("Net amount must be non-negative");
		}

		if (vatRate == null) {
			return new Amounts(
					netAmount.setScale(2, RoundingMode.HALF_UP),
					null,
					null,
					netAmount.setScale(2, RoundingMode.HALF_UP)
			);
		}

		BigDecimal vatAmount = netAmount
				.multiply(vatRate)
				.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
		BigDecimal grossAmount = netAmount.add(vatAmount).setScale(2, RoundingMode.HALF_UP);

		return new Amounts(
				netAmount.setScale(2, RoundingMode.HALF_UP),
				vatRate.setScale(2, RoundingMode.HALF_UP),
				vatAmount,
				grossAmount
		);
	}
}
