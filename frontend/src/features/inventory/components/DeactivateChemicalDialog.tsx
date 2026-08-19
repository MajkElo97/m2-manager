import { Button } from '@/components/ui/Button';
import { Modal } from '@/components/ui/Modal';
import type { Chemical } from '@/features/inventory/types/chemical';

interface DeactivateChemicalDialogProps {
  chemical: Chemical | null;
  loading?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

export function DeactivateChemicalDialog({
  chemical,
  loading = false,
  onConfirm,
  onCancel,
}: DeactivateChemicalDialogProps) {
  return (
    <Modal isOpen={chemical !== null} title="Dezaktywacja chemii" onClose={onCancel}>
      <p>Czy na pewno chcesz dezaktywować pozycję chemiczną?</p>
      {chemical ? (
        <p>
          <strong>{chemical.name}</strong> ({chemical.code})
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
