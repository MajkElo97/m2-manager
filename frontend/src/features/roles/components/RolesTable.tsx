import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { getRoleTypeLabel } from '@/features/users/utils/userLabels';
import type { Role } from '@/features/roles/types/role';
import './RolesTable.css';

interface RolesTableProps {
  roles: Role[];
  canEdit: boolean;
  canDelete: boolean;
  onEdit: (role: Role) => void;
  onDeactivate: (role: Role) => void;
}

function roleTypeVariant(systemRole: boolean): 'neutral' | 'warning' {
  return systemRole ? 'warning' : 'neutral';
}

export function RolesTable({
  roles,
  canEdit,
  canDelete,
  onEdit,
  onDeactivate,
}: RolesTableProps) {
  return (
    <div className="buildings-table-wrapper">
      <table className="buildings-table">
        <thead>
          <tr>
            <th scope="col">Nazwa</th>
            <th scope="col">Typ</th>
            <th scope="col">Liczba użytkowników</th>
            <th scope="col">Liczba uprawnień</th>
            <th scope="col">Akcje</th>
          </tr>
        </thead>
        <tbody>
          {roles.map((role) => (
            <tr key={role.id}>
              <td>{role.name}</td>
              <td>
                <Badge variant={roleTypeVariant(role.systemRole)}>
                  {getRoleTypeLabel(role.systemRole)}
                </Badge>
              </td>
              <td>{role.userCount}</td>
              <td>{role.permissionCount}</td>
              <td>
                <div className="buildings-table__actions">
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
                  {!canEdit && !(canDelete && role.active && !role.systemRole) ? (
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
