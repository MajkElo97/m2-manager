import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import {
  displayValue,
  formatLinkedEmployee,
  formatUserRoles,
  getFullName,
} from '@/features/users/utils/userLabels';
import type { User } from '@/features/users/types/user';
import { getActiveLabel } from '@/features/employees/utils/employeeLabels';
import './UsersTable.css';

interface UsersTableProps {
  users: User[];
  canEdit: boolean;
  canDelete: boolean;
  onEdit: (user: User) => void;
  onDeactivate: (user: User) => void;
}

function activeVariant(active: boolean): 'success' | 'neutral' {
  return active ? 'success' : 'neutral';
}

export function UsersTable({
  users,
  canEdit,
  canDelete,
  onEdit,
  onDeactivate,
}: UsersTableProps) {
  return (
    <div className="buildings-table-wrapper">
      <table className="buildings-table">
        <thead>
          <tr>
            <th scope="col">Imię i nazwisko</th>
            <th scope="col">E-mail</th>
            <th scope="col">Rola</th>
            <th scope="col">Powiązany pracownik</th>
            <th scope="col">Status</th>
            <th scope="col">Akcje</th>
          </tr>
        </thead>
        <tbody>
          {users.map((user) => (
            <tr key={user.id}>
              <td>{getFullName(user.firstName, user.lastName)}</td>
              <td>{displayValue(user.email)}</td>
              <td>{formatUserRoles(user)}</td>
              <td>{formatLinkedEmployee(user)}</td>
              <td>
                <Badge variant={activeVariant(user.active)}>
                  {getActiveLabel(user.active)}
                </Badge>
              </td>
              <td>
                <div className="buildings-table__actions">
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
