import type { FinanceSummary } from '@/features/finance/types/summary';
import { formatCurrency } from '@/features/finance/utils/formatCurrency';
import './FinanceSummaryCards.css';

interface FinanceSummaryCardsProps {
  summary: FinanceSummary | null;
  loading?: boolean;
}

interface SummaryCardConfig {
  key: keyof FinanceSummary | 'operatingResultNet';
  label: string;
  valueKey: keyof FinanceSummary;
  tone?: 'income' | 'expense' | 'result';
}

const CARDS: SummaryCardConfig[] = [
  { key: 'incomeNet', label: 'PRZYCHODY', valueKey: 'incomeNet', tone: 'income' },
  { key: 'expenseNet', label: 'KOSZTY', valueKey: 'expenseNet', tone: 'expense' },
  { key: 'operatingResultNet', label: 'WYNIK', valueKey: 'operatingResultNet', tone: 'result' },
  { key: 'receivables', label: 'NALEŻNOŚCI', valueKey: 'receivables' },
  { key: 'liabilities', label: 'ZOBOWIĄZANIA', valueKey: 'liabilities' },
];

function getResultClass(value: number): string {
  if (value > 0) {
    return 'finance-summary-cards__value--income';
  }

  if (value < 0) {
    return 'finance-summary-cards__value--expense';
  }

  return '';
}

export function FinanceSummaryCards({ summary, loading = false }: FinanceSummaryCardsProps) {
  return (
    <div className="finance-summary-cards">
      {CARDS.map((card) => {
        const value = summary?.[card.valueKey] ?? null;
        const toneClass =
          card.tone === 'income'
            ? 'finance-summary-cards__value--income'
            : card.tone === 'expense'
              ? 'finance-summary-cards__value--expense'
              : card.tone === 'result' && value != null
                ? getResultClass(value)
                : '';

        return (
          <article key={card.key} className="finance-summary-cards__card">
            <p className="finance-summary-cards__label">{card.label}</p>
            <p className={`finance-summary-cards__value ${toneClass}`.trim()}>
              {loading ? '…' : formatCurrency(value)}
            </p>
          </article>
        );
      })}
    </div>
  );
}
