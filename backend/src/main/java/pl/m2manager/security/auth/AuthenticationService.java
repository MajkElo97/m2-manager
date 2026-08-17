package pl.m2manager.security.auth;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.m2manager.security.auth.dto.AuthenticationResult;
import pl.m2manager.user.repository.UserRepository;

import java.time.Clock;
import java.util.UUID;

@Service
public class AuthenticationService {

	private final M2UserDetailsService userDetailsService;
	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepository;
	private final Clock clock;

	public AuthenticationService(
			M2UserDetailsService userDetailsService,
			PasswordEncoder passwordEncoder,
			UserRepository userRepository,
			Clock clock
	) {
		this.userDetailsService = userDetailsService;
		this.passwordEncoder = passwordEncoder;
		this.userRepository = userRepository;
		this.clock = clock;
	}

	@Transactional
	public AuthenticationResult authenticate(String organizationSlug, String email, String password) {
		AuthenticatedUser authenticatedUser = loadUser(organizationSlug, email);

		if (!authenticatedUser.isEnabled() || !passwordEncoder.matches(password, authenticatedUser.getPassword())) {
			throw new BadCredentialsException(M2UserDetailsService.INVALID_CREDENTIALS_MESSAGE);
		}

		updateLastLoginAt(authenticatedUser.getUserId(), authenticatedUser.getOrganizationId());

		new UsernamePasswordAuthenticationToken(
				authenticatedUser,
				null,
				authenticatedUser.getAuthorities()
		);

		return new AuthenticationResult(
				authenticatedUser.getUserId(),
				authenticatedUser.getOrganizationId(),
				authenticatedUser.getEmail()
		);
	}

	private void updateLastLoginAt(UUID userId, UUID organizationId) {
		userRepository.findByIdAndOrganizationId(userId, organizationId).ifPresent(user ->
				user.setLastLoginAt(clock.instant())
		);
	}

	private AuthenticatedUser loadUser(String organizationSlug, String email) {
		try {
			return userDetailsService.loadByOrganizationSlugAndEmail(organizationSlug, email);
		} catch (UsernameNotFoundException ex) {
			throw new BadCredentialsException(M2UserDetailsService.INVALID_CREDENTIALS_MESSAGE);
		}
	}
}
