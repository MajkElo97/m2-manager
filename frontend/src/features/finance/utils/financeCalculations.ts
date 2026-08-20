import type { FinancialTransaction } from '@/features/finance/types/transaction';
import type { FinanceSummary } from '@/features/finance/types/summary';

function sumNet(transactions: FinancialTransaction[]): number {
  return transactions.reduce((total, transaction) => total + transaction.netAmount, 0);
}

function sumUnpaid(
  transactions: FinancialTransaction[],
  type: FinancialTransaction['type'],
  statuses: FinancialTransaction['paymentStatus'][],
): number {
  return transactions
    .filter(
      (transaction) =>
        transaction.type === type &&
        transaction.status === 'ACTIVE' &&
        statuses.includes(transaction.paymentStatus),
    )
    .reduce((total, transaction) => total + transaction.netAmount, 0);
}

export function computeSummaryFromTransactions(
  transactions: FinancialTransaction[],
): FinanceSummary {
  const active = transactions.filter((transaction) => transaction.status === 'ACTIVE');
  const income = active.filter((transaction) => transaction.type === 'INCOME');
  const expense = active.filter((transaction) => transaction.type === 'EXPENSE');

  const incomeNet = sumNet(income);
  const expenseNet = sumNet(expense);

  return {
    incomeNet,
    incomeGross: income.reduce((total, transaction) => total + transaction.grossAmount, 0),
    expenseNet,
    expenseGross: expense.reduce((total, transaction) => total + transaction.grossAmount, 0),
    operatingResultNet: incomeNet - expenseNet,
    receivables: sumUnpaid(active, 'INCOME', ['TO_PAY', 'OVERDUE']),
    liabilities: sumUnpaid(active, 'EXPENSE', ['TO_PAY', 'OVERDUE']),
    overdueReceivables: sumUnpaid(active, 'INCOME', ['OVERDUE']),
    overdueLiabilities: sumUnpaid(active, 'EXPENSE', ['OVERDUE']),
  };
}

export function calculateAmounts(
  netAmount: number,
  vatRate: number | null,
): { vatAmount: number; grossAmount: number } {
  const rate = vatRate ?? 0;
  const vatAmount = Math.round(netAmount * (rate / 100) * 100) / 100;
  const grossAmount = Math.round((netAmount + vatAmount) * 100) / 100;
  return { vatAmount, grossAmount };
}
