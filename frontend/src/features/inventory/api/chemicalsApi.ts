import type {
  Chemical,
  ChemicalListParams,
  CreateChemicalPayload,
  UpdateChemicalPayload,
} from '@/features/inventory/types/chemical';
import { apiClient } from '@/services/apiClient';

function buildQueryString(params: ChemicalListParams): string {
  const searchParams = new URLSearchParams();

  if (params.search?.trim()) {
    searchParams.set('search', params.search.trim());
  }

  if (params.category?.trim()) {
    searchParams.set('category', params.category.trim());
  }

  if (params.active !== null && params.active !== undefined) {
    searchParams.set('active', String(params.active));
  }

  if (params.lowStock !== null && params.lowStock !== undefined) {
    searchParams.set('lowStock', String(params.lowStock));
  }

  const query = searchParams.toString();
  return query ? `?${query}` : '';
}

export function getChemicals(params: ChemicalListParams = {}): Promise<Chemical[]> {
  return apiClient.get<Chemical[]>(`/api/inventory/chemicals${buildQueryString(params)}`);
}

export function getChemicalById(id: string): Promise<Chemical> {
  return apiClient.get<Chemical>(`/api/inventory/chemicals/${id}`);
}

export function createChemical(data: CreateChemicalPayload): Promise<Chemical> {
  return apiClient.post<Chemical>('/api/inventory/chemicals', data);
}

export function updateChemical(id: string, data: UpdateChemicalPayload): Promise<Chemical> {
  return apiClient.put<Chemical>(`/api/inventory/chemicals/${id}`, data);
}

export function deactivateChemical(id: string): Promise<void> {
  return apiClient.delete<void>(`/api/inventory/chemicals/${id}`);
}
