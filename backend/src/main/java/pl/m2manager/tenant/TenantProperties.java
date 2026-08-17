package pl.m2manager.tenant;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.UUID;

@ConfigurationProperties(prefix = "app.tenant")
public record TenantProperties(UUID defaultOrganizationId) {
}
