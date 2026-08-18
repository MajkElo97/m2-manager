package pl.m2manager.staircase.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateStaircaseRequest(
		@NotNull UUID buildingId,
		@NotBlank @Size(max = 100) String code,
		@NotBlank @Size(max = 50) String designation,
		@Size(max = 255) String intercomCode,
		boolean keyRequired,
		boolean elevator,
		@NotNull @Min(0) @Max(200) Integer floors,
		String notes
) {
}
