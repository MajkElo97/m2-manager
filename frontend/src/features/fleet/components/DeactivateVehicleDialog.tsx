import { Button } from '@/components/ui/Button';
import { Modal } from '@/components/ui/Modal';
import type { Vehicle } from '@/features/fleet/types/vehicle';
import { getVehicleDisplayName } from '@/features/fleet/utils/vehicleLabels';

interface DeactivateVehicleDialogProps {
  vehicle: Vehicle | null;
  loading?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

export function DeactivateVehicleDialog({
  vehicle,
  loading = false,
  onConfirm,
  onCancel,
}: DeactivateVehicleDialogProps) {
  return (
    <Modal isOpen={vehicle !== null} title="Dezaktywacja pojazdu" onClose={onCancel}>
      <p>Czy na pewno chcesz dezaktywować pojazd?</p>
      {vehicle ? (
        <p>
          <strong>
            {getVehicleDisplayName(vehicle.make, vehicle.model, vehicle.productionYear)}
          </strong>{' '}
          ({vehicle.registrationNumber}, {vehicle.code})
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
