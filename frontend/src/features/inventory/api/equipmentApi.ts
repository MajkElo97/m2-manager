import type {
  CreateEquipmentPayload,
  Equipment,
  EquipmentListParams,
  UpdateEquipmentPayload,
} from '@/features/inventory/types/equipment';
import { apiClient } from '@/services/apiClient';

function buildQueryString(params: EquipmentListParams): string {
  const searchParams = new URLSearchParams();

  if (params.search?.trim()) {
    searchParams.set('search', params.search.trim());
  }

  if (params.category?.trim()) {
    searchParams.set('category', params.category.trim());
  }

  if (params.employeeId) {
    searchParams.set('employeeId', params.employeeId);
  }

  if (params.condition) {
    searchParams.set('condition', params.condition);
  }

  if (params.active !== null && params.active !== undefined) {
    searchParams.set('active', String(params.active));
  }

  const query = searchParams.toString();
  return query ? `?${query}` : '';
}

export function getEquipment(params: EquipmentListParams = {}): Promise<Equipment[]> {
  return apiClient.get<Equipment[]>(`/api/inventory/equipment${buildQueryString(params)}`);
}

export function getEquipmentById(id: string): Promise<Equipment> {
  return apiClient.get<Equipment>(`/api/inventory/equipment/${id}`);
}

export function createEquipment(data: CreateEquipmentPayload): Promise<Equipment> {
  return apiClient.post<Equipment>('/api/inventory/equipment', data);
}

export function updateEquipment(id: string, data: UpdateEquipmentPayload): Promise<Equipment> {
  return apiClient.put<Equipment>(`/api/inventory/equipment/${id}`, data);
}

export function deactivateEquipment(id: string): Promise<void> {
  return apiClient.delete<void>(`/api/inventory/equipment/${id}`);
}
