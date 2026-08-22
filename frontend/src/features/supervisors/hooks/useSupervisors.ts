import { useCallback, useEffect, useState } from 'react';
import { getSupervisors } from '@/features/supervisors/api/supervisorsApi';
import { getSupervisorErrorMessage } from '@/features/supervisors/supervisorsMessages';
import type { Supervisor, SupervisorListParams } from '@/features/supervisors/types/supervisor';
import { useOrganizationContextKey } from '@/features/auth/useOrganizationContextKey';
import { isTenantScopeActive } from '@/features/auth/tenantScope';
import { ApiError } from '@/services/apiError';

interface UseSupervisorsResult {
  supervisors: Supervisor[];
  isLoading: boolean;
  error: string | null;
  forbidden: boolean;
  unauthorized: boolean;
  refetch: () => Promise<void>;
}

export function useSupervisors(params: SupervisorListParams): UseSupervisorsResult {
  const organizationContextKey = useOrganizationContextKey();
  const [supervisors, setSupervisors] = useState<Supervisor[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [forbidden, setForbidden] = useState(false);
  const [unauthorized, setUnauthorized] = useState(false);

  const refetch = useCallback(async () => {
    if (!isTenantScopeActive(organizationContextKey)) {
      setSupervisors([]);
      setIsLoading(false);
      setError(null);
      setForbidden(false);
      setUnauthorized(false);
      return;
    }

    setIsLoading(true);
    setError(null);
    setForbidden(false);
    setUnauthorized(false);

    try {
      const data = await getSupervisors(params);
      setSupervisors(data);
    } catch (err) {
      if (err instanceof ApiError && err.status === 401) {
        setUnauthorized(true);
        setError('Sesja wygasła. Zaloguj się ponownie.');
      } else if (err instanceof ApiError && err.status === 403) {
        setForbidden(true);
        setError(getSupervisorErrorMessage(err));
      } else {
        setError(getSupervisorErrorMessage(err));
      }
      setSupervisors([]);
    } finally {
      setIsLoading(false);
    }
  }, [params.search, params.managerId, params.active, organizationContextKey]);

  useEffect(() => {
    void refetch();
  }, [refetch]);

  return {
    supervisors,
    isLoading,
    error,
    forbidden,
    unauthorized,
    refetch,
  };
}
