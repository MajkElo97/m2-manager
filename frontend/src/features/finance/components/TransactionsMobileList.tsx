import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { PaymentStatusBadge } from '@/features/finance/components/PaymentStatusBadge';
import type { FinancialTransaction } from '@/features/finance/types/transaction';
import {
  amountClassName,
  displayValue,
  getBuildingDisplay,
  getContractorDisplay,
  getTransactionTypeLabel,
  transactionTypeVariant,
} from '@/features/finance/utils/financeLabels';
import { formatCurrency } from '@/features/finance/utils/formatCurrency';
import { formatDateDisplay } from '@/utils/dateFormat';
import './TransactionsMobileList.css';

interface TransactionsMobileListProps {
  transactions: FinancialTransaction[];
  canEdit: boolean;
  canDelete: boolean;
  onEdit: (transaction: FinancialTransaction) => void;
  onCancel: (transaction: FinancialTransaction) => void;
}

export function TransactionsMobileList({
  transactions,
  canEdit,
  canDelete,
  onEdit,
  onCancel,
}: TransactionsMobileListProps) {
  return (
    <div className="buildings-mobile-list">
      {transactions.map((transaction) => (
        <article key={transaction.id} className="buildings-mobile-card">
          <div className="buildings-mobile-card__header">
            <h3 className="buildings-mobile-card__name">
              {transaction.code} — {formatDateDisplay(transaction.transactionDate)}
            </h3>
            <Badge variant={transactionTypeVariant(transaction.type)}>
              {getTransactionTypeLabel(transaction.type)}
            </Badge>
          </div>

          <dl className="buildings-mobile-card__details">
            <div>
              <dt>Kategoria</dt>
              <dd>{displayValue(transaction.categoryName)}</dd>
            </div>
            <div>
              <dt>Kontrahent</dt>
              <dd>
                {getContractorDisplay(transaction.contractorName, transaction.contractorNip)}
              </dd>
            </div>
            <div>
              <dt>Budynek</dt>
              <dd>{getBuildingDisplay(transaction.buildingName, transaction.buildingCode)}</dd>
            </div>
            <div>
              <dt>Dokument</dt>
              <dd>{displayValue(transaction.documentNumber)}</dd>
            </div>
            <div>
              <dt>Netto</dt>
              <dd className={amountClassName(transaction.type)}>
                {formatCurrency(transaction.netAmount)}
              </dd>
            </div>
            <div>
              <dt>VAT</dt>
              <dd>{formatCurrency(transaction.vatAmount)}</dd>
            </div>
            <div>
              <dt>Brutto</dt>
              <dd className={amountClassName(transaction.type)}>
                {formatCurrency(transaction.grossAmount)}
              </dd>
            </div>
            <div>
              <dt>Płatność</dt>
              <dd>
                <PaymentStatusBadge status={transaction.paymentStatus} />
              </dd>
            </div>
          </dl>

          <div className="buildings-mobile-card__actions">
            {canEdit && transaction.status === 'ACTIVE' ? (
              <Button variant="secondary" size="sm" onClick={() => onEdit(transaction)}>
                Edytuj
              </Button>
            ) : null}
            {canDelete && transaction.status === 'ACTIVE' ? (
              <Button variant="danger" size="sm" onClick={() => onCancel(transaction)}>
                Anuluj
              </Button>
            ) : null}
          </div>
        </article>
      ))}
    </div>
  );
}
