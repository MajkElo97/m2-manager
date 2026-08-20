package pl.m2manager.security.auth.dto;

import java.util.List;

public record AuthContextResponse(
		AuthUserSummary user,
		OrganizationSummary activeOrganization,
		List<OrganizationSummary> availableOrganizations,
		boolean canSwitchOrganizations
) {
}
