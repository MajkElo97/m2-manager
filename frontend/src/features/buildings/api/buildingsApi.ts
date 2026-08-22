import { apiClient } from '@/services/apiClient';
import type {
  Building,
  BuildingListParams,
  CreateBuildingPayload,
  UpdateBuildingPayload,
} from '@/features/buildings/types/building';

function buildQueryString(params: BuildingListParams): string {
  const searchParams = new URLSearchParams();

  if (params.status) {
    searchParams.set('status', params.status);
  }

  if (params.search?.trim()) {
    searchParams.set('search', params.search.trim());
  }

  const query = searchParams.toString();
  return query ? `?${query}` : '';
}

export function getBuildings(params: BuildingListParams = {}): Promise<Building[]> {
  return apiClient.get<Building[]>(`/api/buildings${buildQueryString(params)}`);
}

export function getBuilding(id: string): Promise<Building> {
  return apiClient.get<Building>(`/api/buildings/${id}`);
}

export function createBuilding(data: CreateBuildingPayload): Promise<Building> {
  return apiClient.post<Building>('/api/buildings', data);
}

export function updateBuilding(id: string, data: UpdateBuildingPayload): Promise<Building> {
  return apiClient.put<Building>(`/api/buildings/${id}`, data);
}

export function deactivateBuilding(id: string): Promise<void> {
  return apiClient.delete<void>(`/api/buildings/${id}`);
}

export function permanentDeleteBuilding(id: string): Promise<void> {
  return apiClient.delete<void>(`/api/buildings/${id}/permanent`);
}
