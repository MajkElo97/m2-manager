import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import type { FinancialCategory } from '@/features/finance/types/category';
import {
  getTransactionTypeLabel,
  transactionTypeVariant,
} from '@/features/finance/utils/financeLabels';
import './CategoriesMobileList.css';

interface CategoriesMobileListProps {
  categories: FinancialCategory[];
  canEdit: boolean;
  canDelete: boolean;
  onEdit: (category: FinancialCategory) => void;
  onDeactivate: (category: FinancialCategory) => void;
}

export function CategoriesMobileList({
  categories,
  canEdit,
  canDelete,
  onEdit,
  onDeactivate,
}: CategoriesMobileListProps) {
  return (
    <div className="buildings-mobile-list">
      {categories.map((category) => (
        <article key={category.id} className="buildings-mobile-card">
          <div className="buildings-mobile-card__header">
            <h3 className="buildings-mobile-card__name">
              {category.name} — {category.code}
            </h3>
            <Badge variant={transactionTypeVariant(category.type)}>
              {getTransactionTypeLabel(category.type)}
            </Badge>
          </div>

          <dl className="buildings-mobile-card__details">
            <div>
              <dt>Status</dt>
              <dd>
                <Badge variant={category.active ? 'success' : 'neutral'}>
                  {category.active ? 'Aktywna' : 'Nieaktywna'}
                </Badge>
              </dd>
            </div>
          </dl>

          <div className="buildings-mobile-card__actions">
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
          </div>
        </article>
      ))}
    </div>
  );
}
