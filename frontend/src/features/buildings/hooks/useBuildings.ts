import { useCallback, useEffect, useState } from 'react';
import { getBuildings } from '@/features/buildings/api/buildingsApi';
import { getBuildingErrorMessage } from '@/features/buildings/buildingsMessages';
import type { Building, BuildingListParams } from '@/features/buildings/types/building';
import { useOrganizationContextKey } from '@/features/auth/useOrganizationContextKey';
import { isTenantScopeActive } from '@/features/auth/tenantScope';
import { ApiError } from '@/services/apiError';

interface UseBuildingsResult {
  buildings: Building[];
  isLoading: boolean;
  error: string | null;
  forbidden: boolean;
  unauthorized: boolean;
  refetch: () => Promise<void>;
}

export function useBuildings(params: BuildingListParams): UseBuildingsResult {
  const organizationContextKey = useOrganizationContextKey();
  const [buildings, setBuildings] = useState<Building[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [forbidden, setForbidden] = useState(false);
  const [unauthorized, setUnauthorized] = useState(false);

  const refetch = useCallback(async () => {
    if (!isTenantScopeActive(organizationContextKey)) {
      setBuildings([]);
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
      const data = await getBuildings(params);
      setBuildings(data);
    } catch (err) {
      if (err instanceof ApiError && err.status === 401) {
        setUnauthorized(true);
        setError('Sesja wygasła. Zaloguj się ponownie.');
      } else if (err instanceof ApiError && err.status === 403) {
        setForbidden(true);
        setError(getBuildingErrorMessage(err));
      } else {
        setError(getBuildingErrorMessage(err));
      }
      setBuildings([]);
    } finally {
      setIsLoading(false);
    }
  }, [params.status, params.search, organizationContextKey]);

  useEffect(() => {
    void refetch();
  }, [refetch]);

  return {
    buildings,
    isLoading,
    error,
    forbidden,
    unauthorized,
    refetch,
  };
}
