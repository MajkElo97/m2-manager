import { useCallback, useEffect, useState } from 'react';
import { getCategories } from '@/features/finance/api/financeApi';
import { getFinanceErrorMessage } from '@/features/finance/financeMessages';
import type { CategoryListParams, FinancialCategory } from '@/features/finance/types/category';
import { useOrganizationContextKey } from '@/features/auth/useOrganizationContextKey';
import { ApiError } from '@/services/apiError';

interface UseCategoriesResult {
  categories: FinancialCategory[];
  isLoading: boolean;
  error: string | null;
  forbidden: boolean;
  unauthorized: boolean;
  refetch: () => Promise<void>;
}

export function useCategories(params: CategoryListParams): UseCategoriesResult {
  const organizationContextKey = useOrganizationContextKey();
  const [categories, setCategories] = useState<FinancialCategory[]>([]);
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
      const data = await getCategories(params);
      setCategories(data);
    } catch (err) {
      if (err instanceof ApiError && err.status === 401) {
        setUnauthorized(true);
        setError('Sesja wygasła. Zaloguj się ponownie.');
      } else if (err instanceof ApiError && err.status === 403) {
        setForbidden(true);
        setError(getFinanceErrorMessage(err));
      } else {
        setError(getFinanceErrorMessage(err));
      }
      setCategories([]);
    } finally {
      setIsLoading(false);
    }
  }, [params.search, params.type, params.active, organizationContextKey]);

  useEffect(() => {
    void refetch();
  }, [refetch]);

  return {
    categories,
    isLoading,
    error,
    forbidden,
    unauthorized,
    refetch,
  };
}
