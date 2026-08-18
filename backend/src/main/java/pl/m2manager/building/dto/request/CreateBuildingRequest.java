package pl.m2manager.building.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateBuildingRequest(
		@NotBlank @Size(max = 100) String code,
		@NotBlank @Size(max = 255) String name,
		@NotBlank @Size(max = 255) String address,
		@NotBlank @Size(max = 100) String city,
		@Size(max = 20) String nip,
		@Size(max = 50) String phone,
		@Email @Size(max = 255) String email,
		@Size(max = 50) String managerCode,
		@Size(max = 50) String supervisorCode,
		@Size(max = 50) String employeeCode,
		LocalDate contractSignedAt,
		LocalDate serviceStartDate,
		@NotNull @Min(0) @Max(120) Integer noticePeriodMonths,
		String notes
) {
}
