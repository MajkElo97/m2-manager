import { Button } from '@/components/ui/Button';
import { Modal } from '@/components/ui/Modal';
import type { FinancialCategory } from '@/features/finance/types/category';
import { getTransactionTypeLabel } from '@/features/finance/utils/financeLabels';

interface DeactivateCategoryDialogProps {
  category: FinancialCategory | null;
  loading?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

export function DeactivateCategoryDialog({
  category,
  loading = false,
  onConfirm,
  onCancel,
}: DeactivateCategoryDialogProps) {
  return (
    <Modal isOpen={category !== null} title="Dezaktywacja kategorii" onClose={onCancel}>
      <p>Czy na pewno chcesz dezaktywować tę kategorię finansową?</p>
      {category ? (
        <p>
          <strong>{category.name}</strong> ({category.code}) —{' '}
          {getTransactionTypeLabel(category.type)}
        </p>
      ) : null}
      <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1rem' }}>
        <Button variant="secondary" onClick={onCancel} disabled={loading}>
          Anuluj
        </Button>
        <Button variant="danger" loading={loading} onClick={onConfirm}>
          Dezaktywuj
        </Button>
      </div>
    </Modal>
  );
}
