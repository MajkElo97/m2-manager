import { useCallback, useEffect, useState } from 'react';
import { getFinanceSummary } from '@/features/finance/api/financeApi';
import { getFinanceErrorMessage } from '@/features/finance/financeMessages';
import type { FinanceSummary, FinanceSummaryParams } from '@/features/finance/types/summary';
import { ApiError } from '@/services/apiError';

interface UseFinanceSummaryResult {
  summary: FinanceSummary | null;
  isLoading: boolean;
  error: string | null;
  forbidden: boolean;
  unauthorized: boolean;
  refetch: () => Promise<void>;
}

export function useFinanceSummary(params: FinanceSummaryParams): UseFinanceSummaryResult {
  const [summary, setSummary] = useState<FinanceSummary | null>(null);
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
      const data = await getFinanceSummary(params);
      setSummary(data);
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
      setSummary(null);
    } finally {
      setIsLoading(false);
    }
  }, [params.dateFrom, params.dateTo]);

  useEffect(() => {
    void refetch();
  }, [refetch]);

  return {
    summary,
    isLoading,
    error,
    forbidden,
    unauthorized,
    refetch,
  };
}
