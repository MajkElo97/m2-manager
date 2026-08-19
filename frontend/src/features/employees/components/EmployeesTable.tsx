import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import type { Employee } from '@/features/employees/types/employee';
import {
  displayValue,
  formatRemuneration,
  getActiveLabel,
  getEmployeeRoleLabel,
  getEmploymentTypeLabel,
  getFullName,
} from '@/features/employees/utils/employeeLabels';
import { formatDateDisplay } from '@/utils/dateFormat';
import './EmployeesTable.css';

interface EmployeesTableProps {
  employees: Employee[];
  canEdit: boolean;
  canDelete: boolean;
  onEdit: (employee: Employee) => void;
  onDeactivate: (employee: Employee) => void;
}

function activeVariant(active: boolean): 'success' | 'neutral' {
  return active ? 'success' : 'neutral';
}

export function EmployeesTable({
  employees,
  canEdit,
  canDelete,
  onEdit,
  onDeactivate,
}: EmployeesTableProps) {
  return (
    <div className="buildings-table-wrapper">
      <table className="buildings-table">
        <thead>
          <tr>
            <th scope="col">Imię i nazwisko</th>
            <th scope="col">Telefon</th>
            <th scope="col">E-mail</th>
            <th scope="col">Stanowisko</th>
            <th scope="col">Rola</th>
            <th scope="col">Forma zatrudnienia</th>
            <th scope="col">Data zatrudnienia</th>
            <th scope="col">Wynagrodzenie</th>
            <th scope="col">Aktywny</th>
            <th scope="col">Kolor</th>
            <th scope="col">Akcje</th>
          </tr>
        </thead>
        <tbody>
          {employees.map((employee) => (
            <tr key={employee.id}>
              <td>{getFullName(employee.firstName, employee.lastName)}</td>
              <td>{displayValue(employee.phone)}</td>
              <td>{displayValue(employee.email)}</td>
              <td>{displayValue(employee.position)}</td>
              <td>{getEmployeeRoleLabel(employee.role)}</td>
              <td>{getEmploymentTypeLabel(employee.employmentType)}</td>
              <td>{formatDateDisplay(employee.employmentStartDate)}</td>
              <td>
                {formatRemuneration(
                  employee.remunerationAmount,
                  employee.remunerationUnit,
                  employee.remunerationNet,
                )}
              </td>
              <td>
                <Badge variant={activeVariant(employee.active)}>
                  {getActiveLabel(employee.active)}
                </Badge>
              </td>
              <td>
                {employee.calendarColor ? (
                  <span className="buildings-table__color">
                    <span
                      className="buildings-table__color-swatch"
                      style={{ backgroundColor: employee.calendarColor }}
                      aria-hidden="true"
                    />
                    {employee.calendarColor}
                  </span>
                ) : (
                  '—'
                )}
              </td>
              <td>
                <div className="buildings-table__actions">
                  {canEdit ? (
                    <Button variant="secondary" size="sm" onClick={() => onEdit(employee)}>
                      Edytuj
                    </Button>
                  ) : null}
                  {canDelete && employee.active ? (
                    <Button variant="danger" size="sm" onClick={() => onDeactivate(employee)}>
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
