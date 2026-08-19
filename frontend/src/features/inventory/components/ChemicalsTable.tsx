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
import './ChemicalsTable.css';

interface ChemicalsTableProps {
  chemicals: Chemical[];
  canEdit: boolean;
  canDelete: boolean;
  onEdit: (item: Chemical) => void;
  onDeactivate: (item: Chemical) => void;
}

function stockVariant(lowStock: boolean): 'warning' | 'success' {
  return lowStock ? 'warning' : 'success';
}

export function ChemicalsTable({
  chemicals,
  canEdit,
  canDelete,
  onEdit,
  onDeactivate,
}: ChemicalsTableProps) {
  return (
    <div className="buildings-table-wrapper">
      <table className="buildings-table">
        <thead>
          <tr>
            <th scope="col">Kod</th>
            <th scope="col">Nazwa</th>
            <th scope="col">Kategoria</th>
            <th scope="col">Ilość</th>
            <th scope="col">Jednostka</th>
            <th scope="col">Stan minimalny</th>
            <th scope="col">Status magazynowy</th>
            <th scope="col">Lokalizacja</th>
            <th scope="col">Akcje</th>
          </tr>
        </thead>
        <tbody>
          {chemicals.map((item) => (
            <tr key={item.id}>
              <td>{item.code}</td>
              <td>{item.name}</td>
              <td>{displayValue(item.category)}</td>
              <td>{formatQuantity(item.quantity)}</td>
              <td>{getChemicalUnitLabel(item.unit)}</td>
              <td>{formatMinimumStock(item.minimumStock)}</td>
              <td>
                {item.minimumStock != null ? (
                  <Badge variant={stockVariant(item.lowStock)}>
                    {getStockStatusLabel(item.lowStock)}
                  </Badge>
                ) : (
                  '—'
                )}
              </td>
              <td>{displayValue(item.location)}</td>
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
