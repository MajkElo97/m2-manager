package pl.m2manager.tenant;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Production placeholder until the Users/Authentication module is implemented.
 * Resolving tenant context requires an authenticated user session.
 */
@Component
@Profile("!dev & !test")
public class UnauthenticatedTenantContext implements TenantContext {

	@Override
	public UUID getCurrentOrganizationId() {
		throw new IllegalStateException(
				"Tenant context is not available without authentication. "
						+ "Implement user-based tenant resolution before using this profile."
		);
	}
}
