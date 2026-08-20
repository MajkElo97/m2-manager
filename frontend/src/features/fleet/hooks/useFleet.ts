import { useCallback, useEffect, useState } from 'react';
import { getVehicles } from '@/features/fleet/api/fleetApi';
import { getFleetErrorMessage } from '@/features/fleet/fleetMessages';
import type { Vehicle, VehicleListParams } from '@/features/fleet/types/vehicle';
import { useOrganizationContextKey } from '@/features/auth/useOrganizationContextKey';
import { ApiError } from '@/services/apiError';

interface UseFleetResult {
  vehicles: Vehicle[];
  isLoading: boolean;
  error: string | null;
  forbidden: boolean;
  unauthorized: boolean;
  refetch: () => Promise<void>;
}

export function useFleet(params: VehicleListParams): UseFleetResult {
  const organizationContextKey = useOrganizationContextKey();
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
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
      const data = await getVehicles(params);
      setVehicles(data);
    } catch (err) {
      if (err instanceof ApiError && err.status === 401) {
        setUnauthorized(true);
        setError('Sesja wygasła. Zaloguj się ponownie.');
      } else if (err instanceof ApiError && err.status === 403) {
        setForbidden(true);
        setError(getFleetErrorMessage(err));
      } else {
        setError(getFleetErrorMessage(err));
      }
      setVehicles([]);
    } finally {
      setIsLoading(false);
    }
  }, [params.search, params.status, params.employeeId, params.vehicleType, organizationContextKey]);

  useEffect(() => {
    void refetch();
  }, [refetch]);

  return {
    vehicles,
    isLoading,
    error,
    forbidden,
    unauthorized,
    refetch,
  };
}
