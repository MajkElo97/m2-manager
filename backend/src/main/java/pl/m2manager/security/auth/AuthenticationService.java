package pl.m2manager.security.auth;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.m2manager.security.auth.dto.AuthenticationResult;

@Service
public class AuthenticationService {

	private final M2UserDetailsService userDetailsService;
	private final PasswordEncoder passwordEncoder;

	public AuthenticationService(M2UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
		this.userDetailsService = userDetailsService;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional(readOnly = true)
	public AuthenticationResult authenticate(String organizationSlug, String email, String password) {
		AuthenticatedUser authenticatedUser = loadUser(organizationSlug, email);

		if (!authenticatedUser.isEnabled() || !passwordEncoder.matches(password, authenticatedUser.getPassword())) {
			throw new BadCredentialsException(M2UserDetailsService.INVALID_CREDENTIALS_MESSAGE);
		}

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

	private AuthenticatedUser loadUser(String organizationSlug, String email) {
		try {
			return userDetailsService.loadByOrganizationSlugAndEmail(organizationSlug, email);
		} catch (UsernameNotFoundException ex) {
			throw new BadCredentialsException(M2UserDetailsService.INVALID_CREDENTIALS_MESSAGE);
		}
	}
}
