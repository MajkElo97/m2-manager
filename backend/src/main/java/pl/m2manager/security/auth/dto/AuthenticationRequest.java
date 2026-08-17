package pl.m2manager.security.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthenticationRequest(
		@NotBlank @Size(max = 100) String organizationSlug,
		@NotBlank @Email @Size(max = 255) String email,
		@NotBlank String password
) {
}
