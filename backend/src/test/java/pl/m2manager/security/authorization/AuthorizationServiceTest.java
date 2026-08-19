package pl.m2manager.security.authorization;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import pl.m2manager.security.jwt.JwtAuthenticatedPrincipal;
import pl.m2manager.security.jwt.JwtAuthenticationToken;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

	@Mock
	private EffectivePermissionService effectivePermissionService;

	private AuthorizationService authorizationService;

	private final UUID userId = UUID.randomUUID();
	private final UUID organizationId = UUID.randomUUID();

	@BeforeEach
	void setUp() {
		authorizationService = new AuthorizationService(effectivePermissionService);
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void unauthenticatedRequest_returnsFalse() {
		assertThat(authorizationService.hasPermission("BUILDINGS_VIEW")).isFalse();
		assertThat(authorizationService.hasAnyPermission("BUILDINGS_VIEW")).isFalse();
		assertThat(authorizationService.hasAllPermissions("BUILDINGS_VIEW")).isFalse();
		assertThat(authorizationService.hasModuleAdmin("BUILDINGS")).isFalse();
	}

	@Test
	void hasPermission_delegatesToEffectivePermissions() {
		authenticate();
		when(effectivePermissionService.resolvePermissionCodes(userId, organizationId))
				.thenReturn(Set.of("BUILDINGS_VIEW"));

		assertThat(authorizationService.hasPermission("BUILDINGS_VIEW")).isTrue();
		assertThat(authorizationService.hasPermission("BUILDINGS_CREATE")).isFalse();
	}

	@Test
	void hasPermission_appliesAdminOverride() {
		authenticate();
		when(effectivePermissionService.resolvePermissionCodes(userId, organizationId))
				.thenReturn(Set.of("BUILDINGS_ADMIN"));

		assertThat(authorizationService.hasPermission("BUILDINGS_VIEW")).isTrue();
		assertThat(authorizationService.hasPermission("BUILDINGS_DELETE")).isTrue();
	}

	@Test
	void hasAnyPermission_returnsTrueWhenOneMatches() {
		authenticate();
		when(effectivePermissionService.resolvePermissionCodes(userId, organizationId))
				.thenReturn(Set.of("BUILDINGS_VIEW"));

		assertThat(authorizationService.hasAnyPermission("BUILDINGS_VIEW", "BUILDINGS_ADMIN")).isTrue();
		assertThat(authorizationService.hasAnyPermission("BUILDINGS_CREATE", "BUILDINGS_DELETE")).isFalse();
	}

	@Test
	void hasAllPermissions_requiresEveryPermission() {
		authenticate();
		when(effectivePermissionService.resolvePermissionCodes(userId, organizationId))
				.thenReturn(Set.of("BUILDINGS_VIEW", "BUILDINGS_EDIT"));

		assertThat(authorizationService.hasAllPermissions("BUILDINGS_VIEW", "BUILDINGS_EDIT")).isTrue();
		assertThat(authorizationService.hasAllPermissions("BUILDINGS_VIEW", "BUILDINGS_CREATE")).isFalse();
	}

	@Test
	void hasModuleAdmin_detectsAdminPermission() {
		authenticate();
		when(effectivePermissionService.resolvePermissionCodes(userId, organizationId))
				.thenReturn(Set.of("BUILDINGS_ADMIN"));

		assertThat(authorizationService.hasModuleAdmin("BUILDINGS")).isTrue();
		assertThat(authorizationService.hasModuleAdmin("SCHEDULE")).isFalse();
	}

	@Test
	void canListContacts_withBuildingId_requiresBuildingsView() {
		authenticate();
		when(effectivePermissionService.resolvePermissionCodes(userId, organizationId))
				.thenReturn(Set.of("BUILDINGS_VIEW"));

		assertThat(authorizationService.canListContacts(UUID.randomUUID())).isTrue();
		assertThat(authorizationService.canListContacts(null)).isFalse();
	}

	@Test
	void canListContacts_withoutBuildingId_requiresContactsView() {
		authenticate();
		when(effectivePermissionService.resolvePermissionCodes(userId, organizationId))
				.thenReturn(Set.of("CONTACTS_VIEW"));

		assertThat(authorizationService.canListContacts(null)).isTrue();
		assertThat(authorizationService.canListContacts(UUID.randomUUID())).isFalse();
	}

	private void authenticate() {
		JwtAuthenticatedPrincipal principal = new JwtAuthenticatedPrincipal(userId, organizationId, "user@example.com");
		SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(principal));
	}
}
