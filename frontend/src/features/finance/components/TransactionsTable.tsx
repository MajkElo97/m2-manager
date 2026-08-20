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
import './TransactionsTable.css';

interface TransactionsTableProps {
  transactions: FinancialTransaction[];
  canEdit: boolean;
  canDelete: boolean;
  onEdit: (transaction: FinancialTransaction) => void;
  onCancel: (transaction: FinancialTransaction) => void;
}

export function TransactionsTable({
  transactions,
  canEdit,
  canDelete,
  onEdit,
  onCancel,
}: TransactionsTableProps) {
  return (
    <div className="buildings-table-wrapper">
      <table className="buildings-table">
        <thead>
          <tr>
            <th scope="col">Data</th>
            <th scope="col">Kod</th>
            <th scope="col">Typ</th>
            <th scope="col">Kategoria</th>
            <th scope="col">Kontrahent</th>
            <th scope="col">Budynek</th>
            <th scope="col">Dokument</th>
            <th scope="col">Netto</th>
            <th scope="col">VAT</th>
            <th scope="col">Brutto</th>
            <th scope="col">Płatność</th>
            <th scope="col">Akcje</th>
          </tr>
        </thead>
        <tbody>
          {transactions.map((transaction) => (
            <tr key={transaction.id}>
              <td>{formatDateDisplay(transaction.transactionDate)}</td>
              <td className="buildings-table__code">{transaction.code}</td>
              <td>
                <Badge variant={transactionTypeVariant(transaction.type)}>
                  {getTransactionTypeLabel(transaction.type)}
                </Badge>
              </td>
              <td>{displayValue(transaction.categoryName)}</td>
              <td>
                {getContractorDisplay(transaction.contractorName, transaction.contractorNip)}
              </td>
              <td>{getBuildingDisplay(transaction.buildingName, transaction.buildingCode)}</td>
              <td>{displayValue(transaction.documentNumber)}</td>
              <td className={amountClassName(transaction.type)}>
                {formatCurrency(transaction.netAmount)}
              </td>
              <td>{formatCurrency(transaction.vatAmount)}</td>
              <td className={amountClassName(transaction.type)}>
                {formatCurrency(transaction.grossAmount)}
              </td>
              <td>
                <PaymentStatusBadge status={transaction.paymentStatus} />
              </td>
              <td>
                <div className="buildings-table__actions">
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
                  {!canEdit && !canDelete ? (
                    <span className="buildings-table__empty-cell">—</span>
                  ) : null}
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
