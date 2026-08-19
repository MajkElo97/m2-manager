package pl.m2manager.contact.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateContactRequest(
		@NotNull UUID buildingId,
		@Size(max = 100) String firstName,
		@Size(max = 100) String lastName,
		@Size(max = 100) String functionTitle,
		@Size(max = 50) String phone,
		@Email @Size(max = 255) String email,
		String notes,
		boolean active
) {
}
