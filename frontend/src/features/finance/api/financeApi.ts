import type {
  CreateCategoryPayload,
  CategoryListParams,
  FinancialCategory,
  UpdateCategoryPayload,
} from '@/features/finance/types/category';
import type { FinanceSummary, FinanceSummaryParams } from '@/features/finance/types/summary';
import type {
  CreateTransactionPayload,
  FinancialTransaction,
  TransactionListParams,
  UpdateTransactionPayload,
} from '@/features/finance/types/transaction';
import { apiClient } from '@/services/apiClient';

function buildTransactionQuery(params: TransactionListParams): string {
  const searchParams = new URLSearchParams();

  if (params.search?.trim()) {
    searchParams.set('search', params.search.trim());
  }

  if (params.type) {
    searchParams.set('type', params.type);
  }

  if (params.categoryId) {
    searchParams.set('categoryId', params.categoryId);
  }

  if (params.buildingId) {
    searchParams.set('buildingId', params.buildingId);
  }

  if (params.employeeId) {
    searchParams.set('employeeId', params.employeeId);
  }

  if (params.vehicleId) {
    searchParams.set('vehicleId', params.vehicleId);
  }

  if (params.paymentStatus) {
    searchParams.set('paymentStatus', params.paymentStatus);
  }

  if (params.status) {
    searchParams.set('status', params.status);
  }

  if (params.dateFrom) {
    searchParams.set('dateFrom', params.dateFrom);
  }

  if (params.dateTo) {
    searchParams.set('dateTo', params.dateTo);
  }

  const query = searchParams.toString();
  return query ? `?${query}` : '';
}

function buildCategoryQuery(params: CategoryListParams): string {
  const searchParams = new URLSearchParams();

  if (params.search?.trim()) {
    searchParams.set('search', params.search.trim());
  }

  if (params.type) {
    searchParams.set('type', params.type);
  }

  if (params.active !== null && params.active !== undefined) {
    searchParams.set('active', String(params.active));
  }

  const query = searchParams.toString();
  return query ? `?${query}` : '';
}

function buildSummaryQuery(params: FinanceSummaryParams): string {
  const searchParams = new URLSearchParams();
  searchParams.set('dateFrom', params.dateFrom);
  searchParams.set('dateTo', params.dateTo);
  return `?${searchParams.toString()}`;
}

export function getTransactions(
  params: TransactionListParams = {},
): Promise<FinancialTransaction[]> {
  return apiClient.get<FinancialTransaction[]>(
    `/api/finance/transactions${buildTransactionQuery(params)}`,
  );
}

export function getTransactionById(id: string): Promise<FinancialTransaction> {
  return apiClient.get<FinancialTransaction>(`/api/finance/transactions/${id}`);
}

export function createTransaction(
  data: CreateTransactionPayload,
): Promise<FinancialTransaction> {
  return apiClient.post<FinancialTransaction>('/api/finance/transactions', data);
}

export function updateTransaction(
  id: string,
  data: UpdateTransactionPayload,
): Promise<FinancialTransaction> {
  return apiClient.put<FinancialTransaction>(`/api/finance/transactions/${id}`, data);
}

export function cancelTransaction(id: string): Promise<void> {
  return apiClient.delete<void>(`/api/finance/transactions/${id}`);
}

export function getCategories(params: CategoryListParams = {}): Promise<FinancialCategory[]> {
  return apiClient.get<FinancialCategory[]>(
    `/api/finance/categories${buildCategoryQuery(params)}`,
  );
}

export function getCategoryById(id: string): Promise<FinancialCategory> {
  return apiClient.get<FinancialCategory>(`/api/finance/categories/${id}`);
}

export function createCategory(data: CreateCategoryPayload): Promise<FinancialCategory> {
  return apiClient.post<FinancialCategory>('/api/finance/categories', data);
}

export function updateCategory(
  id: string,
  data: UpdateCategoryPayload,
): Promise<FinancialCategory> {
  return apiClient.put<FinancialCategory>(`/api/finance/categories/${id}`, data);
}

export function deactivateCategory(id: string): Promise<void> {
  return apiClient.delete<void>(`/api/finance/categories/${id}`);
}

export function getFinanceSummary(params: FinanceSummaryParams): Promise<FinanceSummary> {
  return apiClient.get<FinanceSummary>(`/api/finance/summary${buildSummaryQuery(params)}`);
}
