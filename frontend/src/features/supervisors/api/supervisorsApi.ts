import { apiClient } from '@/services/apiClient';
import type {
  CreateSupervisorPayload,
  Supervisor,
  SupervisorListParams,
  UpdateSupervisorPayload,
} from '@/features/supervisors/types/supervisor';

function buildQueryString(params: SupervisorListParams): string {
  const searchParams = new URLSearchParams();

  if (params.search?.trim()) {
    searchParams.set('search', params.search.trim());
  }

  if (params.managerId) {
    searchParams.set('managerId', params.managerId);
  }

  if (params.active !== null && params.active !== undefined) {
    searchParams.set('active', String(params.active));
  }

  const query = searchParams.toString();
  return query ? `?${query}` : '';
}

export function getSupervisors(params: SupervisorListParams = {}): Promise<Supervisor[]> {
  return apiClient.get<Supervisor[]>(`/api/supervisors${buildQueryString(params)}`);
}

export function getSupervisor(id: string): Promise<Supervisor> {
  return apiClient.get<Supervisor>(`/api/supervisors/${id}`);
}

export function createSupervisor(data: CreateSupervisorPayload): Promise<Supervisor> {
  return apiClient.post<Supervisor>('/api/supervisors', data);
}

export function updateSupervisor(id: string, data: UpdateSupervisorPayload): Promise<Supervisor> {
  return apiClient.put<Supervisor>(`/api/supervisors/${id}`, data);
}

export function deactivateSupervisor(id: string): Promise<void> {
  return apiClient.delete<void>(`/api/supervisors/${id}`);
}
