import { useCallback, useEffect, useState } from 'react';
import { getManagers } from '@/features/managers/api/managersApi';
import { getManagerErrorMessage } from '@/features/managers/managersMessages';
import type { Manager, ManagerListParams } from '@/features/managers/types/manager';
import { useOrganizationContextKey } from '@/features/auth/useOrganizationContextKey';
import { isTenantScopeActive } from '@/features/auth/tenantScope';
import { ApiError } from '@/services/apiError';

interface UseManagersResult {
  managers: Manager[];
  isLoading: boolean;
  error: string | null;
  forbidden: boolean;
  unauthorized: boolean;
  refetch: () => Promise<void>;
}

export function useManagers(params: ManagerListParams): UseManagersResult {
  const organizationContextKey = useOrganizationContextKey();
  const [managers, setManagers] = useState<Manager[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [forbidden, setForbidden] = useState(false);
  const [unauthorized, setUnauthorized] = useState(false);

  const refetch = useCallback(async () => {
    if (!isTenantScopeActive(organizationContextKey)) {
      setManagers([]);
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
      const data = await getManagers(params);
      setManagers(data);
    } catch (err) {
      if (err instanceof ApiError && err.status === 401) {
        setUnauthorized(true);
        setError('Sesja wygasła. Zaloguj się ponownie.');
      } else if (err instanceof ApiError && err.status === 403) {
        setForbidden(true);
        setError(getManagerErrorMessage(err));
      } else {
        setError(getManagerErrorMessage(err));
      }
      setManagers([]);
    } finally {
      setIsLoading(false);
    }
  }, [params.search, params.active, organizationContextKey]);

  useEffect(() => {
    void refetch();
  }, [refetch]);

  return {
    managers,
    isLoading,
    error,
    forbidden,
    unauthorized,
    refetch,
  };
}
