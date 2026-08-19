package pl.m2manager.role.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.m2manager.common.exception.BusinessConflictException;
import pl.m2manager.common.exception.ResourceNotFoundException;
import pl.m2manager.permission.dto.PermissionResponse;
import pl.m2manager.permission.entity.Permission;
import pl.m2manager.permission.mapper.PermissionMapper;
import pl.m2manager.permission.service.PermissionService;
import pl.m2manager.role.entity.Role;
import pl.m2manager.role.entity.RolePermission;
import pl.m2manager.role.entity.RolePermissionId;
import pl.m2manager.role.repository.RolePermissionRepository;
import pl.m2manager.tenant.TenantContext;

import java.util.List;
import java.util.UUID;

/**
 * Assigns global permissions to tenant-scoped roles.
 * Roles are always resolved within the current organization from {@link TenantContext}.
 */
@Service
@Transactional(readOnly = true)
public class RolePermissionService {

	private final RoleService roleService;
	private final PermissionService permissionService;
	private final RolePermissionRepository rolePermissionRepository;
	private final PermissionMapper permissionMapper;
	private final TenantContext tenantContext;

	public RolePermissionService(
			RoleService roleService,
			PermissionService permissionService,
			RolePermissionRepository rolePermissionRepository,
			PermissionMapper permissionMapper,
			TenantContext tenantContext
	) {
		this.roleService = roleService;
		this.permissionService = permissionService;
		this.rolePermissionRepository = rolePermissionRepository;
		this.permissionMapper = permissionMapper;
		this.tenantContext = tenantContext;
	}

	@Transactional
	public void assignPermission(UUID roleId, String permissionCode) {
		Role role = roleService.requireRoleInCurrentOrganization(roleId);
		Permission permission = permissionService.findPermissionEntityByCode(permissionCode);
		RolePermissionId id = new RolePermissionId(role.getId(), permission.getId());
		if (rolePermissionRepository.existsById(id)) {
			return;
		}
		rolePermissionRepository.save(new RolePermission(role.getId(), permission.getId()));
	}

	@Transactional
	public void removePermission(UUID roleId, String permissionCode) {
		roleService.requireRoleInCurrentOrganization(roleId);
		Permission permission = permissionService.findPermissionEntityByCode(permissionCode);
		rolePermissionRepository.deleteById(new RolePermissionId(roleId, permission.getId()));
	}

	@Transactional
	public void replacePermissions(UUID roleId, List<String> permissionCodes) {
		Role role = roleService.requireRoleInCurrentOrganization(roleId);
		assertSystemRolePermissionsProtected(role);

		rolePermissionRepository.deleteByIdRoleId(roleId);

		for (String permissionCode : permissionCodes) {
			Permission permission = permissionService.findPermissionEntityByCode(permissionCode);
			RolePermissionId id = new RolePermissionId(role.getId(), permission.getId());
			if (!rolePermissionRepository.existsById(id)) {
				rolePermissionRepository.save(new RolePermission(role.getId(), permission.getId()));
			}
		}
	}

	private void assertSystemRolePermissionsProtected(Role role) {
		if (role.isSystemRole()) {
			throw new BusinessConflictException("Cannot modify permissions of a system role");
		}
	}

	public List<PermissionResponse> listPermissions(UUID roleId) {
		roleService.requireRoleInCurrentOrganization(roleId);
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		return rolePermissionRepository.findPermissionsByRoleIdAndOrganizationId(roleId, organizationId).stream()
				.map(permissionMapper::toResponse)
				.toList();
	}
}
