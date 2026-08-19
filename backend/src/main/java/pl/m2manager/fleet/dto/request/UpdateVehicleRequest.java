package pl.m2manager.fleet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pl.m2manager.fleet.entity.VehicleStatus;
import pl.m2manager.fleet.entity.VehicleType;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateVehicleRequest(
		@NotBlank @Size(max = 50) String code,
		@NotBlank @Size(max = 20) String registrationNumber,
		@NotBlank @Size(max = 100) String make,
		@NotBlank @Size(max = 100) String model,
		Integer productionYear,
		@Size(max = 50) String vin,
		@NotNull VehicleType vehicleType,
		UUID employeeId,
		@NotNull VehicleStatus status,
		LocalDate insuranceStartDate,
		LocalDate insuranceEndDate,
		@Size(max = 255) String insurer,
		@Size(max = 100) String insurancePolicyNumber,
		LocalDate lastInspectionDate,
		LocalDate nextInspectionDate,
		Integer lastInspectionMileage,
		LocalDate purchaseDate,
		Integer currentMileage,
		String notes
) {
}
