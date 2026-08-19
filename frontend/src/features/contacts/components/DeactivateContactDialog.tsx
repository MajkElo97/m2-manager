import { Button } from '@/components/ui/Button';
import { Modal } from '@/components/ui/Modal';
import type { Contact } from '@/features/contacts/types/contact';
import { getFullName } from '@/features/employees/utils/employeeLabels';

interface DeactivateContactDialogProps {
  contact: Contact | null;
  loading?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

export function DeactivateContactDialog({
  contact,
  loading = false,
  onConfirm,
  onCancel,
}: DeactivateContactDialogProps) {
  return (
    <Modal isOpen={contact !== null} title="Dezaktywacja kontaktu" onClose={onCancel}>
      <p>Czy na pewno chcesz dezaktywować kontakt?</p>
      {contact ? (
        <p>
          <strong>{getFullName(contact.firstName, contact.lastName)}</strong>
          {contact.functionTitle ? ` — ${contact.functionTitle}` : ''}
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
