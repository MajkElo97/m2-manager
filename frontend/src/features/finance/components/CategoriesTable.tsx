import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import type { FinancialCategory } from '@/features/finance/types/category';
import {
  getTransactionTypeLabel,
  transactionTypeVariant,
} from '@/features/finance/utils/financeLabels';
import './CategoriesTable.css';

interface CategoriesTableProps {
  categories: FinancialCategory[];
  canEdit: boolean;
  canDelete: boolean;
  onEdit: (category: FinancialCategory) => void;
  onDeactivate: (category: FinancialCategory) => void;
}

export function CategoriesTable({
  categories,
  canEdit,
  canDelete,
  onEdit,
  onDeactivate,
}: CategoriesTableProps) {
  return (
    <div className="buildings-table-wrapper">
      <table className="buildings-table">
        <thead>
          <tr>
            <th scope="col">Kod</th>
            <th scope="col">Nazwa</th>
            <th scope="col">Typ</th>
            <th scope="col">Status</th>
            <th scope="col">Akcje</th>
          </tr>
        </thead>
        <tbody>
          {categories.map((category) => (
            <tr key={category.id}>
              <td className="buildings-table__code">{category.code}</td>
              <td>{category.name}</td>
              <td>
                <Badge variant={transactionTypeVariant(category.type)}>
                  {getTransactionTypeLabel(category.type)}
                </Badge>
              </td>
              <td>
                <Badge variant={category.active ? 'success' : 'neutral'}>
                  {category.active ? 'Aktywna' : 'Nieaktywna'}
                </Badge>
              </td>
              <td>
                <div className="buildings-table__actions">
                  {canEdit ? (
                    <Button variant="secondary" size="sm" onClick={() => onEdit(category)}>
                      Edytuj
                    </Button>
                  ) : null}
                  {canDelete && category.active ? (
                    <Button variant="danger" size="sm" onClick={() => onDeactivate(category)}>
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
