import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import type { Chemical } from '@/features/inventory/types/chemical';
import {
  displayValue,
  formatMinimumStock,
  formatQuantity,
  getChemicalUnitLabel,
  getStockStatusLabel,
} from '@/features/inventory/utils/inventoryLabels';
import './ChemicalsMobileList.css';

interface ChemicalsMobileListProps {
  chemicals: Chemical[];
  canEdit: boolean;
  canDelete: boolean;
  onEdit: (item: Chemical) => void;
  onDeactivate: (item: Chemical) => void;
}

function stockVariant(lowStock: boolean): 'warning' | 'success' {
  return lowStock ? 'warning' : 'success';
}

export function ChemicalsMobileList({
  chemicals,
  canEdit,
  canDelete,
  onEdit,
  onDeactivate,
}: ChemicalsMobileListProps) {
  return (
    <div className="buildings-mobile-list">
      {chemicals.map((item) => (
        <article key={item.id} className="buildings-mobile-card">
          <div className="buildings-mobile-card__header">
            <h3 className="buildings-mobile-card__name">{item.name}</h3>
            {item.minimumStock != null ? (
              <Badge variant={stockVariant(item.lowStock)}>
                {getStockStatusLabel(item.lowStock)}
              </Badge>
            ) : null}
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
              <dd>{formatQuantity(item.quantity)}</dd>
            </div>
            <div>
              <dt>Jednostka</dt>
              <dd>{getChemicalUnitLabel(item.unit)}</dd>
            </div>
            <div>
              <dt>Stan minimalny</dt>
              <dd>{formatMinimumStock(item.minimumStock)}</dd>
            </div>
            <div>
              <dt>Lokalizacja</dt>
              <dd>{displayValue(item.location)}</dd>
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
