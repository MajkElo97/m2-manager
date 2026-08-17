package pl.m2manager.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import pl.m2manager.security.auth.dto.AuthenticationResult;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

	static final String ORGANIZATION_ID_CLAIM = "organization_id";
	static final String EMAIL_CLAIM = "email";

	private final JwtProperties jwtProperties;
	private final Clock clock;
	private SecretKey signingKey;

	public JwtService(JwtProperties jwtProperties, Clock clock) {
		this.jwtProperties = jwtProperties;
		this.clock = clock;
	}

	@PostConstruct
	void initSigningKey() {
		byte[] secretBytes = jwtProperties.secret().getBytes(StandardCharsets.UTF_8);
		if (secretBytes.length < 32) {
			throw new IllegalStateException("security.jwt.secret must be at least 32 bytes for HS256");
		}
		this.signingKey = Keys.hmacShaKeyFor(secretBytes);
	}

	public String generateAccessToken(AuthenticationResult authenticationResult) {
		Instant issuedAt = clock.instant();
		Instant expiresAt = issuedAt.plus(jwtProperties.accessTokenExpiration());

		return Jwts.builder()
				.subject(authenticationResult.userId().toString())
				.claim(ORGANIZATION_ID_CLAIM, authenticationResult.organizationId().toString())
				.claim(EMAIL_CLAIM, authenticationResult.email())
				.issuedAt(Date.from(issuedAt))
				.expiration(Date.from(expiresAt))
				.signWith(signingKey, Jwts.SIG.HS256)
				.compact();
	}

	public JwtAuthenticatedPrincipal parseAndValidate(String token) {
		try {
			Claims claims = Jwts.parser()
					.clock(() -> Date.from(clock.instant()))
					.verifyWith(signingKey)
					.build()
					.parseSignedClaims(token)
					.getPayload();

			return new JwtAuthenticatedPrincipal(
					UUID.fromString(claims.getSubject()),
					UUID.fromString(claims.get(ORGANIZATION_ID_CLAIM, String.class)),
					claims.get(EMAIL_CLAIM, String.class)
			);
		} catch (ExpiredJwtException ex) {
			throw new InvalidJwtException("JWT expired", ex);
		} catch (JwtException | IllegalArgumentException ex) {
			throw new InvalidJwtException("Invalid JWT", ex);
		}
	}

	public long accessTokenExpirationSeconds() {
		return jwtProperties.accessTokenExpiration().toSeconds();
	}
}
