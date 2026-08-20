package pl.m2manager.tenant;

import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import pl.m2manager.security.jwt.JwtAuthenticationToken;

import java.util.UUID;

/**
 * Development and test tenant context.
 * Uses JWT organization when authenticated; otherwise falls back to configured default.
 */
@Component
@Profile({"dev", "test"})
public class ConfigurableTenantContext implements TenantContext {

	private final TenantProperties tenantProperties;

	public ConfigurableTenantContext(TenantProperties tenantProperties) {
		this.tenantProperties = tenantProperties;
	}

	@Override
	public UUID getCurrentOrganizationId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
			return jwtAuthenticationToken.getPrincipal().organizationId();
		}

		UUID organizationId = tenantProperties.defaultOrganizationId();
		if (organizationId == null) {
			throw new IllegalStateException(
					"app.tenant.default-organization-id must be configured for dev/test profile"
			);
		}
		return organizationId;
	}
}
