package pl.m2manager.security.auth;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.m2manager.common.exception.BusinessConflictException;
import pl.m2manager.security.auth.dto.ChangePasswordRequest;
import pl.m2manager.security.jwt.JwtAuthenticatedPrincipal;
import pl.m2manager.security.jwt.RefreshTokenRepository;
import pl.m2manager.user.entity.User;
import pl.m2manager.user.repository.UserRepository;

import java.time.Clock;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@Transactional(readOnly = true)
public class AccountPasswordService {

	private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
	private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
	private static final Pattern DIGIT = Pattern.compile("[0-9]");
	private static final Pattern SPECIAL = Pattern.compile("[^A-Za-z0-9]");

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final RefreshTokenRepository refreshTokenRepository;
	private final Clock clock;

	public AccountPasswordService(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			RefreshTokenRepository refreshTokenRepository,
			Clock clock
	) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.refreshTokenRepository = refreshTokenRepository;
		this.clock = clock;
	}

	@Transactional
	public void changePassword(JwtAuthenticatedPrincipal principal, ChangePasswordRequest request) {
		if (!request.newPassword().equals(request.confirmPassword())) {
			throw new BusinessConflictException("Nowe hasło i potwierdzenie muszą być identyczne.");
		}

		validatePasswordStrength(request.newPassword());

		User user = userRepository.findById(principal.userId())
				.orElseThrow(() -> new BadCredentialsException(M2UserDetailsService.INVALID_CREDENTIALS_MESSAGE));

		if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
			throw new BadCredentialsException(M2UserDetailsService.INVALID_CREDENTIALS_MESSAGE);
		}

		if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
			throw new BusinessConflictException("Nowe hasło musi różnić się od aktualnego.");
		}

		user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
		boolean forcedChange = user.isMustChangePassword();
		user.setMustChangePassword(false);
		userRepository.save(user);

		if (!forcedChange) {
			refreshTokenRepository.revokeAllActiveByUserId(user.getId(), clock.instant());
		}
	}

	public boolean mustChangePassword(UUID userId) {
		return userRepository.findById(userId)
				.map(User::isMustChangePassword)
				.orElse(false);
	}

	public static void validatePasswordStrength(String password) {
		if (password == null || password.length() < 12) {
			throw new BusinessConflictException("Hasło musi mieć co najmniej 12 znaków.");
		}
		if (!UPPERCASE.matcher(password).find()) {
			throw new BusinessConflictException("Hasło musi zawierać wielką literę.");
		}
		if (!LOWERCASE.matcher(password).find()) {
			throw new BusinessConflictException("Hasło musi zawierać małą literę.");
		}
		if (!DIGIT.matcher(password).find()) {
			throw new BusinessConflictException("Hasło musi zawierać cyfrę.");
		}
		if (!SPECIAL.matcher(password).find()) {
			throw new BusinessConflictException("Hasło musi zawierać znak specjalny.");
		}
	}
}
