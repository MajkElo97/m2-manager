import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import {
  displayValue,
  getActiveLabel,
  getFullName,
} from '@/features/employees/utils/employeeLabels';
import type { Supervisor } from '@/features/supervisors/types/supervisor';
import './SupervisorsTable.css';

interface SupervisorsTableProps {
  supervisors: Supervisor[];
  canEdit: boolean;
  canDelete: boolean;
  onEdit: (supervisor: Supervisor) => void;
  onDeactivate: (supervisor: Supervisor) => void;
}

function activeVariant(active: boolean): 'success' | 'neutral' {
  return active ? 'success' : 'neutral';
}

export function SupervisorsTable({
  supervisors,
  canEdit,
  canDelete,
  onEdit,
  onDeactivate,
}: SupervisorsTableProps) {
  return (
    <div className="buildings-table-wrapper">
      <table className="buildings-table">
        <thead>
          <tr>
            <th scope="col">Imię i nazwisko</th>
            <th scope="col">Zarządca</th>
            <th scope="col">Telefon</th>
            <th scope="col">E-mail</th>
            <th scope="col">Uwagi</th>
            <th scope="col">Aktywny</th>
            <th scope="col">Akcje</th>
          </tr>
        </thead>
        <tbody>
          {supervisors.map((supervisor) => (
            <tr key={supervisor.id}>
              <td>{getFullName(supervisor.firstName, supervisor.lastName)}</td>
              <td>{supervisor.managerName}</td>
              <td>{displayValue(supervisor.phone)}</td>
              <td>{displayValue(supervisor.email)}</td>
              <td>{displayValue(supervisor.notes)}</td>
              <td>
                <Badge variant={activeVariant(supervisor.active)}>
                  {getActiveLabel(supervisor.active)}
                </Badge>
              </td>
              <td>
                <div className="buildings-table__actions">
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
