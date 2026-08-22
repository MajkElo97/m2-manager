import { Button } from '@/components/ui/Button';
import type { OrganizationListItem } from '@/features/organizations/types/organization';

interface OrganizationsMobileListProps {
  organizations: OrganizationListItem[];
  onEdit: (organization: OrganizationListItem) => void;
  onResetPassword: (organization: OrganizationListItem) => void;
  onDeactivate: (organization: OrganizationListItem) => void;
}

export function OrganizationsMobileList({
  organizations,
  onEdit,
  onResetPassword,
  onDeactivate,
}: OrganizationsMobileListProps) {
  return (
    <div className="organizations-mobile-list">
      {organizations.map((organization) => (
        <article key={organization.id} className="organizations-mobile-card">
          <h3 className="organizations-mobile-card__title">{organization.name}</h3>
          <p className="organizations-mobile-card__meta">Slug: {organization.slug}</p>
          <p className="organizations-mobile-card__meta">Administrator: {organization.adminName}</p>
          <p className="organizations-mobile-card__meta">E-mail: {organization.adminEmail}</p>
          <p className="organizations-mobile-card__meta">
            Status: {organization.active ? 'Aktywna' : 'Nieaktywna'}
          </p>
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
        </article>
      ))}
    </div>
  );
}
