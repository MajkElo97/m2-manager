import { Button } from '@/components/ui/Button';
import { Modal } from '@/components/ui/Modal';
import type { Staircase } from '@/features/staircases/types/staircase';

interface DeleteStaircaseDialogProps {
  staircase: Staircase | null;
  loading?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

export function DeleteStaircaseDialog({
  staircase,
  loading = false,
  onConfirm,
  onCancel,
}: DeleteStaircaseDialogProps) {
  return (
    <Modal isOpen={staircase !== null} title="Usuwanie klatki" onClose={onCancel}>
      <p>Czy na pewno chcesz usunąć tę klatkę?</p>
      {staircase ? (
        <p>
          <strong>{staircase.code}</strong> — klatka {staircase.designation}
        </p>
      ) : null}
      <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1rem' }}>
        <Button variant="secondary" onClick={onCancel} disabled={loading}>
          Anuluj
        </Button>
        <Button variant="danger" loading={loading} onClick={onConfirm}>
          Usuń
        </Button>
      </div>
    </Modal>
  );
}
