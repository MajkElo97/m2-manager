package pl.m2manager.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreateUserRequest(
		@NotBlank @Size(max = 255) String firstName,
		@NotBlank @Size(max = 255) String lastName,
		@NotBlank @Email @Size(max = 255) String email,
		@NotBlank @Size(min = 8, max = 100) String password,
		@NotEmpty List<UUID> roleIds,
		UUID employeeId
) {
}
