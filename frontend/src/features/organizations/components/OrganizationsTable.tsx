import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import type { OrganizationListItem } from '@/features/organizations/types/organization';

interface OrganizationsTableProps {
  organizations: OrganizationListItem[];
  onEdit: (organization: OrganizationListItem) => void;
  onResetPassword: (organization: OrganizationListItem) => void;
  onDeactivate: (organization: OrganizationListItem) => void;
}

function formatDate(value: string): string {
  return new Intl.DateTimeFormat('pl-PL', {
    dateStyle: 'medium',
  }).format(new Date(value));
}

export function OrganizationsTable({
  organizations,
  onEdit,
  onResetPassword,
  onDeactivate,
}: OrganizationsTableProps) {
  return (
    <div className="organizations-table-wrapper">
      <table className="organizations-table">
        <thead>
          <tr>
            <th scope="col">Nazwa organizacji</th>
            <th scope="col">Administrator</th>
            <th scope="col">Status</th>
            <th scope="col">Utworzono</th>
            <th scope="col">Akcje</th>
          </tr>
        </thead>
        <tbody>
          {organizations.map((organization) => (
            <tr key={organization.id}>
              <td className="organizations-table__name-cell">
                <span className="organizations-table__name">{organization.name}</span>
                <span className="organizations-table__slug">{organization.slug}</span>
              </td>
              <td className="organizations-table__admin-cell">
                <span className="organizations-table__admin-name">{organization.adminName}</span>
                <span className="organizations-table__admin-email">{organization.adminEmail}</span>
              </td>
              <td>
                <Badge variant={organization.active ? 'success' : 'neutral'}>
                  {organization.active ? 'Aktywna' : 'Nieaktywna'}
                </Badge>
              </td>
              <td className="organizations-table__date">{formatDate(organization.createdAt)}</td>
              <td>
                <div className="organizations-table__actions">
                  <Button type="button" variant="secondary" size="sm" onClick={() => onEdit(organization)}>
                    Edytuj
                  </Button>
                  <Button type="button" variant="secondary" size="sm" onClick={() => onResetPassword(organization)}>
                    Resetuj hasło
                  </Button>
                  {organization.active ? (
                    <Button type="button" variant="danger" size="sm" onClick={() => onDeactivate(organization)}>
                      Dezaktywuj
                    </Button>
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
