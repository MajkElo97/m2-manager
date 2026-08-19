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
import './EmployeesMobileList.css';

interface EmployeesMobileListProps {
  employees: Employee[];
  canEdit: boolean;
  canDelete: boolean;
  onEdit: (employee: Employee) => void;
  onDeactivate: (employee: Employee) => void;
}

function activeVariant(active: boolean): 'success' | 'neutral' {
  return active ? 'success' : 'neutral';
}

export function EmployeesMobileList({
  employees,
  canEdit,
  canDelete,
  onEdit,
  onDeactivate,
}: EmployeesMobileListProps) {
  return (
    <div className="buildings-mobile-list">
      {employees.map((employee) => (
        <article key={employee.id} className="buildings-mobile-card">
          <div className="buildings-mobile-card__header">
            <h3 className="buildings-mobile-card__name">
              {getFullName(employee.firstName, employee.lastName)}
            </h3>
            <Badge variant={activeVariant(employee.active)}>
              {getActiveLabel(employee.active)}
            </Badge>
          </div>

          <dl className="buildings-mobile-card__details">
            <div>
              <dt>Telefon</dt>
              <dd>{displayValue(employee.phone)}</dd>
            </div>
            <div>
              <dt>E-mail</dt>
              <dd>{displayValue(employee.email)}</dd>
            </div>
            <div>
              <dt>Stanowisko</dt>
              <dd>{displayValue(employee.position)}</dd>
            </div>
            <div>
              <dt>Rola</dt>
              <dd>{getEmployeeRoleLabel(employee.role)}</dd>
            </div>
            <div>
              <dt>Forma zatrudnienia</dt>
              <dd>{getEmploymentTypeLabel(employee.employmentType)}</dd>
            </div>
            <div>
              <dt>Data zatrudnienia</dt>
              <dd>{formatDateDisplay(employee.employmentStartDate)}</dd>
            </div>
            <div>
              <dt>Wynagrodzenie</dt>
              <dd>
                {formatRemuneration(
                  employee.remunerationAmount,
                  employee.remunerationUnit,
                  employee.remunerationNet,
                )}
              </dd>
            </div>
            {employee.calendarColor ? (
              <div>
                <dt>Kolor</dt>
                <dd>
                  <span className="buildings-mobile-card__color">
                    <span
                      className="buildings-mobile-card__color-swatch"
                      style={{ backgroundColor: employee.calendarColor }}
                      aria-hidden="true"
                    />
                    {employee.calendarColor}
                  </span>
                </dd>
              </div>
            ) : null}
          </dl>

          <div className="buildings-mobile-card__actions">
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
          </div>
        </article>
      ))}
    </div>
  );
}
