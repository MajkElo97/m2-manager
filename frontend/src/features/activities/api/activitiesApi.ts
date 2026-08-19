import { apiClient } from '@/services/apiClient';
import type {
  Activity,
  ActivityListParams,
  CreateActivityPayload,
  UpdateActivityPayload,
} from '@/features/activities/types/activity';

function buildQueryString(params: ActivityListParams): string {
  const searchParams = new URLSearchParams();

  if (params.search?.trim()) {
    searchParams.set('search', params.search.trim());
  }

  if (params.category?.trim()) {
    searchParams.set('category', params.category.trim());
  }

  if (params.planningType) {
    searchParams.set('planningType', params.planningType);
  }

  if (params.active !== null && params.active !== undefined) {
    searchParams.set('active', String(params.active));
  }

  const query = searchParams.toString();
  return query ? `?${query}` : '';
}

export function getActivities(params: ActivityListParams = {}): Promise<Activity[]> {
  return apiClient.get<Activity[]>(`/api/activities${buildQueryString(params)}`);
}

export function getActivity(id: string): Promise<Activity> {
  return apiClient.get<Activity>(`/api/activities/${id}`);
}

export function createActivity(data: CreateActivityPayload): Promise<Activity> {
  return apiClient.post<Activity>('/api/activities', data);
}

export function updateActivity(id: string, data: UpdateActivityPayload): Promise<Activity> {
  return apiClient.put<Activity>(`/api/activities/${id}`, data);
}

export function deactivateActivity(id: string): Promise<void> {
  return apiClient.delete<void>(`/api/activities/${id}`);
}
