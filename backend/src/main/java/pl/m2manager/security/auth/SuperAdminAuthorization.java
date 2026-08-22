package pl.m2manager.security.auth;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import pl.m2manager.security.jwt.JwtAuthenticatedPrincipal;

@Component
public class SuperAdminAuthorization {

	private final OrganizationAccessService organizationAccessService;

	public SuperAdminAuthorization(OrganizationAccessService organizationAccessService) {
		this.organizationAccessService = organizationAccessService;
	}

	public void requireSuperAdmin(JwtAuthenticatedPrincipal principal) {
		requireSuperAdmin(principal.userId());
	}

	public void requireSuperAdmin(java.util.UUID userId) {
		if (!organizationAccessService.isSuperAdmin(userId)) {
			throw new AccessDeniedException("Super admin access required");
		}
	}
}
