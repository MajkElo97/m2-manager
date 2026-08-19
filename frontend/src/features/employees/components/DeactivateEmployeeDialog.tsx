import { Button } from '@/components/ui/Button';
import { Modal } from '@/components/ui/Modal';
import type { Employee } from '@/features/employees/types/employee';
import { getFullName } from '@/features/employees/utils/employeeLabels';

interface DeactivateEmployeeDialogProps {
  employee: Employee | null;
  loading?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

export function DeactivateEmployeeDialog({
  employee,
  loading = false,
  onConfirm,
  onCancel,
}: DeactivateEmployeeDialogProps) {
  return (
    <Modal isOpen={employee !== null} title="Dezaktywacja pracownika" onClose={onCancel}>
      <p>Czy na pewno chcesz dezaktywować pracownika?</p>
      {employee ? (
        <p>
          <strong>{getFullName(employee.firstName, employee.lastName)}</strong> ({employee.code})
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
