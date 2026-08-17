package pl.m2manager.tenant;

import java.util.UUID;

/**
 * Provides the current tenant (organization) identifier for the active request.
 * Production implementation will resolve the value from the authenticated user.
 */
public interface TenantContext {

	UUID getCurrentOrganizationId();
}
