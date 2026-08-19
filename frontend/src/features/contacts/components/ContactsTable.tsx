import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import type { Contact } from '@/features/contacts/types/contact';
import {
  displayValue,
  getActiveLabel,
  getFullName,
} from '@/features/employees/utils/employeeLabels';
import './ContactsTable.css';

interface ContactsTableProps {
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

export function ContactsTable({
  contacts,
  canEdit,
  canDelete,
  showBuilding = false,
  onEdit,
  onDeactivate,
  onBuildingClick,
}: ContactsTableProps) {
  return (
    <div className="buildings-table-wrapper">
      <table className="buildings-table">
        <thead>
          <tr>
            {showBuilding ? <th scope="col">Budynek</th> : null}
            <th scope="col">Imię i nazwisko</th>
            <th scope="col">Funkcja</th>
            <th scope="col">Telefon</th>
            <th scope="col">E-mail</th>
            <th scope="col">Uwagi</th>
            <th scope="col">Aktywny</th>
            <th scope="col">Akcje</th>
          </tr>
        </thead>
        <tbody>
          {contacts.map((contact) => (
            <tr key={contact.id}>
              {showBuilding ? (
                <td>
                  {onBuildingClick ? (
                    <button
                      type="button"
                      className="buildings-table__building-link"
                      onClick={() => onBuildingClick(contact.buildingId)}
                    >
                      {formatBuildingLabel(contact)}
                    </button>
                  ) : (
                    formatBuildingLabel(contact)
                  )}
                </td>
              ) : null}
              <td>{getFullName(contact.firstName, contact.lastName)}</td>
              <td>{displayValue(contact.functionTitle)}</td>
              <td>{displayValue(contact.phone)}</td>
              <td>{displayValue(contact.email)}</td>
              <td>{displayValue(contact.notes)}</td>
              <td>
                <Badge variant={activeVariant(contact.active)}>
                  {getActiveLabel(contact.active)}
                </Badge>
              </td>
              <td>
                <div className="buildings-table__actions">
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
