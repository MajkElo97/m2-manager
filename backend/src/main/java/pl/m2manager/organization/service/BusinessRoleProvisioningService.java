package pl.m2manager.organization.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.m2manager.organization.entity.Organization;
import pl.m2manager.permission.entity.Permission;
import pl.m2manager.permission.repository.PermissionRepository;
import pl.m2manager.role.entity.Role;
import pl.m2manager.role.entity.RolePermission;
import pl.m2manager.role.repository.RolePermissionRepository;
import pl.m2manager.role.repository.RoleRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provisions business roles for a new organization using the same permission mapping as dev seed V27.
 */
@Service
@Transactional
public class BusinessRoleProvisioningService {

	private static final List<String> ADMIN_MODULES = List.of(
			"DASHBOARD",
			"BUILDINGS",
			"STAIRCASES",
			"SCOPES",
			"ACTIVITIES",
			"EMPLOYEES",
			"MANAGERS",
			"SUPERVISORS",
			"CONTACTS",
			"USERS",
			"ROLES"
	);
	private static final List<String> CRUD_ACTIONS = List.of("VIEW", "CREATE", "EDIT", "DELETE");
	private static final List<String> BIURO_MODULES = List.of("DASHBOARD", "BUILDINGS", "EMPLOYEES");
	private static final List<String> KOORDYNATOR_MODULES = List.of("DASHBOARD", "BUILDINGS", "EMPLOYEES");

	private final RoleRepository roleRepository;
	private final RolePermissionRepository rolePermissionRepository;
	private final PermissionRepository permissionRepository;

	public BusinessRoleProvisioningService(
			RoleRepository roleRepository,
			RolePermissionRepository rolePermissionRepository,
			PermissionRepository permissionRepository
	) {
		this.roleRepository = roleRepository;
		this.rolePermissionRepository = rolePermissionRepository;
		this.permissionRepository = permissionRepository;
	}

	public Map<String, Role> provisionBusinessRoles(Organization organization) {
		Map<String, Role> roles = new LinkedHashMap<>();
		roles.put("ADMIN", createRole(organization, "ADMIN", "Administrator organizacji — pełny dostęp do modułów biznesowych"));
		roles.put("BIURO", createRole(organization, "BIURO", "Biuro — dashboard, budynki, pracownicy (EDIT)"));
		roles.put("KOORDYNATOR", createRole(organization, "KOORDYNATOR", "Koordynator — dashboard, budynki, pracownicy (READ)"));
		roles.put("PRACOWNIK", createRole(organization, "PRACOWNIK", "Pracownik — dashboard (READ)"));

		assignPermissions(roles.get("ADMIN"), resolveAdminPermissions());
		assignPermissions(roles.get("BIURO"), resolveBiuroPermissions());
		assignPermissions(roles.get("KOORDYNATOR"), resolveKoordynatorPermissions());
		assignPermissions(roles.get("PRACOWNIK"), resolvePracownikPermissions());

		return roles;
	}

	private Role createRole(Organization organization, String name, String description) {
		Role role = new Role();
		role.setOrganization(organization);
		role.setName(name);
		role.setDescription(description);
		role.setSystemRole(false);
		role.setActive(true);
		return roleRepository.save(role);
	}

	private void assignPermissions(Role role, List<Permission> permissions) {
		for (Permission permission : permissions) {
			rolePermissionRepository.save(new RolePermission(role.getId(), permission.getId()));
		}
	}

	private List<Permission> resolveAdminPermissions() {
		return permissionRepository.findByModulesAndActions(ADMIN_MODULES, CRUD_ACTIONS);
	}

	private List<Permission> resolveBiuroPermissions() {
		return permissionRepository.findByModulesAndActions(BIURO_MODULES, CRUD_ACTIONS);
	}

	private List<Permission> resolveKoordynatorPermissions() {
		List<Permission> permissions = new ArrayList<>();
		for (String module : KOORDYNATOR_MODULES) {
			permissions.addAll(permissionRepository.findByModuleAndAction(module, "VIEW"));
		}
		return permissions;
	}

	private List<Permission> resolvePracownikPermissions() {
		return permissionRepository.findByModuleAndAction("DASHBOARD", "VIEW");
	}

	public Set<String> expectedRoleNames() {
		return Set.of("ADMIN", "BIURO", "KOORDYNATOR", "PRACOWNIK");
	}
}
