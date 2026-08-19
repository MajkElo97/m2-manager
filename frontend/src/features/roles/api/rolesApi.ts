import { apiClient } from '@/services/apiClient';
import type { Permission } from '@/features/roles/types/permission';
import type {
  CreateRolePayload,
  Role,
  UpdateRolePayload,
  UpdateRolePermissionsPayload,
} from '@/features/roles/types/role';

export function getRoles(): Promise<Role[]> {
  return apiClient.get<Role[]>('/api/roles');
}

export function getRole(id: string): Promise<Role> {
  return apiClient.get<Role>(`/api/roles/${id}`);
}

export function createRole(data: CreateRolePayload): Promise<Role> {
  return apiClient.post<Role>('/api/roles', data);
}

export function updateRole(id: string, data: UpdateRolePayload): Promise<Role> {
  return apiClient.put<Role>(`/api/roles/${id}`, data);
}

export function deactivateRole(id: string): Promise<void> {
  return apiClient.delete<void>(`/api/roles/${id}`);
}

export function getRolePermissions(id: string): Promise<Permission[]> {
  return apiClient.get<Permission[]>(`/api/roles/${id}/permissions`);
}

export function updateRolePermissions(
  id: string,
  data: UpdateRolePermissionsPayload,
): Promise<void> {
  return apiClient.put<void>(`/api/roles/${id}/permissions`, data);
}
