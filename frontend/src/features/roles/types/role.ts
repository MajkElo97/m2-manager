export interface Role {
  id: string;
  name: string;
  description: string | null;
  systemRole: boolean;
  active: boolean;
  userCount: number;
  permissionCount: number;
}

export interface CreateRolePayload {
  name: string;
  description?: string;
}

export interface UpdateRolePayload {
  name?: string;
  description?: string;
  active?: boolean;
}

export interface UpdateRolePermissionsPayload {
  permissionCodes: string[];
}
