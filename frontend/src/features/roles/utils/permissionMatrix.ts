import type { Permission, PermissionAction } from '@/features/roles/types/permission';
import { PERMISSION_MATRIX_ACTIONS } from '@/features/roles/types/permission';

export function getModuleLabel(module: string): string {
  switch (module) {
    case 'DASHBOARD':
      return 'Dashboard';
    case 'BUILDINGS':
      return 'Budynki';
    case 'STAIRCASES':
      return 'Klatki schodowe';
    case 'SCOPES':
      return 'Zakresy';
    case 'ACTIVITIES':
      return 'Katalog czynności';
    case 'EMPLOYEES':
      return 'Pracownicy';
    case 'MANAGERS':
      return 'Zarządcy';
    case 'SUPERVISORS':
      return 'Opiekunowie';
    case 'CONTACTS':
      return 'Kontakty';
    case 'SCHEDULE':
      return 'Harmonogram';
    case 'FINANCES':
      return 'Finanse';
    case 'WAREHOUSE':
      return 'Magazyn';
    case 'FLEET':
      return 'Flota';
    case 'REPORTS':
      return 'Raporty';
    case 'USERS':
      return 'Użytkownicy';
    case 'ROLES':
      return 'Role';
    case 'SETTINGS':
      return 'Ustawienia';
    default:
      return module;
  }
}

export function getPermissionActionLabel(action: PermissionAction): string {
  switch (action) {
    case 'VIEW':
      return 'Podgląd';
    case 'CREATE':
      return 'Dodawanie';
    case 'EDIT':
      return 'Edycja';
    case 'DELETE':
      return 'Dezaktywacja';
    default:
      return action;
  }
}

export interface PermissionModuleRow {
  module: string;
  label: string;
  permissions: Partial<Record<PermissionAction, Permission>>;
}

export function buildPermissionMatrix(permissions: Permission[]): PermissionModuleRow[] {
  const byModule = new Map<string, Partial<Record<PermissionAction, Permission>>>();

  for (const permission of permissions) {
    if (permission.action === 'ADMIN') {
      continue;
    }

    if (!PERMISSION_MATRIX_ACTIONS.includes(permission.action as PermissionAction)) {
      continue;
    }

    const action = permission.action as PermissionAction;
    const modulePermissions = byModule.get(permission.module) ?? {};
    modulePermissions[action] = permission;
    byModule.set(permission.module, modulePermissions);
  }

  return Array.from(byModule.entries())
    .map(([module, modulePermissions]) => ({
      module,
      label: getModuleLabel(module),
      permissions: modulePermissions,
    }))
    .sort((left, right) => left.label.localeCompare(right.label, 'pl'));
}

export function getModulePermissionCodes(
  row: PermissionModuleRow,
  actions: PermissionAction[],
): string[] {
  return actions
    .map((action) => row.permissions[action]?.code)
    .filter((code): code is string => code != null);
}

export function getReadPermissionCodes(row: PermissionModuleRow): string[] {
  return getModulePermissionCodes(row, ['VIEW']);
}

export function getEditPermissionCodes(row: PermissionModuleRow): string[] {
  return getModulePermissionCodes(row, ['VIEW', 'CREATE', 'EDIT', 'DELETE']);
}

export function isReadPresetSelected(row: PermissionModuleRow, selectedCodes: Set<string>): boolean {
  const readCodes = getReadPermissionCodes(row);
  if (readCodes.length === 0) {
    return false;
  }
  return readCodes.every((code) => selectedCodes.has(code));
}

export function isEditPresetSelected(row: PermissionModuleRow, selectedCodes: Set<string>): boolean {
  const editCodes = getEditPermissionCodes(row);
  if (editCodes.length === 0) {
    return false;
  }
  return editCodes.every((code) => selectedCodes.has(code));
}

export function togglePermissionCodes(
  selectedCodes: Set<string>,
  codes: string[],
  enabled: boolean,
): Set<string> {
  const next = new Set(selectedCodes);
  for (const code of codes) {
    if (enabled) {
      next.add(code);
    } else {
      next.delete(code);
    }
  }
  return next;
}

export function applyReadPreset(
  row: PermissionModuleRow,
  selectedCodes: Set<string>,
): Set<string> {
  const editCodes = getEditPermissionCodes(row);
  const withoutModule = new Set(
    [...selectedCodes].filter((code) => !editCodes.includes(code)),
  );
  return togglePermissionCodes(withoutModule, getReadPermissionCodes(row), true);
}

export function applyEditPreset(
  row: PermissionModuleRow,
  selectedCodes: Set<string>,
): Set<string> {
  const editCodes = getEditPermissionCodes(row);
  const withoutModule = new Set(
    [...selectedCodes].filter((code) => !editCodes.includes(code)),
  );
  return togglePermissionCodes(withoutModule, editCodes, true);
}

export function clearModulePermissions(
  row: PermissionModuleRow,
  selectedCodes: Set<string>,
): Set<string> {
  const editCodes = getEditPermissionCodes(row);
  return new Set([...selectedCodes].filter((code) => !editCodes.includes(code)));
}
