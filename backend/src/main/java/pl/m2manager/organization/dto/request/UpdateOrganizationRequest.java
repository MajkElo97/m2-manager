package pl.m2manager.organization.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateOrganizationRequest(
		@NotBlank @Size(max = 255) String name,
		@Size(max = 20) String nip,
		@Email @Size(max = 255) String email,
		@Size(max = 50) String phone,
		@NotBlank @Size(max = 64) String timezone
) {
}
