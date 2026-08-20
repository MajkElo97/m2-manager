import type { TransactionType, PaymentStatus, TransactionStatus } from '@/features/finance/types/transaction';

type BadgeVariant = 'neutral' | 'success' | 'warning' | 'danger';

export function getTransactionTypeLabel(type: TransactionType): string {
  switch (type) {
    case 'INCOME':
      return 'Przychód';
    case 'EXPENSE':
      return 'Koszt';
  }
}

export function transactionTypeVariant(type: TransactionType): BadgeVariant {
  return type === 'INCOME' ? 'success' : 'danger';
}

export function getPaymentStatusLabel(status: PaymentStatus): string {
  switch (status) {
    case 'NOT_APPLICABLE':
      return 'Nie dotyczy';
    case 'TO_PAY':
      return 'Do zapłaty';
    case 'PAID':
      return 'Opłacone';
    case 'OVERDUE':
      return 'Przeterminowane';
  }
}

export function paymentStatusVariant(status: PaymentStatus): BadgeVariant {
  switch (status) {
    case 'NOT_APPLICABLE':
      return 'neutral';
    case 'TO_PAY':
      return 'warning';
    case 'PAID':
      return 'success';
    case 'OVERDUE':
      return 'danger';
  }
}

export function getTransactionStatusLabel(status: TransactionStatus): string {
  switch (status) {
    case 'ACTIVE':
      return 'Aktywna';
    case 'CANCELLED':
      return 'Anulowana';
  }
}

export function displayValue(value: string | null | undefined): string {
  return value?.trim() ? value : '—';
}

export function getContractorDisplay(
  name: string | null,
  nip: string | null,
): string {
  if (!name?.trim()) {
    return '—';
  }

  if (nip?.trim()) {
    return `${name} (NIP: ${nip})`;
  }

  return name;
}

export function getBuildingDisplay(
  name: string | null,
  code: string | null,
): string {
  if (!name?.trim()) {
    return '—';
  }

  if (code?.trim()) {
    return `${name} (${code})`;
  }

  return name;
}

export function amountClassName(type: TransactionType): string {
  return type === 'INCOME'
    ? 'finance-amount finance-amount--income'
    : 'finance-amount finance-amount--expense';
}
