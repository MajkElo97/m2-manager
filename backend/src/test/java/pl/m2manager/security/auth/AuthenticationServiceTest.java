package pl.m2manager.security.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.m2manager.security.auth.dto.AuthenticationResult;
import pl.m2manager.user.entity.User;
import pl.m2manager.user.repository.UserRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

	private static final UUID USER_ID = UUID.fromString("b0000000-0000-4000-8000-000000000001");
	private static final UUID ORGANIZATION_ID = UUID.fromString("a0000000-0000-4000-8000-000000000002");
	private static final String EMAIL = "john@example.com";
	private static final String PASSWORD = "passwordA";
	private static final String PASSWORD_HASH = "$2a$12$encoded-password-hash";
	private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

	@Mock
	private M2UserDetailsService userDetailsService;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private UserRepository userRepository;

	@Mock
	private Clock clock;

	@InjectMocks
	private AuthenticationService authenticationService;

	private AuthenticatedUser authenticatedUser;

	@BeforeEach
	void setUp() {
		authenticatedUser = new AuthenticatedUser(USER_ID, ORGANIZATION_ID, EMAIL, PASSWORD_HASH, true);
	}

	@Test
	void authenticate_validCredentialsReturnsAuthenticationResult() {
		User user = new User();
		when(userDetailsService.loadByOrganizationSlugAndEmail("org-a", EMAIL)).thenReturn(authenticatedUser);
		when(passwordEncoder.matches(PASSWORD, PASSWORD_HASH)).thenReturn(true);
		when(clock.instant()).thenReturn(NOW);
		when(userRepository.findByIdAndOrganizationId(USER_ID, ORGANIZATION_ID)).thenReturn(Optional.of(user));

		AuthenticationResult result = authenticationService.authenticate("org-a", EMAIL, PASSWORD);

		assertThat(result.userId()).isEqualTo(USER_ID);
		assertThat(result.organizationId()).isEqualTo(ORGANIZATION_ID);
		assertThat(result.email()).isEqualTo(EMAIL);
		assertThat(user.getLastLoginAt()).isEqualTo(NOW);
		verify(userDetailsService).loadByOrganizationSlugAndEmail("org-a", EMAIL);
		verify(passwordEncoder).matches(PASSWORD, PASSWORD_HASH);
	}

	@Test
	void authenticate_wrongPasswordFails() {
		when(userDetailsService.loadByOrganizationSlugAndEmail("org-a", EMAIL)).thenReturn(authenticatedUser);
		when(passwordEncoder.matches("wrong-password", PASSWORD_HASH)).thenReturn(false);

		assertThatThrownBy(() -> authenticationService.authenticate("org-a", EMAIL, "wrong-password"))
				.isInstanceOf(BadCredentialsException.class)
				.hasMessage(M2UserDetailsService.INVALID_CREDENTIALS_MESSAGE);

		verify(userRepository, never()).findByIdAndOrganizationId(any(), any());
	}

	@Test
	void authenticate_unknownOrganizationFails() {
		when(userDetailsService.loadByOrganizationSlugAndEmail("unknown-slug", EMAIL))
				.thenThrow(new UsernameNotFoundException(M2UserDetailsService.INVALID_CREDENTIALS_MESSAGE));

		assertThatThrownBy(() -> authenticationService.authenticate("unknown-slug", EMAIL, PASSWORD))
				.isInstanceOf(BadCredentialsException.class)
				.hasMessage(M2UserDetailsService.INVALID_CREDENTIALS_MESSAGE);
	}

	@Test
	void authenticate_unknownUserFails() {
		when(userDetailsService.loadByOrganizationSlugAndEmail("org-a", "missing@example.com"))
				.thenThrow(new UsernameNotFoundException(M2UserDetailsService.INVALID_CREDENTIALS_MESSAGE));

		assertThatThrownBy(() -> authenticationService.authenticate("org-a", "missing@example.com", PASSWORD))
				.isInstanceOf(BadCredentialsException.class)
				.hasMessage(M2UserDetailsService.INVALID_CREDENTIALS_MESSAGE);
	}

	@Test
	void authenticate_inactiveUserFails() {
		AuthenticatedUser inactiveUser = new AuthenticatedUser(USER_ID, ORGANIZATION_ID, EMAIL, PASSWORD_HASH, false);
		when(userDetailsService.loadByOrganizationSlugAndEmail("org-a", EMAIL)).thenReturn(inactiveUser);

		assertThatThrownBy(() -> authenticationService.authenticate("org-a", EMAIL, PASSWORD))
				.isInstanceOf(BadCredentialsException.class)
				.hasMessage(M2UserDetailsService.INVALID_CREDENTIALS_MESSAGE);

		verify(userRepository, never()).findByIdAndOrganizationId(any(), any());
	}
}
