import { apiClient } from '@/services/apiClient';
import type {
  CreateEmployeePayload,
  Employee,
  EmployeeListParams,
  UpdateEmployeePayload,
} from '@/features/employees/types/employee';

function buildQueryString(params: EmployeeListParams): string {
  const searchParams = new URLSearchParams();

  if (params.search?.trim()) {
    searchParams.set('search', params.search.trim());
  }

  if (params.position?.trim()) {
    searchParams.set('position', params.position.trim());
  }

  if (params.role) {
    searchParams.set('role', params.role);
  }

  if (params.employmentType) {
    searchParams.set('employmentType', params.employmentType);
  }

  if (params.active !== null && params.active !== undefined) {
    searchParams.set('active', String(params.active));
  }

  const query = searchParams.toString();
  return query ? `?${query}` : '';
}

export function getEmployees(params: EmployeeListParams = {}): Promise<Employee[]> {
  return apiClient.get<Employee[]>(`/api/employees${buildQueryString(params)}`);
}

export function getEmployee(id: string): Promise<Employee> {
  return apiClient.get<Employee>(`/api/employees/${id}`);
}

export function createEmployee(data: CreateEmployeePayload): Promise<Employee> {
  return apiClient.post<Employee>('/api/employees', data);
}

export function updateEmployee(id: string, data: UpdateEmployeePayload): Promise<Employee> {
  return apiClient.put<Employee>(`/api/employees/${id}`, data);
}

export function deactivateEmployee(id: string): Promise<void> {
  return apiClient.delete<void>(`/api/employees/${id}`);
}
