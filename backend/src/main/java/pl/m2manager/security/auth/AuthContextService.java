package pl.m2manager.security.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.m2manager.common.exception.ResourceNotFoundException;
import pl.m2manager.organization.entity.Organization;
import pl.m2manager.organization.repository.OrganizationRepository;
import pl.m2manager.security.auth.dto.AuthContextResponse;
import pl.m2manager.security.auth.dto.AuthUserSummary;
import pl.m2manager.security.auth.dto.OrganizationSummary;
import pl.m2manager.security.jwt.JwtAuthenticatedPrincipal;
import pl.m2manager.user.entity.User;
import pl.m2manager.user.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AuthContextService {

	private final UserRepository userRepository;
	private final OrganizationRepository organizationRepository;
	private final OrganizationAccessService organizationAccessService;

	public AuthContextService(
			UserRepository userRepository,
			OrganizationRepository organizationRepository,
			OrganizationAccessService organizationAccessService
	) {
		this.userRepository = userRepository;
		this.organizationRepository = organizationRepository;
		this.organizationAccessService = organizationAccessService;
	}

	public AuthContextResponse getContext(JwtAuthenticatedPrincipal principal) {
		User user = requireUser(principal.userId());
		Organization activeOrganization = requireOrganization(principal.organizationId());
		List<OrganizationSummary> availableOrganizations =
				organizationAccessService.findAvailableOrganizations(principal.userId());

		return new AuthContextResponse(
				toUserSummary(user),
				organizationAccessService.toSummary(activeOrganization),
				availableOrganizations,
				availableOrganizations.size() > 1,
				user.isMustChangePassword(),
				organizationAccessService.isSuperAdmin(principal.userId())
		);
	}

	public AuthUserSummary toUserSummary(User user) {
		return new AuthUserSummary(user.getId(), resolveDisplayName(user), user.getEmail());
	}

	public static String resolveDisplayName(User user) {
		String firstName = user.getFirstName();
		String lastName = user.getLastName();
		boolean hasFirst = firstName != null && !firstName.isBlank();
		boolean hasLast = lastName != null && !lastName.isBlank();

		if (hasFirst && hasLast) {
			return firstName.trim() + " " + lastName.trim();
		}
		if (hasFirst) {
			return firstName.trim();
		}
		if (hasLast) {
			return lastName.trim();
		}
		return user.getEmail();
	}

	private User requireUser(UUID userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}

	private Organization requireOrganization(UUID organizationId) {
		return organizationRepository.findById(organizationId)
				.orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
	}
}
