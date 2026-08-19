import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import {
  displayValue,
  getActiveLabel,
  getFullName,
} from '@/features/employees/utils/employeeLabels';
import type { Supervisor } from '@/features/supervisors/types/supervisor';
import './SupervisorsMobileList.css';

interface SupervisorsMobileListProps {
  supervisors: Supervisor[];
  canEdit: boolean;
  canDelete: boolean;
  onEdit: (supervisor: Supervisor) => void;
  onDeactivate: (supervisor: Supervisor) => void;
}

function activeVariant(active: boolean): 'success' | 'neutral' {
  return active ? 'success' : 'neutral';
}

export function SupervisorsMobileList({
  supervisors,
  canEdit,
  canDelete,
  onEdit,
  onDeactivate,
}: SupervisorsMobileListProps) {
  return (
    <div className="buildings-mobile-list">
      {supervisors.map((supervisor) => (
        <article key={supervisor.id} className="buildings-mobile-card">
          <div className="buildings-mobile-card__header">
            <h3 className="buildings-mobile-card__name">
              {getFullName(supervisor.firstName, supervisor.lastName)}
            </h3>
            <Badge variant={activeVariant(supervisor.active)}>
              {getActiveLabel(supervisor.active)}
            </Badge>
          </div>

          <dl className="buildings-mobile-card__details">
            <div>
              <dt>Zarządca</dt>
              <dd>{supervisor.managerName}</dd>
            </div>
            <div>
              <dt>Telefon</dt>
              <dd>{displayValue(supervisor.phone)}</dd>
            </div>
            <div>
              <dt>E-mail</dt>
              <dd>{displayValue(supervisor.email)}</dd>
            </div>
            <div>
              <dt>Uwagi</dt>
              <dd>{displayValue(supervisor.notes)}</dd>
            </div>
          </dl>

          <div className="buildings-mobile-card__actions">
            {canEdit ? (
              <Button variant="secondary" size="sm" onClick={() => onEdit(supervisor)}>
                Edytuj
              </Button>
            ) : null}
            {canDelete && supervisor.active ? (
              <Button variant="danger" size="sm" onClick={() => onDeactivate(supervisor)}>
                Dezaktywuj
              </Button>
            ) : null}
          </div>
        </article>
      ))}
    </div>
  );
}
