import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import type { Manager } from '@/features/managers/types/manager';
import { displayValue, getActiveLabel } from '@/features/employees/utils/employeeLabels';
import './ManagersTable.css';

interface ManagersTableProps {
  managers: Manager[];
  canEdit: boolean;
  canDelete: boolean;
  onEdit: (manager: Manager) => void;
  onDeactivate: (manager: Manager) => void;
}

function activeVariant(active: boolean): 'success' | 'neutral' {
  return active ? 'success' : 'neutral';
}

export function ManagersTable({
  managers,
  canEdit,
  canDelete,
  onEdit,
  onDeactivate,
}: ManagersTableProps) {
  return (
    <div className="buildings-table-wrapper">
      <table className="buildings-table">
        <thead>
          <tr>
            <th scope="col">Nazwa firmy</th>
            <th scope="col">Telefon</th>
            <th scope="col">E-mail</th>
            <th scope="col">Adres</th>
            <th scope="col">Opiekunowie</th>
            <th scope="col">Aktywny</th>
            <th scope="col">Akcje</th>
          </tr>
        </thead>
        <tbody>
          {managers.map((manager) => (
            <tr key={manager.id}>
              <td>{manager.name}</td>
              <td>{displayValue(manager.phone)}</td>
              <td>{displayValue(manager.email)}</td>
              <td>{displayValue(manager.address)}</td>
              <td>{manager.supervisorCount}</td>
              <td>
                <Badge variant={activeVariant(manager.active)}>
                  {getActiveLabel(manager.active)}
                </Badge>
              </td>
              <td>
                <div className="buildings-table__actions">
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
