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
    timeStyle: 'short',
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
            <th>Nazwa organizacji</th>
            <th>Slug</th>
            <th>Administrator</th>
            <th>E-mail administratora</th>
            <th>Status</th>
            <th>Data utworzenia</th>
            <th>Akcje</th>
          </tr>
        </thead>
        <tbody>
          {organizations.map((organization) => (
            <tr key={organization.id}>
              <td>{organization.name}</td>
              <td>{organization.slug}</td>
              <td>{organization.adminName}</td>
              <td>{organization.adminEmail}</td>
              <td>{organization.active ? 'Aktywna' : 'Nieaktywna'}</td>
              <td>{formatDate(organization.createdAt)}</td>
              <td>
                <div className="organizations-table__actions">
                  <Button type="button" variant="secondary" onClick={() => onEdit(organization)}>
                    Edytuj
                  </Button>
                  <Button type="button" variant="secondary" onClick={() => onResetPassword(organization)}>
                    Resetuj hasło
                  </Button>
                  {organization.active ? (
                    <Button type="button" variant="danger" onClick={() => onDeactivate(organization)}>
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
