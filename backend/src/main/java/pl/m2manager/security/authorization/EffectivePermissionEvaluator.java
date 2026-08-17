package pl.m2manager.security.authorization;

import java.util.Set;

public final class EffectivePermissionEvaluator {

	private EffectivePermissionEvaluator() {
	}

	public static boolean hasPermission(Set<String> assignedPermissionCodes, String permissionCode) {
		if (assignedPermissionCodes.contains(permissionCode)) {
			return true;
		}
		return hasModuleAdmin(assignedPermissionCodes, extractModule(permissionCode));
	}

	public static boolean hasAnyPermission(Set<String> assignedPermissionCodes, String... permissionCodes) {
		for (String permissionCode : permissionCodes) {
			if (hasPermission(assignedPermissionCodes, permissionCode)) {
				return true;
			}
		}
		return false;
	}

	public static boolean hasAllPermissions(Set<String> assignedPermissionCodes, String... permissionCodes) {
		for (String permissionCode : permissionCodes) {
			if (!hasPermission(assignedPermissionCodes, permissionCode)) {
				return false;
			}
		}
		return true;
	}

	public static boolean hasModuleAdmin(Set<String> assignedPermissionCodes, String module) {
		return assignedPermissionCodes.contains(module + "_ADMIN");
	}

	static String extractModule(String permissionCode) {
		int separatorIndex = permissionCode.lastIndexOf('_');
		if (separatorIndex <= 0) {
			throw new IllegalArgumentException("Invalid permission code: " + permissionCode);
		}
		return permissionCode.substring(0, separatorIndex);
	}
}
