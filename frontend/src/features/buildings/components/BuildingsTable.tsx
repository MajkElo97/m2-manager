import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import type { Building } from '@/features/buildings/types/building';
import { formatDateDisplay } from '@/utils/dateFormat';
import './BuildingsTable.css';

interface BuildingsTableProps {
  buildings: Building[];
  canEdit: boolean;
  canDelete: boolean;
  onEdit: (building: Building) => void;
  onDeactivate: (building: Building) => void;
}

function statusLabel(status: Building['status']): string {
  return status === 'ACTIVE' ? 'Aktywny' : 'Nieaktywny';
}

function statusVariant(status: Building['status']): 'success' | 'neutral' {
  return status === 'ACTIVE' ? 'success' : 'neutral';
}

export function BuildingsTable({
  buildings,
  canEdit,
  canDelete,
  onEdit,
  onDeactivate,
}: BuildingsTableProps) {
  return (
    <div className="buildings-table-wrapper">
      <table className="buildings-table">
        <thead>
          <tr>
            <th scope="col">Kod</th>
            <th scope="col">Nazwa</th>
            <th scope="col">Adres</th>
            <th scope="col">Miasto</th>
            <th scope="col">Zarządca</th>
            <th scope="col">Opiekun</th>
            <th scope="col">Pracownik</th>
            <th scope="col">Start obsługi</th>
            <th scope="col">Wypowiedzenie</th>
            <th scope="col">Status</th>
            <th scope="col">Akcje</th>
          </tr>
        </thead>
        <tbody>
          {buildings.map((building) => (
            <tr key={building.id}>
              <td className="buildings-table__code">{building.code}</td>
              <td>{building.name}</td>
              <td>{building.address}</td>
              <td>{building.city}</td>
              <td>{building.managerCode ?? '—'}</td>
              <td>{building.supervisorCode ?? '—'}</td>
              <td>{building.employeeCode ?? '—'}</td>
              <td>{formatDateDisplay(building.serviceStartDate)}</td>
              <td>{building.noticePeriodMonths} mies.</td>
              <td>
                <Badge variant={statusVariant(building.status)}>
                  {statusLabel(building.status)}
                </Badge>
              </td>
              <td>
                <div className="buildings-table__actions">
                  {canEdit ? (
                    <Button variant="secondary" size="sm" onClick={() => onEdit(building)}>
                      Edytuj
                    </Button>
                  ) : null}
                  {canDelete && building.status === 'ACTIVE' ? (
                    <Button variant="danger" size="sm" onClick={() => onDeactivate(building)}>
                      Dezaktywuj
                    </Button>
                  ) : null}
                  {!canEdit && !canDelete ? (
                    <span className="buildings-table__empty-cell">—</span>
                  ) : null}
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
