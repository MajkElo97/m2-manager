package pl.m2manager.employee.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import pl.m2manager.employee.entity.EmployeeRole;
import pl.m2manager.employee.entity.EmploymentType;
import pl.m2manager.employee.entity.RemunerationUnit;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record EmployeeResponse(
		UUID id,
		String code,
		String firstName,
		String lastName,
		String phone,
		String email,
		String googleEmail,
		String position,
		EmployeeRole role,
		EmploymentType employmentType,
		@JsonFormat(pattern = "yyyy-MM-dd") LocalDate employmentStartDate,
		BigDecimal remunerationAmount,
		RemunerationUnit remunerationUnit,
		Boolean remunerationNet,
		String calendarColor,
		String notes,
		boolean active,
		Instant createdAt,
		Instant updatedAt
) {
}
