import { useCallback, useEffect, useState } from 'react';
import { getTransactions } from '@/features/finance/api/financeApi';
import { getFinanceErrorMessage } from '@/features/finance/financeMessages';
import type {
  FinancialTransaction,
  TransactionListParams,
} from '@/features/finance/types/transaction';
import { ApiError } from '@/services/apiError';

interface UseTransactionsResult {
  transactions: FinancialTransaction[];
  isLoading: boolean;
  error: string | null;
  forbidden: boolean;
  unauthorized: boolean;
  refetch: () => Promise<void>;
}

export function useTransactions(params: TransactionListParams): UseTransactionsResult {
  const [transactions, setTransactions] = useState<FinancialTransaction[]>([]);
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
      const data = await getTransactions(params);
      setTransactions(data);
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
      setTransactions([]);
    } finally {
      setIsLoading(false);
    }
  }, [
    params.search,
    params.type,
    params.categoryId,
    params.buildingId,
    params.employeeId,
    params.vehicleId,
    params.paymentStatus,
    params.status,
    params.dateFrom,
    params.dateTo,
  ]);

  useEffect(() => {
    void refetch();
  }, [refetch]);

  return {
    transactions,
    isLoading,
    error,
    forbidden,
    unauthorized,
    refetch,
  };
}
