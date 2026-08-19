import type { Contact } from '@/features/contacts/types/contact';
import { getFullName } from '@/features/employees/utils/employeeLabels';

export interface ContactFilterParams {
  search: string;
  buildingId: string;
  functionTitle: string;
  active: 'ALL' | 'ACTIVE' | 'INACTIVE';
}

export function filterContacts(contacts: Contact[], params: ContactFilterParams): Contact[] {
  const search = params.search.trim().toLowerCase();
  const functionFilter = params.functionTitle.trim().toLowerCase();

  return contacts.filter((contact) => {
    if (params.buildingId !== 'ALL' && contact.buildingId !== params.buildingId) {
      return false;
    }

    if (params.active === 'ACTIVE' && !contact.active) {
      return false;
    }

    if (params.active === 'INACTIVE' && contact.active) {
      return false;
    }

    if (functionFilter && !(contact.functionTitle ?? '').toLowerCase().includes(functionFilter)) {
      return false;
    }

    if (!search) {
      return true;
    }

    const haystack = [
      contact.buildingCode,
      contact.buildingName,
      contact.firstName,
      contact.lastName,
      contact.functionTitle,
      contact.phone,
      contact.email,
      contact.notes,
      getFullName(contact.firstName, contact.lastName),
    ]
      .filter(Boolean)
      .join(' ')
      .toLowerCase();

    return haystack.includes(search);
  });
}
