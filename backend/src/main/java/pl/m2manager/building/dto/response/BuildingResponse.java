package pl.m2manager.building.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import pl.m2manager.building.entity.BuildingStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record BuildingResponse(
		UUID id,
		String code,
		String name,
		String address,
		String city,
		String nip,
		String phone,
		String email,
		String managerCode,
		String supervisorCode,
		String employeeCode,
		@JsonFormat(pattern = "yyyy-MM-dd") LocalDate contractSignedAt,
		@JsonFormat(pattern = "yyyy-MM-dd") LocalDate serviceStartDate,
		Integer noticePeriodMonths,
		BuildingStatus status,
		String notes,
		Instant createdAt,
		Instant updatedAt
) {
}
