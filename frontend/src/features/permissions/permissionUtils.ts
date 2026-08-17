export function extractModule(permissionCode: string): string {
  const separatorIndex = permissionCode.lastIndexOf('_');
  if (separatorIndex <= 0) {
    throw new Error(`Invalid permission code: ${permissionCode}`);
  }
  return permissionCode.substring(0, separatorIndex);
}

export function hasPermission(permissions: ReadonlySet<string>, permissionCode: string): boolean {
  if (permissions.has(permissionCode)) {
    return true;
  }

  return hasModuleAdmin(permissions, extractModule(permissionCode));
}

export function hasAnyPermission(permissions: ReadonlySet<string>, permissionCodes: string[]): boolean {
  return permissionCodes.some((code) => hasPermission(permissions, code));
}

export function hasAllPermissions(permissions: ReadonlySet<string>, permissionCodes: string[]): boolean {
  return permissionCodes.every((code) => hasPermission(permissions, code));
}

export function hasModuleAdmin(permissions: ReadonlySet<string>, module: string): boolean {
  return permissions.has(`${module}_ADMIN`);
}

const MODULES = [
  'DASHBOARD',
  'BUILDINGS',
  'STAIRCASES',
  'SCOPES',
  'ACTIVITIES',
  'EMPLOYEES',
  'MANAGERS',
  'SUPERVISORS',
  'CONTACTS',
  'SCHEDULE',
  'FINANCES',
  'WAREHOUSE',
  'FLEET',
  'REPORTS',
  'USERS',
  'ROLES',
  'SETTINGS',
] as const;

const ACTIONS = ['VIEW', 'CREATE', 'EDIT', 'DELETE', 'ADMIN'] as const;

export function buildAllPermissionCodes(): string[] {
  return MODULES.flatMap((module) => ACTIONS.map((action) => `${module}_${action}`));
}
