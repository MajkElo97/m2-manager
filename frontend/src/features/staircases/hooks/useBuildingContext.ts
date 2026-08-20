import { useCallback, useEffect, useState } from 'react';
import { getBuilding } from '@/features/buildings/api/buildingsApi';
import { getBuildingErrorMessage } from '@/features/buildings/buildingsMessages';
import type { Building } from '@/features/buildings/types/building';
import { useOrganizationContextKey } from '@/features/auth/useOrganizationContextKey';
import { ApiError } from '@/services/apiError';

interface UseBuildingContextResult {
  building: Building | null;
  isLoading: boolean;
  error: string | null;
  notFound: boolean;
  refetch: () => Promise<void>;
}

export function useBuildingContext(buildingId: string): UseBuildingContextResult {
  const organizationContextKey = useOrganizationContextKey();
  const [building, setBuilding] = useState<Building | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [notFound, setNotFound] = useState(false);

  const refetch = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    setNotFound(false);

    try {
      const data = await getBuilding(buildingId);
      setBuilding(data);
    } catch (err) {
      if (err instanceof ApiError && err.status === 404) {
        setNotFound(true);
      }
      setError(getBuildingErrorMessage(err));
      setBuilding(null);
    } finally {
      setIsLoading(false);
    }
  }, [buildingId, organizationContextKey]);

  useEffect(() => {
    void refetch();
  }, [refetch]);

  return {
    building,
    isLoading,
    error,
    notFound,
    refetch,
  };
}
