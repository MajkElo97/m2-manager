package pl.m2manager.role.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.m2manager.permission.dto.PermissionResponse;
import pl.m2manager.role.dto.CreateRoleRequest;
import pl.m2manager.role.dto.RoleResponse;
import pl.m2manager.role.dto.UpdateRolePermissionsRequest;
import pl.m2manager.role.dto.UpdateRoleRequest;
import pl.m2manager.role.service.RolePermissionService;
import pl.m2manager.role.service.RoleService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

	private final RoleService roleService;
	private final RolePermissionService rolePermissionService;

	public RoleController(RoleService roleService, RolePermissionService rolePermissionService) {
		this.roleService = roleService;
		this.rolePermissionService = rolePermissionService;
	}

	@GetMapping
	@PreAuthorize("@authorizationService.hasPermission('ROLES_VIEW')")
	public List<RoleResponse> list() {
		return roleService.listRoles();
	}

	@GetMapping("/{id}")
	@PreAuthorize("@authorizationService.hasPermission('ROLES_VIEW')")
	public RoleResponse getById(@PathVariable UUID id) {
		return roleService.findById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@PreAuthorize("@authorizationService.hasPermission('ROLES_CREATE')")
	public RoleResponse create(@Valid @RequestBody CreateRoleRequest request) {
		return roleService.createRole(request);
	}

	@PutMapping("/{id}")
	@PreAuthorize("@authorizationService.hasPermission('ROLES_EDIT')")
	public RoleResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateRoleRequest request) {
		return roleService.updateRole(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("@authorizationService.hasPermission('ROLES_DELETE')")
	public void deactivate(@PathVariable UUID id) {
		roleService.deactivateRole(id);
	}

	@GetMapping("/{id}/permissions")
	@PreAuthorize("@authorizationService.hasPermission('ROLES_VIEW')")
	public List<PermissionResponse> listPermissions(@PathVariable UUID id) {
		return rolePermissionService.listPermissions(id);
	}

	@PutMapping("/{id}/permissions")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("@authorizationService.hasPermission('ROLES_EDIT')")
	public void replacePermissions(@PathVariable UUID id, @Valid @RequestBody UpdateRolePermissionsRequest request) {
		rolePermissionService.replacePermissions(id, request.permissionCodes());
	}
}
