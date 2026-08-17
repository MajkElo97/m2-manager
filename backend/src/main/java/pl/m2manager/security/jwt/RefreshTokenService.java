package pl.m2manager.security.jwt;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.m2manager.security.auth.dto.AuthenticationResult;
import pl.m2manager.user.entity.User;
import pl.m2manager.user.repository.UserRepository;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class RefreshTokenService {

	private static final int RAW_TOKEN_BYTE_LENGTH = 32;

	private final RefreshTokenRepository refreshTokenRepository;
	private final RefreshTokenHasher refreshTokenHasher;
	private final UserRepository userRepository;
	private final JwtProperties jwtProperties;
	private final Clock clock;
	private final SecureRandom secureRandom;

	public RefreshTokenService(
			RefreshTokenRepository refreshTokenRepository,
			RefreshTokenHasher refreshTokenHasher,
			UserRepository userRepository,
			JwtProperties jwtProperties,
			Clock clock
	) {
		this.refreshTokenRepository = refreshTokenRepository;
		this.refreshTokenHasher = refreshTokenHasher;
		this.userRepository = userRepository;
		this.jwtProperties = jwtProperties;
		this.clock = clock;
		this.secureRandom = new SecureRandom();
	}

	@Transactional
	public IssuedRefreshToken issueNewFamily(UUID userId, UUID organizationId) {
		UUID familyId = UUID.randomUUID();
		return persistToken(userId, organizationId, familyId, generateRawToken());
	}

	@Transactional(noRollbackFor = InvalidRefreshTokenException.class)
	public RefreshRotationResult rotate(String rawRefreshToken) {
		if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
			throw new InvalidRefreshTokenException();
		}

		String tokenHash = refreshTokenHasher.hash(rawRefreshToken);
		RefreshToken token = refreshTokenRepository.findByTokenHashForUpdate(tokenHash)
				.orElseThrow(InvalidRefreshTokenException::new);

		if (token.getRevokedAt() != null) {
			handleReuse(token);
			throw new InvalidRefreshTokenException();
		}

		Instant now = clock.instant();
		if (!token.getExpiresAt().isAfter(now)) {
			throw new InvalidRefreshTokenException();
		}

		User user = userRepository.findByIdAndOrganizationId(token.getUserId(), token.getOrganizationId())
				.orElseThrow(InvalidRefreshTokenException::new);

		if (!user.isActive()) {
			throw new InvalidRefreshTokenException();
		}

		token.setRevokedAt(now);

		String newRawToken = generateRawToken();
		RefreshToken replacement = createTokenEntity(
				user.getId(),
				token.getOrganizationId(),
				token.getFamilyId(),
				newRawToken,
				now
		);
		replacement = refreshTokenRepository.saveAndFlush(replacement);

		token.setReplacedByTokenId(replacement.getId());
		refreshTokenRepository.saveAndFlush(token);

		AuthenticationResult authenticationResult = new AuthenticationResult(
				user.getId(),
				token.getOrganizationId(),
				user.getEmail()
		);
		return new RefreshRotationResult(authenticationResult, newRawToken);
	}

	@Transactional
	public void revoke(String rawRefreshToken) {
		if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
			return;
		}

		String tokenHash = refreshTokenHasher.hash(rawRefreshToken);
		refreshTokenRepository.findByTokenHashForUpdate(tokenHash).ifPresent(token -> {
			if (token.getRevokedAt() == null) {
				token.setRevokedAt(clock.instant());
				refreshTokenRepository.save(token);
			}
		});
	}

	public String generateRawToken() {
		byte[] bytes = new byte[RAW_TOKEN_BYTE_LENGTH];
		secureRandom.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	public String hashRawToken(String rawRefreshToken) {
		return refreshTokenHasher.hash(rawRefreshToken);
	}

	public Instant calculateExpiration(Instant issuedAt) {
		return issuedAt.plus(jwtProperties.refreshTokenExpiration());
	}

	private IssuedRefreshToken persistToken(UUID userId, UUID organizationId, UUID familyId, String rawToken) {
		Instant now = clock.instant();
		RefreshToken entity = createTokenEntity(userId, organizationId, familyId, rawToken, now);
		refreshTokenRepository.save(entity);
		return new IssuedRefreshToken(rawToken);
	}

	private RefreshToken createTokenEntity(
			UUID userId,
			UUID organizationId,
			UUID familyId,
			String rawToken,
			Instant now
	) {
		RefreshToken entity = new RefreshToken();
		entity.setUserId(userId);
		entity.setOrganizationId(organizationId);
		entity.setTokenHash(refreshTokenHasher.hash(rawToken));
		entity.setFamilyId(familyId);
		entity.setCreatedAt(now);
		entity.setExpiresAt(now.plus(jwtProperties.refreshTokenExpiration()));
		return entity;
	}

	private void handleReuse(RefreshToken token) {
		Instant now = clock.instant();
		if (token.getReuseDetectedAt() == null) {
			token.setReuseDetectedAt(now);
			refreshTokenRepository.saveAndFlush(token);
		}
		refreshTokenRepository.revokeActiveByFamilyId(token.getFamilyId(), now);
	}
}
