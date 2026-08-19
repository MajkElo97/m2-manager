import { Button } from '@/components/ui/Button';
import { Modal } from '@/components/ui/Modal';
import { getFullName } from '@/features/employees/utils/employeeLabels';
import type { Supervisor } from '@/features/supervisors/types/supervisor';

interface DeactivateSupervisorDialogProps {
  supervisor: Supervisor | null;
  loading?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

export function DeactivateSupervisorDialog({
  supervisor,
  loading = false,
  onConfirm,
  onCancel,
}: DeactivateSupervisorDialogProps) {
  return (
    <Modal isOpen={supervisor !== null} title="Dezaktywacja opiekuna" onClose={onCancel}>
      <p>Czy na pewno chcesz dezaktywować opiekuna?</p>
      {supervisor ? (
        <p>
          <strong>{getFullName(supervisor.firstName, supervisor.lastName)}</strong> ({supervisor.code})
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
