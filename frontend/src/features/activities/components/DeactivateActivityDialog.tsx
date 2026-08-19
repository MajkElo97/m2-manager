import { Button } from '@/components/ui/Button';
import { Modal } from '@/components/ui/Modal';
import type { Activity } from '@/features/activities/types/activity';

interface DeactivateActivityDialogProps {
  activity: Activity | null;
  loading?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

export function DeactivateActivityDialog({
  activity,
  loading = false,
  onConfirm,
  onCancel,
}: DeactivateActivityDialogProps) {
  return (
    <Modal
      isOpen={activity !== null}
      title="Dezaktywacja czynności"
      onClose={onCancel}
    >
      <p>Czy na pewno chcesz dezaktywować czynność?</p>
      {activity ? (
        <p>
          <strong>{activity.code}</strong> — {activity.name}
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
