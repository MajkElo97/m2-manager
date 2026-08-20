import { Button } from '@/components/ui/Button';
import { Modal } from '@/components/ui/Modal';
import type { FinancialTransaction } from '@/features/finance/types/transaction';
import { formatCurrency } from '@/features/finance/utils/formatCurrency';
import { getTransactionTypeLabel } from '@/features/finance/utils/financeLabels';

interface CancelTransactionDialogProps {
  transaction: FinancialTransaction | null;
  loading?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

export function CancelTransactionDialog({
  transaction,
  loading = false,
  onConfirm,
  onCancel,
}: CancelTransactionDialogProps) {
  return (
    <Modal isOpen={transaction !== null} title="Anulowanie operacji" onClose={onCancel}>
      <p>Czy na pewno chcesz anulować tę operację finansową?</p>
      {transaction ? (
        <p>
          <strong>{transaction.code}</strong> — {getTransactionTypeLabel(transaction.type)},{' '}
          {formatCurrency(transaction.grossAmount)}
        </p>
      ) : null}
      <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1rem' }}>
        <Button variant="secondary" onClick={onCancel} disabled={loading}>
          Anuluj
        </Button>
        <Button variant="danger" loading={loading} onClick={onConfirm}>
          Potwierdź anulowanie
        </Button>
      </div>
    </Modal>
  );
}
