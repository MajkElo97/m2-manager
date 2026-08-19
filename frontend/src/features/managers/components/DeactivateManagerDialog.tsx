import { Button } from '@/components/ui/Button';
import { Modal } from '@/components/ui/Modal';
import type { Manager } from '@/features/managers/types/manager';

interface DeactivateManagerDialogProps {
  manager: Manager | null;
  loading?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

export function DeactivateManagerDialog({
  manager,
  loading = false,
  onConfirm,
  onCancel,
}: DeactivateManagerDialogProps) {
  return (
    <Modal isOpen={manager !== null} title="Dezaktywacja zarządcy" onClose={onCancel}>
      <p>Czy na pewno chcesz dezaktywować zarządcę?</p>
      {manager ? (
        <p>
          <strong>{manager.name}</strong> ({manager.code})
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
