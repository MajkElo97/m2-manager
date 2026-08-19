import { Button } from '@/components/ui/Button';
import { Modal } from '@/components/ui/Modal';
import type { Role } from '@/features/roles/types/role';

interface DeactivateRoleDialogProps {
  role: Role | null;
  loading?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

export function DeactivateRoleDialog({
  role,
  loading = false,
  onConfirm,
  onCancel,
}: DeactivateRoleDialogProps) {
  return (
    <Modal isOpen={role !== null} title="Dezaktywacja roli" onClose={onCancel}>
      <p>Czy na pewno chcesz dezaktywować rolę?</p>
      {role ? (
        <p>
          <strong>{role.name}</strong>
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
