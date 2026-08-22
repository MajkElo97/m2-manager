import { useCallback, useEffect, useState } from 'react';
import { getEquipment } from '@/features/inventory/api/equipmentApi';
import { getInventoryErrorMessage } from '@/features/inventory/inventoryMessages';
import type { Equipment, EquipmentListParams } from '@/features/inventory/types/equipment';
import { useOrganizationContextKey } from '@/features/auth/useOrganizationContextKey';
import { isTenantScopeActive } from '@/features/auth/tenantScope';
import { ApiError } from '@/services/apiError';

interface UseEquipmentResult {
  equipment: Equipment[];
  isLoading: boolean;
  error: string | null;
  forbidden: boolean;
  unauthorized: boolean;
  refetch: () => Promise<void>;
}

export function useEquipment(params: EquipmentListParams): UseEquipmentResult {
  const organizationContextKey = useOrganizationContextKey();
  const [equipment, setEquipment] = useState<Equipment[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [forbidden, setForbidden] = useState(false);
  const [unauthorized, setUnauthorized] = useState(false);

  const refetch = useCallback(async () => {
    if (!isTenantScopeActive(organizationContextKey)) {
      setEquipment([]);
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
      const data = await getEquipment(params);
      setEquipment(data);
    } catch (err) {
      if (err instanceof ApiError && err.status === 401) {
        setUnauthorized(true);
        setError('Sesja wygasła. Zaloguj się ponownie.');
      } else if (err instanceof ApiError && err.status === 403) {
        setForbidden(true);
        setError(getInventoryErrorMessage(err));
      } else {
        setError(getInventoryErrorMessage(err));
      }
      setEquipment([]);
    } finally {
      setIsLoading(false);
    }
  }, [params.search, params.category, params.employeeId, params.condition, params.active, organizationContextKey]);

  useEffect(() => {
    void refetch();
  }, [refetch]);

  return {
    equipment,
    isLoading,
    error,
    forbidden,
    unauthorized,
    refetch,
  };
}
