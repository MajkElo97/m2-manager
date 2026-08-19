import type {
  CreateVehiclePayload,
  UpdateVehiclePayload,
  Vehicle,
  VehicleListParams,
} from '@/features/fleet/types/vehicle';
import { apiClient } from '@/services/apiClient';

function buildQueryString(params: VehicleListParams): string {
  const searchParams = new URLSearchParams();

  if (params.search?.trim()) {
    searchParams.set('search', params.search.trim());
  }

  if (params.status) {
    searchParams.set('status', params.status);
  }

  if (params.employeeId) {
    searchParams.set('employeeId', params.employeeId);
  }

  if (params.vehicleType) {
    searchParams.set('vehicleType', params.vehicleType);
  }

  const query = searchParams.toString();
  return query ? `?${query}` : '';
}

export function getVehicles(params: VehicleListParams = {}): Promise<Vehicle[]> {
  return apiClient.get<Vehicle[]>(`/api/fleet${buildQueryString(params)}`);
}

export function getVehicle(id: string): Promise<Vehicle> {
  return apiClient.get<Vehicle>(`/api/fleet/${id}`);
}

export function createVehicle(data: CreateVehiclePayload): Promise<Vehicle> {
  return apiClient.post<Vehicle>('/api/fleet', data);
}

export function updateVehicle(id: string, data: UpdateVehiclePayload): Promise<Vehicle> {
  return apiClient.put<Vehicle>(`/api/fleet/${id}`, data);
}

export function deactivateVehicle(id: string): Promise<void> {
  return apiClient.delete<void>(`/api/fleet/${id}`);
}
