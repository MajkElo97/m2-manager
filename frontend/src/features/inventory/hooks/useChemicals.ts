import { useCallback, useEffect, useState } from 'react';
import { getChemicals } from '@/features/inventory/api/chemicalsApi';
import { getInventoryErrorMessage } from '@/features/inventory/inventoryMessages';
import type { Chemical, ChemicalListParams } from '@/features/inventory/types/chemical';
import { ApiError } from '@/services/apiError';

interface UseChemicalsResult {
  chemicals: Chemical[];
  isLoading: boolean;
  error: string | null;
  forbidden: boolean;
  unauthorized: boolean;
  refetch: () => Promise<void>;
}

export function useChemicals(params: ChemicalListParams): UseChemicalsResult {
  const [chemicals, setChemicals] = useState<Chemical[]>([]);
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
      const data = await getChemicals(params);
      setChemicals(data);
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
      setChemicals([]);
    } finally {
      setIsLoading(false);
    }
  }, [params.search, params.category, params.active, params.lowStock]);

  useEffect(() => {
    void refetch();
  }, [refetch]);

  return {
    chemicals,
    isLoading,
    error,
    forbidden,
    unauthorized,
    refetch,
  };
}
