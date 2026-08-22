import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import type { OrganizationListItem } from '@/features/organizations/types/organization';

interface OrganizationsMobileListProps {
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
          <div className="organizations-mobile-card__header">
            <div>
              <h3 className="organizations-mobile-card__title">{organization.name}</h3>
              <p className="organizations-mobile-card__slug">{organization.slug}</p>
            </div>
            <Badge variant={organization.active ? 'success' : 'neutral'}>
              {organization.active ? 'Aktywna' : 'Nieaktywna'}
            </Badge>
          </div>

          <dl className="organizations-mobile-card__details">
            <div>
              <dt>Administrator</dt>
              <dd>{organization.adminName}</dd>
            </div>
            <div>
              <dt>E-mail administratora</dt>
              <dd>{organization.adminEmail}</dd>
            </div>
            <div>
              <dt>Utworzono</dt>
              <dd>{formatDate(organization.createdAt)}</dd>
            </div>
          </dl>

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
        </article>
      ))}
    </div>
  );
}
