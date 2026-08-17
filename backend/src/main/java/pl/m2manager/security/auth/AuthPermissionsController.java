package pl.m2manager.security.auth;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.m2manager.security.auth.dto.PermissionsResponse;
import pl.m2manager.security.authorization.EffectivePermissionService;
import pl.m2manager.security.jwt.JwtAuthenticatedPrincipal;
import pl.m2manager.security.jwt.JwtAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthPermissionsController {

	private final EffectivePermissionService effectivePermissionService;

	public AuthPermissionsController(EffectivePermissionService effectivePermissionService) {
		this.effectivePermissionService = effectivePermissionService;
	}

	@GetMapping("/permissions")
	@PreAuthorize("isAuthenticated()")
	public PermissionsResponse getPermissions() {
		JwtAuthenticatedPrincipal principal = requireAuthenticatedPrincipal();
		List<String> permissions = new ArrayList<>(effectivePermissionService.resolvePermissionCodes(
				principal.userId(),
				principal.organizationId()
		));
		permissions.sort(String::compareTo);
		return new PermissionsResponse(permissions);
	}

	private JwtAuthenticatedPrincipal requireAuthenticatedPrincipal() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
			return jwtAuthenticationToken.getPrincipal();
		}
		throw new IllegalStateException("Authenticated JWT principal required");
	}
}
