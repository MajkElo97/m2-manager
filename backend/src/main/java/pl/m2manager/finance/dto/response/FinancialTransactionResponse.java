package pl.m2manager.finance.dto.response;

import pl.m2manager.finance.entity.PaymentStatus;
import pl.m2manager.finance.entity.TransactionStatus;
import pl.m2manager.finance.entity.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record FinancialTransactionResponse(
		UUID id,
		String code,
		LocalDate transactionDate,
		TransactionType type,
		BigDecimal netAmount,
		BigDecimal vatRate,
		BigDecimal vatAmount,
		BigDecimal grossAmount,
		UUID categoryId,
		String categoryCode,
		String categoryName,
		String contractorName,
		String contractorNip,
		UUID buildingId,
		String buildingCode,
		String buildingName,
		UUID employeeId,
		String employeeCode,
		String employeeName,
		UUID vehicleId,
		String vehicleCode,
		String vehicleRegistrationNumber,
		UUID equipmentId,
		String equipmentCode,
		String equipmentName,
		UUID chemicalId,
		String chemicalCode,
		String chemicalName,
		String description,
		String documentNumber,
		LocalDate dueDate,
		LocalDate paymentDate,
		PaymentStatus paymentStatus,
		TransactionStatus status,
		String notes,
		Instant createdAt,
		Instant updatedAt
) {
}
