import { apiClient } from '@/services/apiClient';
import type { Permission } from '@/features/roles/types/permission';

export function getPermissions(): Promise<Permission[]> {
  return apiClient.get<Permission[]>('/api/permissions');
}
