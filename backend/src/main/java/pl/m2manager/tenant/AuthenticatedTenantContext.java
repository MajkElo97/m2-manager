package pl.m2manager.tenant;

import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import pl.m2manager.security.jwt.JwtAuthenticatedPrincipal;
import pl.m2manager.security.jwt.JwtAuthenticationToken;

import java.util.UUID;

/**
 * Production tenant context resolved from the authenticated JWT principal.
 */
@Component
@Profile("!dev & !test")
public class AuthenticatedTenantContext implements TenantContext {

	@Override
	public UUID getCurrentOrganizationId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
			JwtAuthenticatedPrincipal principal = jwtAuthenticationToken.getPrincipal();
			return principal.organizationId();
		}
		throw new IllegalStateException("Tenant context requires authenticated JWT principal");
	}
}
