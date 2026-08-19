import { apiClient } from '@/services/apiClient';
import type {
  CreateStaircasePayload,
  Staircase,
  UpdateStaircasePayload,
} from '@/features/staircases/types/staircase';

export function getStaircases(buildingId?: string): Promise<Staircase[]> {
  const path = buildingId
    ? `/api/staircases?buildingId=${encodeURIComponent(buildingId)}`
    : '/api/staircases';
  return apiClient.get<Staircase[]>(path);
}

export function getStaircase(id: string): Promise<Staircase> {
  return apiClient.get<Staircase>(`/api/staircases/${id}`);
}

export function createStaircase(data: CreateStaircasePayload): Promise<Staircase> {
  return apiClient.post<Staircase>('/api/staircases', data);
}

export function updateStaircase(id: string, data: UpdateStaircasePayload): Promise<Staircase> {
  return apiClient.put<Staircase>(`/api/staircases/${id}`, data);
}

export function deleteStaircase(id: string): Promise<void> {
  return apiClient.delete<void>(`/api/staircases/${id}`);
}
