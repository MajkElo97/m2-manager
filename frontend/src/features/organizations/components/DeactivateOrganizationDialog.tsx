import { Modal } from '@/components/ui/Modal';
import { Button } from '@/components/ui/Button';
import type { OrganizationListItem } from '@/features/organizations/types/organization';

interface DeactivateOrganizationDialogProps {
  organization: OrganizationListItem | null;
  loading?: boolean;
  onConfirm: () => Promise<void>;
  onClose: () => void;
}

export function DeactivateOrganizationDialog({
  organization,
  loading = false,
  onConfirm,
  onClose,
}: DeactivateOrganizationDialogProps) {
  return (
    <Modal
      isOpen={organization != null}
      title="Dezaktywuj organizację"
      onClose={() => {
        if (!loading) {
          onClose();
        }
      }}
    >
      <p>
        Czy na pewno chcesz dezaktywować organizację <strong>{organization?.name}</strong>?
      </p>
      <div className="organization-form__actions">
        <Button type="button" variant="secondary" onClick={onClose} disabled={loading}>
          Anuluj
        </Button>
        <Button type="button" variant="danger" disabled={loading} onClick={() => void onConfirm()}>
          Dezaktywuj
        </Button>
      </div>
    </Modal>
  );
}
