import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { displayValue, getActiveLabel } from '@/features/employees/utils/employeeLabels';
import type { Manager } from '@/features/managers/types/manager';
import './ManagersMobileList.css';

interface ManagersMobileListProps {
  managers: Manager[];
  canEdit: boolean;
  canDelete: boolean;
  onEdit: (manager: Manager) => void;
  onDeactivate: (manager: Manager) => void;
}

function activeVariant(active: boolean): 'success' | 'neutral' {
  return active ? 'success' : 'neutral';
}

export function ManagersMobileList({
  managers,
  canEdit,
  canDelete,
  onEdit,
  onDeactivate,
}: ManagersMobileListProps) {
  return (
    <div className="buildings-mobile-list">
      {managers.map((manager) => (
        <article key={manager.id} className="buildings-mobile-card">
          <div className="buildings-mobile-card__header">
            <h3 className="buildings-mobile-card__name">{manager.name}</h3>
            <Badge variant={activeVariant(manager.active)}>
              {getActiveLabel(manager.active)}
            </Badge>
          </div>

          <dl className="buildings-mobile-card__details">
            <div>
              <dt>Telefon</dt>
              <dd>{displayValue(manager.phone)}</dd>
            </div>
            <div>
              <dt>E-mail</dt>
              <dd>{displayValue(manager.email)}</dd>
            </div>
            <div>
              <dt>Adres</dt>
              <dd>{displayValue(manager.address)}</dd>
            </div>
            <div>
              <dt>Opiekunowie</dt>
              <dd>{manager.supervisorCount}</dd>
            </div>
          </dl>

          <div className="buildings-mobile-card__actions">
            {canEdit ? (
              <Button variant="secondary" size="sm" onClick={() => onEdit(manager)}>
                Edytuj
              </Button>
            ) : null}
            {canDelete && manager.active ? (
              <Button variant="danger" size="sm" onClick={() => onDeactivate(manager)}>
                Dezaktywuj
              </Button>
            ) : null}
          </div>
        </article>
      ))}
    </div>
  );
}
