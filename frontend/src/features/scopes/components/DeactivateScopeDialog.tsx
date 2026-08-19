import { Button } from '@/components/ui/Button';
import { Modal } from '@/components/ui/Modal';
import type { Scope } from '@/features/scopes/types/scope';

interface DeactivateScopeDialogProps {
  scope: Scope | null;
  loading?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

export function DeactivateScopeDialog({
  scope,
  loading = false,
  onConfirm,
  onCancel,
}: DeactivateScopeDialogProps) {
  return (
    <Modal
      isOpen={scope !== null}
      title="Dezaktywacja zakresu"
      onClose={onCancel}
    >
      <p>Czy na pewno chcesz dezaktywować zakres?</p>
      {scope ? (
        <p>
          <strong>{scope.code}</strong>
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
