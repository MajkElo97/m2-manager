package pl.m2manager.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import pl.m2manager.security.auth.dto.AuthenticationResult;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

	private static final String SECRET = "test-jwt-secret-not-for-production-min-32-chars";
	private static final UUID USER_ID = UUID.fromString("b0000000-0000-4000-8000-000000000001");
	private static final UUID ORGANIZATION_ID = UUID.fromString("a0000000-0000-4000-8000-000000000002");
	private static final String EMAIL = "john@example.com";

	private JwtService jwtService;
	private AuthenticationResult authenticationResult;

	@BeforeEach
	void setUp() {
		Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
		jwtService = new JwtService(new JwtProperties(SECRET, Duration.ofMinutes(15)), clock);
		ReflectionTestUtils.invokeMethod(jwtService, "initSigningKey");
		authenticationResult = new AuthenticationResult(USER_ID, ORGANIZATION_ID, EMAIL);
	}

	@Test
	void generateAccessToken_producesValidToken() {
		String token = jwtService.generateAccessToken(authenticationResult);

		assertThat(token).isNotBlank();
		assertThat(token.split("\\.")).hasSize(3);
	}

	@Test
	void token_containsCorrectSubject() {
		String token = jwtService.generateAccessToken(authenticationResult);

		JwtAuthenticatedPrincipal principal = jwtService.parseAndValidate(token);

		assertThat(principal.userId()).isEqualTo(USER_ID);
	}

	@Test
	void token_containsCorrectOrganizationId() {
		String token = jwtService.generateAccessToken(authenticationResult);

		JwtAuthenticatedPrincipal principal = jwtService.parseAndValidate(token);

		assertThat(principal.organizationId()).isEqualTo(ORGANIZATION_ID);
	}

	@Test
	void token_containsCorrectEmail() {
		String token = jwtService.generateAccessToken(authenticationResult);

		JwtAuthenticatedPrincipal principal = jwtService.parseAndValidate(token);

		assertThat(principal.email()).isEqualTo(EMAIL);
	}

	@Test
	void token_containsExpiration() {
		String token = jwtService.generateAccessToken(authenticationResult);

		Claims claims = parseClaims(token);

		assertThat(claims.getExpiration()).isNotNull();
		assertThat(claims.getExpiration().toInstant()).isAfter(claims.getIssuedAt().toInstant());
	}

	@Test
	void parseAndValidate_acceptsValidToken() {
		String token = jwtService.generateAccessToken(authenticationResult);

		JwtAuthenticatedPrincipal principal = jwtService.parseAndValidate(token);

		assertThat(principal.userId()).isEqualTo(USER_ID);
		assertThat(principal.organizationId()).isEqualTo(ORGANIZATION_ID);
		assertThat(principal.email()).isEqualTo(EMAIL);
	}

	@Test
	void parseAndValidate_rejectsExpiredToken() {
		Clock issuedClock = Clock.fixed(Instant.parse("2020-01-01T00:00:00Z"), ZoneOffset.UTC);
		JwtService issuedJwtService = new JwtService(new JwtProperties(SECRET, Duration.ofSeconds(60)), issuedClock);
		ReflectionTestUtils.invokeMethod(issuedJwtService, "initSigningKey");
		String token = issuedJwtService.generateAccessToken(authenticationResult);

		Clock expiredClock = Clock.fixed(Instant.parse("2020-01-01T00:05:00Z"), ZoneOffset.UTC);
		JwtService expiredJwtService = new JwtService(new JwtProperties(SECRET, Duration.ofSeconds(60)), expiredClock);
		ReflectionTestUtils.invokeMethod(expiredJwtService, "initSigningKey");

		assertThatThrownBy(() -> expiredJwtService.parseAndValidate(token))
				.isInstanceOf(InvalidJwtException.class);
	}

	@Test
	void parseAndValidate_rejectsInvalidSignature() {
		String token = jwtService.generateAccessToken(authenticationResult);

		JwtService otherSecretService = new JwtService(
				new JwtProperties("another-secret-not-for-production-min-32-chars", Duration.ofMinutes(15)),
				Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC)
		);
		ReflectionTestUtils.invokeMethod(otherSecretService, "initSigningKey");

		assertThatThrownBy(() -> otherSecretService.parseAndValidate(token))
				.isInstanceOf(InvalidJwtException.class);
	}

	@Test
	void parseAndValidate_rejectsMalformedToken() {
		assertThatThrownBy(() -> jwtService.parseAndValidate("not-a-jwt"))
				.isInstanceOf(InvalidJwtException.class);
	}

	private Claims parseClaims(String token) {
		SecretKey signingKey = (SecretKey) ReflectionTestUtils.getField(jwtService, "signingKey");
		Instant now = Instant.parse("2026-01-01T00:00:00Z");
		return Jwts.parser()
				.clock(() -> Date.from(now))
				.verifyWith(signingKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
}
