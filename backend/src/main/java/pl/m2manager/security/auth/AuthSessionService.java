package pl.m2manager.security.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import pl.m2manager.security.auth.dto.AuthenticationResponse;
import pl.m2manager.security.auth.dto.AuthenticationResult;
import pl.m2manager.security.jwt.IssuedRefreshToken;
import pl.m2manager.security.jwt.JwtService;
import pl.m2manager.security.jwt.RefreshRotationResult;
import pl.m2manager.security.jwt.RefreshTokenCookieProperties;
import pl.m2manager.security.jwt.RefreshTokenCookieService;
import pl.m2manager.security.jwt.RefreshTokenService;

import java.util.Arrays;

@Service
public class AuthSessionService {

	private final AuthenticationService authenticationService;
	private final JwtService jwtService;
	private final RefreshTokenService refreshTokenService;
	private final RefreshTokenCookieService refreshTokenCookieService;
	private final RefreshTokenCookieProperties cookieProperties;

	public AuthSessionService(
			AuthenticationService authenticationService,
			JwtService jwtService,
			RefreshTokenService refreshTokenService,
			RefreshTokenCookieService refreshTokenCookieService,
			RefreshTokenCookieProperties cookieProperties
	) {
		this.authenticationService = authenticationService;
		this.jwtService = jwtService;
		this.refreshTokenService = refreshTokenService;
		this.refreshTokenCookieService = refreshTokenCookieService;
		this.cookieProperties = cookieProperties;
	}

	public AuthenticationResponse login(
			String organizationSlug,
			String email,
			String password,
			HttpServletResponse response
	) {
		AuthenticationResult result = authenticationService.authenticate(organizationSlug, email, password);
		IssuedRefreshToken refreshToken = refreshTokenService.issueNewFamily(
				result.userId(),
				result.organizationId()
		);
		refreshTokenCookieService.setRefreshTokenCookie(response, refreshToken.rawToken());
		return buildAccessTokenResponse(result);
	}

	public AuthenticationResponse refresh(HttpServletRequest request, HttpServletResponse response) {
		String rawRefreshToken = extractRefreshToken(request);
		RefreshRotationResult rotation = refreshTokenService.rotate(rawRefreshToken);
		refreshTokenCookieService.setRefreshTokenCookie(response, rotation.rawRefreshToken());
		return buildAccessTokenResponse(rotation.authenticationResult());
	}

	public void logout(HttpServletRequest request, HttpServletResponse response) {
		refreshTokenService.revoke(extractRefreshToken(request));
		refreshTokenCookieService.clearRefreshTokenCookie(response);
	}

	private AuthenticationResponse buildAccessTokenResponse(AuthenticationResult result) {
		return new AuthenticationResponse(
				jwtService.generateAccessToken(result),
				"Bearer",
				jwtService.accessTokenExpirationSeconds()
		);
	}

	private String extractRefreshToken(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return null;
		}
		return Arrays.stream(cookies)
				.filter(cookie -> cookieProperties.name().equals(cookie.getName()))
				.map(Cookie::getValue)
				.findFirst()
				.orElse(null);
	}
}
