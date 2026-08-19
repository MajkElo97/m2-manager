import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import type { Role } from '@/features/roles/types/role';
import { getRoleTypeLabel } from '@/features/users/utils/userLabels';
import './RolesMobileList.css';

interface RolesMobileListProps {
  roles: Role[];
  canEdit: boolean;
  canDelete: boolean;
  onEdit: (role: Role) => void;
  onDeactivate: (role: Role) => void;
}

function roleTypeVariant(systemRole: boolean): 'neutral' | 'warning' {
  return systemRole ? 'warning' : 'neutral';
}

export function RolesMobileList({
  roles,
  canEdit,
  canDelete,
  onEdit,
  onDeactivate,
}: RolesMobileListProps) {
  return (
    <div className="buildings-mobile-list">
      {roles.map((role) => (
        <article key={role.id} className="buildings-mobile-card">
          <div className="buildings-mobile-card__header">
            <h3 className="buildings-mobile-card__name">{role.name}</h3>
            <Badge variant={roleTypeVariant(role.systemRole)}>
              {getRoleTypeLabel(role.systemRole)}
            </Badge>
          </div>

          <dl className="buildings-mobile-card__details">
            <div>
              <dt>Użytkownicy</dt>
              <dd>{role.userCount}</dd>
            </div>
            <div>
              <dt>Uprawnienia</dt>
              <dd>{role.permissionCount}</dd>
            </div>
          </dl>

          <div className="buildings-mobile-card__actions">
            {canEdit ? (
              <Button variant="secondary" size="sm" onClick={() => onEdit(role)}>
                Edytuj
              </Button>
            ) : null}
            {canDelete && role.active && !role.systemRole ? (
              <Button variant="danger" size="sm" onClick={() => onDeactivate(role)}>
                Dezaktywuj
              </Button>
            ) : null}
          </div>
        </article>
      ))}
    </div>
  );
}
