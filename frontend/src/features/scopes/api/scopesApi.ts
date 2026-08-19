import { apiClient } from '@/services/apiClient';
import type {
  CreateScopePayload,
  Scope,
  ScopeListParams,
  UpdateScopePayload,
} from '@/features/scopes/types/scope';

function buildQueryString(params: ScopeListParams): string {
  const searchParams = new URLSearchParams();

  if (params.buildingId) {
    searchParams.set('buildingId', params.buildingId);
  }

  if (params.activityId) {
    searchParams.set('activityId', params.activityId);
  }

  if (params.planningType) {
    searchParams.set('planningType', params.planningType);
  }

  if (params.status) {
    searchParams.set('status', params.status);
  }

  if (params.search?.trim()) {
    searchParams.set('search', params.search.trim());
  }

  const query = searchParams.toString();
  return query ? `?${query}` : '';
}

export function getScopes(params: ScopeListParams = {}): Promise<Scope[]> {
  return apiClient.get<Scope[]>(`/api/scopes${buildQueryString(params)}`);
}

export function getScope(id: string): Promise<Scope> {
  return apiClient.get<Scope>(`/api/scopes/${id}`);
}

export function createScope(data: CreateScopePayload): Promise<Scope> {
  return apiClient.post<Scope>('/api/scopes', data);
}

export function updateScope(id: string, data: UpdateScopePayload): Promise<Scope> {
  return apiClient.put<Scope>(`/api/scopes/${id}`, data);
}

export function deactivateScope(id: string): Promise<void> {
  return apiClient.delete<void>(`/api/scopes/${id}`);
}
