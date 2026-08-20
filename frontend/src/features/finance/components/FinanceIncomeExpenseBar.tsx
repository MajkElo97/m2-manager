import type { FinanceSummary } from '@/features/finance/types/summary';
import { formatCurrency } from '@/features/finance/utils/formatCurrency';
import './FinanceIncomeExpenseBar.css';

interface FinanceIncomeExpenseBarProps {
  summary: FinanceSummary | null;
}

export function FinanceIncomeExpenseBar({ summary }: FinanceIncomeExpenseBarProps) {
  if (!summary) {
    return null;
  }

  const income = Math.max(summary.incomeNet, 0);
  const expense = Math.max(summary.expenseNet, 0);
  const total = income + expense;

  if (total === 0) {
    return null;
  }

  const incomePercent = (income / total) * 100;
  const expensePercent = (expense / total) * 100;

  return (
    <div className="finance-income-expense-bar">
      <div className="finance-income-expense-bar__legend">
        <span className="finance-income-expense-bar__legend-item finance-income-expense-bar__legend-item--income">
          Przychody: {formatCurrency(income)}
        </span>
        <span className="finance-income-expense-bar__legend-item finance-income-expense-bar__legend-item--expense">
          Koszty: {formatCurrency(expense)}
        </span>
      </div>
      <div
        className="finance-income-expense-bar__track"
        role="img"
        aria-label={`Przychody ${Math.round(incomePercent)}%, koszty ${Math.round(expensePercent)}%`}
      >
        <div
          className="finance-income-expense-bar__segment finance-income-expense-bar__segment--income"
          style={{ width: `${incomePercent}%` }}
        />
        <div
          className="finance-income-expense-bar__segment finance-income-expense-bar__segment--expense"
          style={{ width: `${expensePercent}%` }}
        />
      </div>
    </div>
  );
}
