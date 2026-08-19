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
import './EquipmentMobileList.css';

interface EquipmentMobileListProps {
  equipment: Equipment[];
  canEdit: boolean;
  canDelete: boolean;
  onEdit: (item: Equipment) => void;
  onDeactivate: (item: Equipment) => void;
}

function activeVariant(active: boolean): 'success' | 'neutral' {
  return active ? 'success' : 'neutral';
}

export function EquipmentMobileList({
  equipment,
  canEdit,
  canDelete,
  onEdit,
  onDeactivate,
}: EquipmentMobileListProps) {
  return (
    <div className="buildings-mobile-list">
      {equipment.map((item) => (
        <article key={item.id} className="buildings-mobile-card">
          <div className="buildings-mobile-card__header">
            <h3 className="buildings-mobile-card__name">{item.name}</h3>
            <Badge variant={activeVariant(item.active)}>{getActiveLabel(item.active)}</Badge>
          </div>

          <dl className="buildings-mobile-card__details">
            <div>
              <dt>Kod</dt>
              <dd>{item.code}</dd>
            </div>
            <div>
              <dt>Kategoria</dt>
              <dd>{displayValue(item.category)}</dd>
            </div>
            <div>
              <dt>Ilość</dt>
              <dd>{item.quantity}</dd>
            </div>
            <div>
              <dt>Stan</dt>
              <dd>{getEquipmentConditionLabel(item.conditionStatus)}</dd>
            </div>
            <div>
              <dt>Lokalizacja</dt>
              <dd>{displayValue(item.location)}</dd>
            </div>
            <div>
              <dt>Pracownik</dt>
              <dd>{getEmployeeDisplayName(item.employeeName, item.employeeCode)}</dd>
            </div>
            <div>
              <dt>Data zakupu</dt>
              <dd>{formatDateDisplay(item.purchaseDate)}</dd>
            </div>
          </dl>

          <div className="buildings-mobile-card__actions">
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
          </div>
        </article>
      ))}
    </div>
  );
}
