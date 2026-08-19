import { Button } from '@/components/ui/Button';
import { Modal } from '@/components/ui/Modal';
import { getFullName } from '@/features/users/utils/userLabels';
import type { User } from '@/features/users/types/user';

interface DeactivateUserDialogProps {
  user: User | null;
  loading?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

export function DeactivateUserDialog({
  user,
  loading = false,
  onConfirm,
  onCancel,
}: DeactivateUserDialogProps) {
  return (
    <Modal isOpen={user !== null} title="Dezaktywacja użytkownika" onClose={onCancel}>
      <p>Czy na pewno chcesz dezaktywować użytkownika?</p>
      {user ? (
        <p>
          <strong>{getFullName(user.firstName, user.lastName)}</strong> ({user.email})
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
