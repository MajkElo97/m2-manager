package pl.m2manager.finance.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pl.m2manager.finance.entity.PaymentStatus;
import pl.m2manager.finance.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateFinancialTransactionRequest(
		@NotBlank @Size(max = 50) String code,
		@NotNull LocalDate transactionDate,
		@NotNull TransactionType type,
		@NotNull @DecimalMin("0.00") BigDecimal netAmount,
		BigDecimal vatRate,
		@NotNull UUID categoryId,
		@Size(max = 255) String contractorName,
		@Size(max = 20) String contractorNip,
		UUID buildingId,
		UUID employeeId,
		UUID vehicleId,
		UUID equipmentId,
		UUID chemicalId,
		String description,
		@Size(max = 100) String documentNumber,
		LocalDate dueDate,
		LocalDate paymentDate,
		@NotNull PaymentStatus paymentStatus,
		String notes
) {
}
