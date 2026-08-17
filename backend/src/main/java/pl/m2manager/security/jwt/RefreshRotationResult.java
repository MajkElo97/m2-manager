package pl.m2manager.security.jwt;

import pl.m2manager.security.auth.dto.AuthenticationResult;

public record RefreshRotationResult(
		AuthenticationResult authenticationResult,
		String rawRefreshToken
) {
}
