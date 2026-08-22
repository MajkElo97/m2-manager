package pl.m2manager.organization.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateOrganizationRequest(
		@NotBlank @Size(max = 255) String name,
		@NotBlank
		@Size(min = 2, max = 100)
		@Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Slug must contain lowercase letters, digits and hyphens only")
		String slug,
		@NotBlank @Email @Size(max = 255) String adminEmail
) {
}
