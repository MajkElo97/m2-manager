package pl.m2manager.fleet.dto.response;

import pl.m2manager.fleet.entity.VehicleStatus;
import pl.m2manager.fleet.entity.VehicleType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record VehicleResponse(
		UUID id,
		String code,
		String registrationNumber,
		String make,
		String model,
		Integer productionYear,
		String vin,
		VehicleType vehicleType,
		UUID employeeId,
		String employeeCode,
		String employeeName,
		VehicleStatus status,
		LocalDate insuranceStartDate,
		LocalDate insuranceEndDate,
		String insurer,
		String insurancePolicyNumber,
		LocalDate lastInspectionDate,
		LocalDate nextInspectionDate,
		Integer lastInspectionMileage,
		LocalDate purchaseDate,
		Integer currentMileage,
		String notes,
		Instant createdAt,
		Instant updatedAt
) {
}
