import { apiClient } from '@/services/apiClient';
import type {
  CreateManagerPayload,
  Manager,
  ManagerListParams,
  UpdateManagerPayload,
} from '@/features/managers/types/manager';

function buildQueryString(params: ManagerListParams): string {
  const searchParams = new URLSearchParams();

  if (params.search?.trim()) {
    searchParams.set('search', params.search.trim());
  }

  if (params.active !== null && params.active !== undefined) {
    searchParams.set('active', String(params.active));
  }

  const query = searchParams.toString();
  return query ? `?${query}` : '';
}

export function getManagers(params: ManagerListParams = {}): Promise<Manager[]> {
  return apiClient.get<Manager[]>(`/api/managers${buildQueryString(params)}`);
}

export function getManager(id: string): Promise<Manager> {
  return apiClient.get<Manager>(`/api/managers/${id}`);
}

export function createManager(data: CreateManagerPayload): Promise<Manager> {
  return apiClient.post<Manager>('/api/managers', data);
}

export function updateManager(id: string, data: UpdateManagerPayload): Promise<Manager> {
  return apiClient.put<Manager>(`/api/managers/${id}`, data);
}

export function deactivateManager(id: string): Promise<void> {
  return apiClient.delete<void>(`/api/managers/${id}`);
}
