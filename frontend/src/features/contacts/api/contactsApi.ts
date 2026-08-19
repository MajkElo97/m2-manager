import { apiClient } from '@/services/apiClient';
import type {
  Contact,
  ContactListParams,
  CreateContactPayload,
  UpdateContactPayload,
} from '@/features/contacts/types/contact';

function buildQueryString(params: ContactListParams): string {
  const searchParams = new URLSearchParams();

  if (params.buildingId) {
    searchParams.set('buildingId', params.buildingId);
  }

  const query = searchParams.toString();
  return query ? `?${query}` : '';
}

export function getContacts(params: ContactListParams = {}): Promise<Contact[]> {
  return apiClient.get<Contact[]>(`/api/contacts${buildQueryString(params)}`);
}

export function getContact(id: string): Promise<Contact> {
  return apiClient.get<Contact>(`/api/contacts/${id}`);
}

export function createContact(data: CreateContactPayload): Promise<Contact> {
  return apiClient.post<Contact>('/api/contacts', data);
}

export function updateContact(id: string, data: UpdateContactPayload): Promise<Contact> {
  return apiClient.put<Contact>(`/api/contacts/${id}`, data);
}

export function deactivateContact(id: string): Promise<void> {
  return apiClient.delete<void>(`/api/contacts/${id}`);
}
