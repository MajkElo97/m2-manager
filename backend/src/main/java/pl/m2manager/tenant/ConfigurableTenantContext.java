package pl.m2manager.tenant;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Development and test tenant context.
 * Reads organization ID from {@code app.tenant.default-organization-id} configuration.
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
		UUID organizationId = tenantProperties.defaultOrganizationId();
		if (organizationId == null) {
			throw new IllegalStateException(
					"app.tenant.default-organization-id must be configured for dev/test profile"
			);
		}
		return organizationId;
	}
}
