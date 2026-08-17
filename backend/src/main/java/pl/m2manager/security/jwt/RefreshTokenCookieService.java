package pl.m2manager.security.jwt;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RefreshTokenCookieService {

	private final RefreshTokenCookieProperties cookieProperties;
	private final JwtProperties jwtProperties;

	public RefreshTokenCookieService(
			RefreshTokenCookieProperties cookieProperties,
			JwtProperties jwtProperties
	) {
		this.cookieProperties = cookieProperties;
		this.jwtProperties = jwtProperties;
	}

	public void setRefreshTokenCookie(HttpServletResponse response, String rawRefreshToken) {
		response.addHeader("Set-Cookie", buildCookie(rawRefreshToken, jwtProperties.refreshTokenExpiration()).toString());
	}

	public void clearRefreshTokenCookie(HttpServletResponse response) {
		response.addHeader("Set-Cookie", buildCookie("", Duration.ZERO).toString());
	}

	private ResponseCookie buildCookie(String value, Duration maxAge) {
		ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(cookieProperties.name(), value)
				.path(cookieProperties.path())
				.httpOnly(true)
				.secure(cookieProperties.secure())
				.sameSite(cookieProperties.sameSite());

		if (maxAge.isZero()) {
			builder.maxAge(0);
		} else {
			builder.maxAge(maxAge);
		}

		return builder.build();
	}
}
