package pl.m2manager.finance.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class FinanceAmountCalculatorTest {

	@Test
	void calculate_withVatRate_computesVatAndGross() {
		FinanceAmountCalculator.Amounts amounts = FinanceAmountCalculator.calculate(
				new BigDecimal("1500.00"),
				new BigDecimal("23.00")
		);

		assertThat(amounts.netAmount()).isEqualByComparingTo("1500.00");
		assertThat(amounts.vatAmount()).isEqualByComparingTo("345.00");
		assertThat(amounts.grossAmount()).isEqualByComparingTo("1845.00");
	}

	@Test
	void calculate_withoutVatRate_setsGrossEqualToNet() {
		FinanceAmountCalculator.Amounts amounts = FinanceAmountCalculator.calculate(
				new BigDecimal("300.00"),
				null
		);

		assertThat(amounts.vatRate()).isNull();
		assertThat(amounts.vatAmount()).isNull();
		assertThat(amounts.grossAmount()).isEqualByComparingTo("300.00");
	}
}
