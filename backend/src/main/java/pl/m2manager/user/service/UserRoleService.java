package pl.m2manager.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.m2manager.common.exception.ResourceNotFoundException;
import pl.m2manager.role.dto.RoleResponse;
import pl.m2manager.role.entity.Role;
import pl.m2manager.role.repository.RoleRepository;
import pl.m2manager.role.service.RoleService;
import pl.m2manager.tenant.TenantContext;
import pl.m2manager.user.entity.User;
import pl.m2manager.user.entity.UserRole;
import pl.m2manager.user.entity.UserRoleId;
import pl.m2manager.user.repository.UserRepository;
import pl.m2manager.user.repository.UserRoleRepository;

import java.util.List;
import java.util.UUID;

/**
 * Assigns tenant-scoped roles to users within the authenticated organization.
 */
@Service
@Transactional(readOnly = true)
public class UserRoleService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final UserRoleRepository userRoleRepository;
	private final RoleService roleService;
	private final TenantContext tenantContext;

	public UserRoleService(
			UserRepository userRepository,
			RoleRepository roleRepository,
			UserRoleRepository userRoleRepository,
			RoleService roleService,
			TenantContext tenantContext
	) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.userRoleRepository = userRoleRepository;
		this.roleService = roleService;
		this.tenantContext = tenantContext;
	}

	@Transactional
	public void assignRole(UUID userId, UUID roleId) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		requireUserInCurrentOrganization(userId, organizationId);
		requireRoleInCurrentOrganization(roleId, organizationId);

		UserRoleId id = new UserRoleId(userId, roleId);
		if (userRoleRepository.existsById(id)) {
			return;
		}
		userRoleRepository.save(new UserRole(organizationId, userId, roleId));
	}

	@Transactional
	public void removeRole(UUID userId, UUID roleId) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		requireUserInCurrentOrganization(userId, organizationId);
		requireRoleInCurrentOrganization(roleId, organizationId);
		userRoleRepository.deleteById(new UserRoleId(userId, roleId));
	}

	public List<RoleResponse> listRoles(UUID userId) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		requireUserInCurrentOrganization(userId, organizationId);
		return roleRepository.findRolesByUserIdAndOrganizationId(userId, organizationId).stream()
				.map(roleService::toResponse)
				.toList();
	}

	private User requireUserInCurrentOrganization(UUID userId, UUID organizationId) {
		return userRepository.findByIdAndOrganizationId(userId, organizationId)
				.orElseThrow(() -> new ResourceNotFoundException("User", userId));
	}

	private Role requireRoleInCurrentOrganization(UUID roleId, UUID organizationId) {
		return roleRepository.findByIdAndOrganizationId(roleId, organizationId)
				.orElseThrow(() -> new ResourceNotFoundException("Role", roleId));
	}
}
