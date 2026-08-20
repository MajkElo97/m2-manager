package pl.m2manager.security.auth;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.m2manager.organization.entity.Organization;
import pl.m2manager.organization.repository.OrganizationRepository;
import pl.m2manager.security.auth.dto.OrganizationSummary;
import pl.m2manager.user.repository.UserOrganizationRepository;
import pl.m2manager.user.repository.UserRepository;
import pl.m2manager.user.repository.UserRoleRepository;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class OrganizationAccessService {

	private final UserOrganizationRepository userOrganizationRepository;
	private final UserRoleRepository userRoleRepository;
	private final OrganizationRepository organizationRepository;
	private final UserRepository userRepository;

	public OrganizationAccessService(
			UserOrganizationRepository userOrganizationRepository,
			UserRoleRepository userRoleRepository,
			OrganizationRepository organizationRepository,
			UserRepository userRepository
	) {
		this.userOrganizationRepository = userOrganizationRepository;
		this.userRoleRepository = userRoleRepository;
		this.organizationRepository = organizationRepository;
		this.userRepository = userRepository;
	}

	public boolean isSuperAdmin(UUID userId) {
		return userRoleRepository.hasSuperAdminRole(userId);
	}

	public boolean canAccessOrganization(UUID userId, UUID organizationId) {
		if (organizationRepository.findById(organizationId).isEmpty()) {
			return false;
		}
		if (isSuperAdmin(userId)) {
			return true;
		}
		if (userOrganizationRepository.existsByIdUserIdAndIdOrganizationId(userId, organizationId)) {
			return true;
		}
		return userRepository.findById(userId)
				.map(user -> user.getOrganization().getId().equals(organizationId))
				.orElse(false);
	}

	public void requireOrganizationAccess(UUID userId, UUID organizationId) {
		if (!canAccessOrganization(userId, organizationId)) {
			throw new AccessDeniedException("Organization access denied");
		}
	}

	public List<OrganizationSummary> findAvailableOrganizations(UUID userId) {
		List<Organization> organizations;
		if (isSuperAdmin(userId)) {
			organizations = organizationRepository.findAll();
		} else {
			Set<UUID> organizationIds = new HashSet<>(userOrganizationRepository.findOrganizationIdsByUserId(userId));
			userRepository.findById(userId)
					.ifPresent(user -> organizationIds.add(user.getOrganization().getId()));
			organizations = organizationRepository.findAllById(organizationIds);
		}

		return organizations.stream()
				.sorted(Comparator.comparing(Organization::getName, String.CASE_INSENSITIVE_ORDER))
				.map(this::toSummary)
				.toList();
	}

	public OrganizationSummary toSummary(Organization organization) {
		return new OrganizationSummary(organization.getId(), organization.getName(), organization.getSlug());
	}
}
