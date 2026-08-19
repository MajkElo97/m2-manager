package pl.m2manager.manager.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateManagerRequest(
		@NotBlank @Size(max = 50) String code,
		@NotBlank @Size(max = 255) String name,
		@Size(max = 50) String phone,
		@Email @Size(max = 255) String email,
		@Size(max = 500) String address,
		String notes
) {
}
