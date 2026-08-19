package pl.m2manager.role.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.m2manager.common.exception.BusinessConflictException;
import pl.m2manager.common.exception.ResourceNotFoundException;
import pl.m2manager.organization.entity.Organization;
import pl.m2manager.organization.repository.OrganizationRepository;
import pl.m2manager.role.dto.CreateRoleRequest;
import pl.m2manager.role.dto.RoleResponse;
import pl.m2manager.role.dto.UpdateRoleRequest;
import pl.m2manager.role.entity.Role;
import pl.m2manager.role.mapper.RoleMapper;
import pl.m2manager.role.repository.RolePermissionRepository;
import pl.m2manager.role.repository.RoleRepository;
import pl.m2manager.tenant.TenantContext;

import java.util.List;
import java.util.UUID;

/**
 * Tenant-scoped role management. Organization is always taken from {@link TenantContext};
 * callers cannot supply an organization identifier.
 *
 * <p>System roles ({@code systemRole=true}) are protected from identity changes and deactivation.
 * Full system-role lifecycle policy will be implemented in a later provisioning phase.
 */
@Service
@Transactional(readOnly = true)
public class RoleService {

	private final RoleRepository roleRepository;
	private final RolePermissionRepository rolePermissionRepository;
	private final OrganizationRepository organizationRepository;
	private final TenantContext tenantContext;
	private final RoleMapper roleMapper;

	public RoleService(
			RoleRepository roleRepository,
			RolePermissionRepository rolePermissionRepository,
			OrganizationRepository organizationRepository,
			TenantContext tenantContext,
			RoleMapper roleMapper
	) {
		this.roleRepository = roleRepository;
		this.rolePermissionRepository = rolePermissionRepository;
		this.organizationRepository = organizationRepository;
		this.tenantContext = tenantContext;
		this.roleMapper = roleMapper;
	}

	public RoleResponse findById(UUID roleId) {
		Role role = requireRoleInCurrentOrganization(roleId);
		return toResponse(role);
	}

	public List<RoleResponse> listRoles() {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		return roleRepository.findByOrganizationId(organizationId).stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional
	public RoleResponse createRole(CreateRoleRequest request) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		assertUniqueRoleName(organizationId, request.name(), null);

		Organization organization = organizationRepository.findById(organizationId)
				.orElseThrow(() -> new ResourceNotFoundException("Organization", organizationId));

		Role role = new Role();
		role.setOrganization(organization);
		role.setName(request.name());
		role.setDescription(request.description());

		try {
			return toResponse(roleRepository.saveAndFlush(role));
		}
		catch (DataIntegrityViolationException ex) {
			throw new BusinessConflictException("Role name already exists in organization");
		}
	}

	@Transactional
	public RoleResponse updateRole(UUID roleId, UpdateRoleRequest request) {
		Role role = requireRoleInCurrentOrganization(roleId);

		if (request.name() != null && !request.name().equals(role.getName())) {
			assertSystemRoleIdentityProtected(role, "change the name of");
			assertUniqueRoleName(role.getOrganization().getId(), request.name(), roleId);
			role.setName(request.name());
		}

		if (request.description() != null) {
			role.setDescription(request.description());
		}

		if (request.active() != null && request.active() != role.isActive()) {
			if (!request.active()) {
				assertSystemRoleIdentityProtected(role, "deactivate");
			}
			role.setActive(request.active());
		}

		try {
			return toResponse(roleRepository.save(role));
		}
		catch (DataIntegrityViolationException ex) {
			throw new BusinessConflictException("Role name already exists in organization");
		}
	}

	@Transactional
	public RoleResponse deactivateRole(UUID roleId) {
		Role role = requireRoleInCurrentOrganization(roleId);
		assertSystemRoleIdentityProtected(role, "deactivate");
		role.setActive(false);
		return toResponse(roleRepository.save(role));
	}

	public RoleResponse toResponse(Role role) {
		UUID organizationId = role.getOrganization().getId();
		long userCount = roleRepository.countUsersByRoleIdAndOrganizationId(role.getId(), organizationId);
		long permissionCount = rolePermissionRepository.countByIdRoleId(role.getId());
		return roleMapper.toResponse(role, userCount, permissionCount);
	}

	public Role requireRoleInCurrentOrganization(UUID roleId) {
		UUID organizationId = tenantContext.getCurrentOrganizationId();
		return roleRepository.findByIdAndOrganizationId(roleId, organizationId)
				.orElseThrow(() -> new ResourceNotFoundException("Role", roleId));
	}

	private void assertUniqueRoleName(UUID organizationId, String name, UUID excludeRoleId) {
		roleRepository.findByOrganizationIdAndName(organizationId, name).ifPresent(existing -> {
			if (excludeRoleId == null || !existing.getId().equals(excludeRoleId)) {
				throw new BusinessConflictException("Role name already exists in organization");
			}
		});
	}

	private void assertSystemRoleIdentityProtected(Role role, String action) {
		if (role.isSystemRole()) {
			throw new BusinessConflictException("Cannot " + action + " a system role");
		}
	}
}
