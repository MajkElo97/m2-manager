import { useCallback, useEffect, useState } from 'react';
import { getScopes } from '@/features/scopes/api/scopesApi';
import { getScopeErrorMessage } from '@/features/scopes/scopesMessages';
import type { Scope } from '@/features/scopes/types/scope';
import { useOrganizationContextKey } from '@/features/auth/useOrganizationContextKey';
import { ApiError } from '@/services/apiError';

interface UseScopesResult {
  scopes: Scope[];
  isLoading: boolean;
  error: string | null;
  forbidden: boolean;
  notFound: boolean;
  refetch: () => Promise<void>;
}

export function useScopes(buildingId?: string): UseScopesResult {
  const organizationContextKey = useOrganizationContextKey();
  const [scopes, setScopes] = useState<Scope[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [forbidden, setForbidden] = useState(false);
  const [notFound, setNotFound] = useState(false);

  const refetch = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    setForbidden(false);
    setNotFound(false);

    try {
      const data = await getScopes(buildingId ? { buildingId } : {});
      setScopes(data);
    } catch (err) {
      if (err instanceof ApiError && err.status === 403) {
        setForbidden(true);
      } else if (err instanceof ApiError && err.status === 404) {
        setNotFound(true);
      }
      setError(getScopeErrorMessage(err));
      setScopes([]);
    } finally {
      setIsLoading(false);
    }
  }, [buildingId, organizationContextKey]);

  useEffect(() => {
    void refetch();
  }, [refetch]);

  return {
    scopes,
    isLoading,
    error,
    forbidden,
    notFound,
    refetch,
  };
}
