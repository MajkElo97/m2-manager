import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { getActiveLabel } from '@/features/employees/utils/employeeLabels';
import type { User } from '@/features/users/types/user';
import {
  displayValue,
  formatLinkedEmployee,
  formatUserRoles,
  getFullName,
} from '@/features/users/utils/userLabels';
import './UsersMobileList.css';

interface UsersMobileListProps {
  users: User[];
  canEdit: boolean;
  canDelete: boolean;
  onEdit: (user: User) => void;
  onDeactivate: (user: User) => void;
}

function activeVariant(active: boolean): 'success' | 'neutral' {
  return active ? 'success' : 'neutral';
}

export function UsersMobileList({
  users,
  canEdit,
  canDelete,
  onEdit,
  onDeactivate,
}: UsersMobileListProps) {
  return (
    <div className="buildings-mobile-list">
      {users.map((user) => (
        <article key={user.id} className="buildings-mobile-card">
          <div className="buildings-mobile-card__header">
            <h3 className="buildings-mobile-card__name">
              {getFullName(user.firstName, user.lastName)}
            </h3>
            <Badge variant={activeVariant(user.active)}>
              {getActiveLabel(user.active)}
            </Badge>
          </div>

          <dl className="buildings-mobile-card__details">
            <div>
              <dt>E-mail</dt>
              <dd>{displayValue(user.email)}</dd>
            </div>
            <div>
              <dt>Rola</dt>
              <dd>{formatUserRoles(user)}</dd>
            </div>
            <div>
              <dt>Powiązany pracownik</dt>
              <dd>{formatLinkedEmployee(user)}</dd>
            </div>
          </dl>

          <div className="buildings-mobile-card__actions">
            {canEdit ? (
              <Button variant="secondary" size="sm" onClick={() => onEdit(user)}>
                Edytuj
              </Button>
            ) : null}
            {canDelete && user.active ? (
              <Button variant="danger" size="sm" onClick={() => onDeactivate(user)}>
                Dezaktywuj
              </Button>
            ) : null}
          </div>
        </article>
      ))}
    </div>
  );
}
