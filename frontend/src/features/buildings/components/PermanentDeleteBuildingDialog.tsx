import { useEffect, useState } from 'react';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Modal } from '@/components/ui/Modal';
import type { Building } from '@/features/buildings/types/building';
import './PermanentDeleteBuildingDialog.css';

interface PermanentDeleteBuildingDialogProps {
  building: Building | null;
  loading?: boolean;
  error?: string | null;
  onConfirm: () => void;
  onCancel: () => void;
}

export function PermanentDeleteBuildingDialog({
  building,
  loading = false,
  error = null,
  onConfirm,
  onCancel,
}: PermanentDeleteBuildingDialogProps) {
  const [confirmationName, setConfirmationName] = useState('');

  const expectedName = building?.name ?? '';
  const canConfirm = confirmationName === expectedName && !loading;

  useEffect(() => {
    setConfirmationName('');
  }, [building?.id]);

  const handleClose = () => {
    if (!loading) {
      setConfirmationName('');
      onCancel();
    }
  };

  return (
    <Modal isOpen={building !== null} title="Usuń budynek na stałe" onClose={handleClose}>
      <div className="permanent-delete-building-dialog">
        <p className="permanent-delete-building-dialog__warning">
          Ta operacja jest nieodwracalna. Budynek oraz dane, które można bezpiecznie usunąć wraz
          z nim, zostaną trwale usunięte z bazy.
        </p>

        {building ? (
          <>
            <p>
              Usuń budynek: <strong>{building.name}</strong>
            </p>
            <p className="permanent-delete-building-dialog__code">{building.code}</p>

            <Input
              label="Pole potwierdzenia"
              name="confirmationName"
              placeholder="wpisz nazwę budynku"
              value={confirmationName}
              onChange={(event) => setConfirmationName(event.target.value)}
              disabled={loading}
            />
          </>
        ) : null}

        {error ? (
          <p role="alert" className="permanent-delete-building-dialog__error">
            {error}
          </p>
        ) : null}

        <div className="permanent-delete-building-dialog__actions">
          <Button variant="secondary" onClick={handleClose} disabled={loading}>
            Anuluj
          </Button>
          <Button variant="danger" loading={loading} disabled={!canConfirm} onClick={onConfirm}>
            Usuń na stałe
          </Button>
        </div>
      </div>
    </Modal>
  );
}
