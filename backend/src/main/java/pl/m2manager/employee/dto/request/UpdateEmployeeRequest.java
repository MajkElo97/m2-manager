package pl.m2manager.employee.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import pl.m2manager.employee.entity.EmployeeRole;
import pl.m2manager.employee.entity.EmploymentType;
import pl.m2manager.employee.entity.RemunerationUnit;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateEmployeeRequest(
		@NotBlank @Size(max = 50) String code,
		@NotBlank @Size(max = 100) String firstName,
		@Size(max = 100) String lastName,
		@Size(max = 50) String phone,
		@Email @Size(max = 255) String email,
		@Email @Size(max = 255) String googleEmail,
		@Size(max = 100) String position,
		@NotNull EmployeeRole role,
		EmploymentType employmentType,
		LocalDate employmentStartDate,
		@DecimalMin(value = "0.0", inclusive = true) BigDecimal remunerationAmount,
		RemunerationUnit remunerationUnit,
		Boolean remunerationNet,
		@Pattern(regexp = "^#[0-9A-Fa-f]{6}$") @Size(max = 7) String calendarColor,
		String notes,
		@NotNull Boolean active
) {
}
