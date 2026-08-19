import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import type { Contact } from '@/features/contacts/types/contact';
import {
  displayValue,
  getActiveLabel,
  getFullName,
} from '@/features/employees/utils/employeeLabels';
import './ContactsMobileList.css';

interface ContactsMobileListProps {
  contacts: Contact[];
  canEdit: boolean;
  canDelete: boolean;
  showBuilding?: boolean;
  onEdit?: (contact: Contact) => void;
  onDeactivate?: (contact: Contact) => void;
  onBuildingClick?: (buildingId: string) => void;
}

function activeVariant(active: boolean): 'success' | 'neutral' {
  return active ? 'success' : 'neutral';
}

function formatBuildingLabel(contact: Contact): string {
  return `${contact.buildingName} · ${contact.buildingCode}`;
}

export function ContactsMobileList({
  contacts,
  canEdit,
  canDelete,
  showBuilding = false,
  onEdit,
  onDeactivate,
  onBuildingClick,
}: ContactsMobileListProps) {
  return (
    <div className="buildings-mobile-list">
      {contacts.map((contact) => (
        <article key={contact.id} className="buildings-mobile-card">
          <div className="buildings-mobile-card__header">
            <div>
              <h3 className="buildings-mobile-card__name">
                {getFullName(contact.firstName, contact.lastName)}
              </h3>
              {showBuilding ? (
                onBuildingClick ? (
                  <button
                    type="button"
                    className="buildings-mobile-card__building"
                    onClick={() => onBuildingClick(contact.buildingId)}
                  >
                    {formatBuildingLabel(contact)}
                  </button>
                ) : (
                  <p className="buildings-mobile-card__building">{formatBuildingLabel(contact)}</p>
                )
              ) : null}
            </div>
            <Badge variant={activeVariant(contact.active)}>
              {getActiveLabel(contact.active)}
            </Badge>
          </div>

          <dl className="buildings-mobile-card__details">
            <div>
              <dt>Funkcja</dt>
              <dd>{displayValue(contact.functionTitle)}</dd>
            </div>
            <div>
              <dt>Telefon</dt>
              <dd>{displayValue(contact.phone)}</dd>
            </div>
            <div>
              <dt>E-mail</dt>
              <dd>{displayValue(contact.email)}</dd>
            </div>
            <div>
              <dt>Uwagi</dt>
              <dd>{displayValue(contact.notes)}</dd>
            </div>
          </dl>

          <div className="buildings-mobile-card__actions">
            {canEdit && onEdit ? (
              <Button variant="secondary" size="sm" onClick={() => onEdit(contact)}>
                Edytuj
              </Button>
            ) : null}
            {canDelete && contact.active && onDeactivate ? (
              <Button variant="danger" size="sm" onClick={() => onDeactivate(contact)}>
                Dezaktywuj
              </Button>
            ) : null}
          </div>
        </article>
      ))}
    </div>
  );
}
