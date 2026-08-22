import { useCallback, useEffect, useState } from 'react';
import { getContacts } from '@/features/contacts/api/contactsApi';
import { getContactErrorMessage } from '@/features/contacts/contactsMessages';
import type { Contact, ContactListParams } from '@/features/contacts/types/contact';
import { useOrganizationContextKey } from '@/features/auth/useOrganizationContextKey';
import { isTenantScopeActive } from '@/features/auth/tenantScope';
import { ApiError } from '@/services/apiError';

interface UseContactsResult {
  contacts: Contact[];
  isLoading: boolean;
  error: string | null;
  forbidden: boolean;
  unauthorized: boolean;
  refetch: () => Promise<void>;
}

export function useContacts(params: ContactListParams = {}): UseContactsResult {
  const organizationContextKey = useOrganizationContextKey();
  const [contacts, setContacts] = useState<Contact[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [forbidden, setForbidden] = useState(false);
  const [unauthorized, setUnauthorized] = useState(false);

  const refetch = useCallback(async () => {
    if (!isTenantScopeActive(organizationContextKey)) {
      setContacts([]);
      setIsLoading(false);
      setError(null);
      setForbidden(false);
      setUnauthorized(false);
      return;
    }

    setIsLoading(true);
    setError(null);
    setForbidden(false);
    setUnauthorized(false);

    try {
      const data = await getContacts(params);
      setContacts(data);
    } catch (err) {
      if (err instanceof ApiError && err.status === 401) {
        setUnauthorized(true);
        setError('Sesja wygasła. Zaloguj się ponownie.');
      } else if (err instanceof ApiError && err.status === 403) {
        setForbidden(true);
        setError(getContactErrorMessage(err));
      } else {
        setError(getContactErrorMessage(err));
      }
      setContacts([]);
    } finally {
      setIsLoading(false);
    }
  }, [params.buildingId, organizationContextKey]);

  useEffect(() => {
    void refetch();
  }, [refetch]);

  return {
    contacts,
    isLoading,
    error,
    forbidden,
    unauthorized,
    refetch,
  };
}
