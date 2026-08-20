import type { TransactionType } from '@/features/finance/types/transaction';

export interface FinancialCategory {
  id: string;
  code: string;
  name: string;
  type: TransactionType;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CategoryListParams {
  search?: string;
  type?: TransactionType | null;
  active?: boolean | null;
}

export interface CreateCategoryPayload {
  code: string;
  name: string;
  type: TransactionType;
  active: boolean;
}

export type UpdateCategoryPayload = CreateCategoryPayload;
