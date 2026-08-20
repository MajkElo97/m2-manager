import { useCallback, useEffect, useState } from 'react';
import { getEmployees } from '@/features/employees/api/employeesApi';
import { getEmployeeErrorMessage } from '@/features/employees/employeesMessages';
import type { Employee, EmployeeListParams } from '@/features/employees/types/employee';
import { useOrganizationContextKey } from '@/features/auth/useOrganizationContextKey';
import { ApiError } from '@/services/apiError';

interface UseEmployeesResult {
  employees: Employee[];
  isLoading: boolean;
  error: string | null;
  forbidden: boolean;
  unauthorized: boolean;
  refetch: () => Promise<void>;
}

export function useEmployees(params: EmployeeListParams): UseEmployeesResult {
  const organizationContextKey = useOrganizationContextKey();
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [forbidden, setForbidden] = useState(false);
  const [unauthorized, setUnauthorized] = useState(false);

  const refetch = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    setForbidden(false);
    setUnauthorized(false);

    try {
      const data = await getEmployees(params);
      setEmployees(data);
    } catch (err) {
      if (err instanceof ApiError && err.status === 401) {
        setUnauthorized(true);
        setError('Sesja wygasła. Zaloguj się ponownie.');
      } else if (err instanceof ApiError && err.status === 403) {
        setForbidden(true);
        setError(getEmployeeErrorMessage(err));
      } else {
        setError(getEmployeeErrorMessage(err));
      }
      setEmployees([]);
    } finally {
      setIsLoading(false);
    }
  }, [params.search, params.position, params.role, params.employmentType, params.active, organizationContextKey]);

  useEffect(() => {
    void refetch();
  }, [refetch]);

  return {
    employees,
    isLoading,
    error,
    forbidden,
    unauthorized,
    refetch,
  };
}
