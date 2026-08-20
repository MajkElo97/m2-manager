package pl.m2manager.security.authorization;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.m2manager.permission.repository.PermissionRepository;
import pl.m2manager.user.repository.UserRoleRepository;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Resolves the effective permission set for a user within an organization.
 *
 * <p>Uses a single tenant-scoped JPQL query joining user_roles → roles → role_permissions → permissions.
 * Only active roles belonging to the given organization participate.
 */
@Service
@Transactional(readOnly = true)
public class EffectivePermissionService {

	private final UserRoleRepository userRoleRepository;
	private final PermissionRepository permissionRepository;
	private final pl.m2manager.security.auth.OrganizationAccessService organizationAccessService;

	public EffectivePermissionService(
			UserRoleRepository userRoleRepository,
			PermissionRepository permissionRepository,
			pl.m2manager.security.auth.OrganizationAccessService organizationAccessService
	) {
		this.userRoleRepository = userRoleRepository;
		this.permissionRepository = permissionRepository;
		this.organizationAccessService = organizationAccessService;
	}

	public Set<String> resolvePermissionCodes(UUID userId, UUID organizationId) {
		if (organizationAccessService.isSuperAdmin(userId)) {
			return new HashSet<>(permissionRepository.findAllPermissionCodes());
		}
		return new HashSet<>(userRoleRepository.findEffectivePermissionCodesByUserIdAndOrganizationId(
				userId,
				organizationId
		));
	}

	public boolean hasEffectivePermission(UUID userId, UUID organizationId, String permissionCode) {
		Set<String> codes = resolvePermissionCodes(userId, organizationId);
		return EffectivePermissionEvaluator.hasPermission(codes, permissionCode);
	}
}
