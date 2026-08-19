export interface Permission {
  code: string;
  module: string;
  action: string;
  description: string;
}

export type PermissionAction = 'VIEW' | 'CREATE' | 'EDIT' | 'DELETE';

export const PERMISSION_MATRIX_ACTIONS: PermissionAction[] = ['VIEW', 'CREATE', 'EDIT', 'DELETE'];
