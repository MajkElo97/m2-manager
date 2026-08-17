import { Button } from '@/components/ui/Button';
import { Modal } from '@/components/ui/Modal';
import type { Building } from '@/features/buildings/types/building';

interface DeactivateBuildingDialogProps {
  building: Building | null;
  loading?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

export function DeactivateBuildingDialog({
  building,
  loading = false,
  onConfirm,
  onCancel,
}: DeactivateBuildingDialogProps) {
  return (
    <Modal
      isOpen={building !== null}
      title="Dezaktywacja budynku"
      onClose={onCancel}
    >
      <p>Czy na pewno chcesz dezaktywować budynek?</p>
      {building ? (
        <p>
          <strong>{building.code}</strong> — {building.name}
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
