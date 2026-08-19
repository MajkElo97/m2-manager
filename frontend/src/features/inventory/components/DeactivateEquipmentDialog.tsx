import { Button } from '@/components/ui/Button';
import { Modal } from '@/components/ui/Modal';
import type { Equipment } from '@/features/inventory/types/equipment';

interface DeactivateEquipmentDialogProps {
  equipment: Equipment | null;
  loading?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

export function DeactivateEquipmentDialog({
  equipment,
  loading = false,
  onConfirm,
  onCancel,
}: DeactivateEquipmentDialogProps) {
  return (
    <Modal isOpen={equipment !== null} title="Dezaktywacja sprzętu" onClose={onCancel}>
      <p>Czy na pewno chcesz dezaktywować sprzęt?</p>
      {equipment ? (
        <p>
          <strong>{equipment.name}</strong> ({equipment.code})
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
