package pl.m2manager.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt.refresh-token-cookie")
public record RefreshTokenCookieProperties(
		String name,
		String path,
		boolean secure,
		String sameSite
) {

	public static final String DEFAULT_NAME = "m2_refresh_token";
	public static final String DEFAULT_PATH = "/api/auth";

	public RefreshTokenCookieProperties {
		if (name == null || name.isBlank()) {
			name = DEFAULT_NAME;
		}
		if (path == null || path.isBlank()) {
			path = DEFAULT_PATH;
		}
		if (sameSite == null || sameSite.isBlank()) {
			sameSite = "Lax";
		}
	}
}
