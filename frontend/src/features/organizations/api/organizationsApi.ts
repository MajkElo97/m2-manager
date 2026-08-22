import { apiClient } from '@/services/apiClient';
import type {
  CreateOrganizationPayload,
  CreateOrganizationResult,
  OrganizationDetail,
  OrganizationListItem,
  OrganizationListParams,
  ResetAdminPasswordResult,
  UpdateOrganizationPayload,
} from '@/features/organizations/types/organization';

function buildQueryString(params: OrganizationListParams): string {
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

export function getOrganizations(params: OrganizationListParams = {}): Promise<OrganizationListItem[]> {
  return apiClient.get<OrganizationListItem[]>(`/api/organizations${buildQueryString(params)}`);
}

export function getOrganization(id: string): Promise<OrganizationDetail> {
  return apiClient.get<OrganizationDetail>(`/api/organizations/${id}`);
}

export function createOrganization(data: CreateOrganizationPayload): Promise<CreateOrganizationResult> {
  return apiClient.post<CreateOrganizationResult>('/api/organizations', data);
}

export function updateOrganization(id: string, data: UpdateOrganizationPayload): Promise<OrganizationDetail> {
  return apiClient.put<OrganizationDetail>(`/api/organizations/${id}`, data);
}

export function deactivateOrganization(id: string): Promise<OrganizationDetail> {
  return apiClient.delete<OrganizationDetail>(`/api/organizations/${id}`);
}

export function resetAdminPassword(id: string): Promise<ResetAdminPasswordResult> {
  return apiClient.post<ResetAdminPasswordResult>(`/api/organizations/${id}/reset-admin-password`);
}
