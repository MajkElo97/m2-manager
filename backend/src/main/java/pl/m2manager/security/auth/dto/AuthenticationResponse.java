package pl.m2manager.security.auth.dto;

public record AuthenticationResponse(
		String accessToken,
		String tokenType,
		long expiresIn
) {
}
