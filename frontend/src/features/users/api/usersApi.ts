import { apiClient } from '@/services/apiClient';
import type {
  CreateUserPayload,
  UpdateUserPayload,
  User,
  UserListParams,
} from '@/features/users/types/user';

function buildQueryString(params: UserListParams): string {
  const searchParams = new URLSearchParams();

  if (params.search?.trim()) {
    searchParams.set('search', params.search.trim());
  }

  if (params.active !== null && params.active !== undefined) {
    searchParams.set('active', String(params.active));
  }

  if (params.roleId) {
    searchParams.set('roleId', params.roleId);
  }

  const query = searchParams.toString();
  return query ? `?${query}` : '';
}

export function getUsers(params: UserListParams = {}): Promise<User[]> {
  return apiClient.get<User[]>(`/api/users${buildQueryString(params)}`);
}

export function getUser(id: string): Promise<User> {
  return apiClient.get<User>(`/api/users/${id}`);
}

export function createUser(data: CreateUserPayload): Promise<User> {
  return apiClient.post<User>('/api/users', data);
}

export function updateUser(id: string, data: UpdateUserPayload): Promise<User> {
  return apiClient.put<User>(`/api/users/${id}`, data);
}

export function deactivateUser(id: string): Promise<void> {
  return apiClient.delete<void>(`/api/users/${id}`);
}
