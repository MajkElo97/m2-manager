package pl.m2manager.security.authorization;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pl.m2manager.security.jwt.JwtAuthenticatedPrincipal;
import pl.m2manager.security.jwt.JwtAuthenticationToken;

import java.util.Optional;
import java.util.Set;

/**
 * Authorization facade for Spring Security method expressions and application code.
 * User and organization identity is always derived from the authenticated JWT principal.
 */
@Service("authorizationService")
public class AuthorizationService {

	private final EffectivePermissionService effectivePermissionService;

	public AuthorizationService(EffectivePermissionService effectivePermissionService) {
		this.effectivePermissionService = effectivePermissionService;
	}

	public boolean hasPermission(String permissionCode) {
		return resolveEffectiveCodes()
				.map(codes -> EffectivePermissionEvaluator.hasPermission(codes, permissionCode))
				.orElse(false);
	}

	public boolean hasAnyPermission(String... permissionCodes) {
		return resolveEffectiveCodes()
				.map(codes -> EffectivePermissionEvaluator.hasAnyPermission(codes, permissionCodes))
				.orElse(false);
	}

	public boolean hasAllPermissions(String... permissionCodes) {
		return resolveEffectiveCodes()
				.map(codes -> EffectivePermissionEvaluator.hasAllPermissions(codes, permissionCodes))
				.orElse(false);
	}

	public boolean hasModuleAdmin(String module) {
		return resolveEffectiveCodes()
				.map(codes -> EffectivePermissionEvaluator.hasModuleAdmin(codes, module))
				.orElse(false);
	}

	private Optional<Set<String>> resolveEffectiveCodes() {
		return getAuthenticatedPrincipal().map(principal -> effectivePermissionService.resolvePermissionCodes(
				principal.userId(),
				principal.organizationId()
		));
	}

	private Optional<JwtAuthenticatedPrincipal> getAuthenticatedPrincipal() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken
				&& authentication.isAuthenticated()) {
			return Optional.of(jwtAuthenticationToken.getPrincipal());
		}
		return Optional.empty();
	}
}
