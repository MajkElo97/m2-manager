import { useCallback, useEffect, useState } from 'react';
import { getStaircases } from '@/features/staircases/api/staircasesApi';
import { getStaircaseErrorMessage } from '@/features/staircases/staircasesMessages';
import type { Staircase } from '@/features/staircases/types/staircase';
import { useOrganizationContextKey } from '@/features/auth/useOrganizationContextKey';
import { isTenantScopeActive } from '@/features/auth/tenantScope';
import { ApiError } from '@/services/apiError';

interface UseStaircasesResult {
  staircases: Staircase[];
  isLoading: boolean;
  error: string | null;
  forbidden: boolean;
  notFound: boolean;
  refetch: () => Promise<void>;
}

export function useStaircases(buildingId?: string): UseStaircasesResult {
  const organizationContextKey = useOrganizationContextKey();
  const [staircases, setStaircases] = useState<Staircase[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [forbidden, setForbidden] = useState(false);
  const [notFound, setNotFound] = useState(false);

  const refetch = useCallback(async () => {
    if (!isTenantScopeActive(organizationContextKey)) {
      setStaircases([]);
      setIsLoading(false);
      setError(null);
      setForbidden(false);
      setNotFound(false);
      return;
    }

    setIsLoading(true);
    setError(null);
    setForbidden(false);
    setNotFound(false);

    try {
      const data = await getStaircases(buildingId);
      setStaircases(data);
    } catch (err) {
      if (err instanceof ApiError && err.status === 403) {
        setForbidden(true);
      } else if (err instanceof ApiError && err.status === 404) {
        setNotFound(true);
      }
      setError(getStaircaseErrorMessage(err));
      setStaircases([]);
    } finally {
      setIsLoading(false);
    }
  }, [buildingId, organizationContextKey]);

  useEffect(() => {
    void refetch();
  }, [refetch]);

  return {
    staircases,
    isLoading,
    error,
    forbidden,
    notFound,
    refetch,
  };
}
