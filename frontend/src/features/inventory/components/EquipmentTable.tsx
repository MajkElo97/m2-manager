import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import type { Equipment } from '@/features/inventory/types/equipment';
import {
  displayValue,
  getActiveLabel,
  getEmployeeDisplayName,
  getEquipmentConditionLabel,
} from '@/features/inventory/utils/inventoryLabels';
import { formatDateDisplay } from '@/utils/dateFormat';
import './EquipmentTable.css';

interface EquipmentTableProps {
  equipment: Equipment[];
  canEdit: boolean;
  canDelete: boolean;
  onEdit: (item: Equipment) => void;
  onDeactivate: (item: Equipment) => void;
}

function activeVariant(active: boolean): 'success' | 'neutral' {
  return active ? 'success' : 'neutral';
}

export function EquipmentTable({
  equipment,
  canEdit,
  canDelete,
  onEdit,
  onDeactivate,
}: EquipmentTableProps) {
  return (
    <div className="buildings-table-wrapper">
      <table className="buildings-table">
        <thead>
          <tr>
            <th scope="col">Kod</th>
            <th scope="col">Nazwa</th>
            <th scope="col">Kategoria</th>
            <th scope="col">Ilość</th>
            <th scope="col">Stan</th>
            <th scope="col">Lokalizacja</th>
            <th scope="col">Pracownik</th>
            <th scope="col">Data zakupu</th>
            <th scope="col">Status</th>
            <th scope="col">Akcje</th>
          </tr>
        </thead>
        <tbody>
          {equipment.map((item) => (
            <tr key={item.id}>
              <td>{item.code}</td>
              <td>{item.name}</td>
              <td>{displayValue(item.category)}</td>
              <td>{item.quantity}</td>
              <td>{getEquipmentConditionLabel(item.conditionStatus)}</td>
              <td>{displayValue(item.location)}</td>
              <td>{getEmployeeDisplayName(item.employeeName, item.employeeCode)}</td>
              <td>{formatDateDisplay(item.purchaseDate)}</td>
              <td>
                <Badge variant={activeVariant(item.active)}>{getActiveLabel(item.active)}</Badge>
              </td>
              <td>
                <div className="buildings-table__actions">
                  {canEdit ? (
                    <Button variant="secondary" size="sm" onClick={() => onEdit(item)}>
                      Edytuj
                    </Button>
                  ) : null}
                  {canDelete && item.active ? (
                    <Button variant="danger" size="sm" onClick={() => onDeactivate(item)}>
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
