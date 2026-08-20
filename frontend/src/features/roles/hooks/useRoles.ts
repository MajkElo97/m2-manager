import { useCallback, useEffect, useState } from 'react';
import { getRoles } from '@/features/roles/api/rolesApi';
import { getRoleErrorMessage } from '@/features/roles/rolesMessages';
import type { Role } from '@/features/roles/types/role';
import { useOrganizationContextKey } from '@/features/auth/useOrganizationContextKey';
import { ApiError } from '@/services/apiError';

interface UseRolesResult {
  roles: Role[];
  isLoading: boolean;
  error: string | null;
  forbidden: boolean;
  unauthorized: boolean;
  refetch: () => Promise<void>;
}

export function useRoles(): UseRolesResult {
  const organizationContextKey = useOrganizationContextKey();
  const [roles, setRoles] = useState<Role[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [forbidden, setForbidden] = useState(false);
  const [unauthorized, setUnauthorized] = useState(false);

  const refetch = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    setForbidden(false);
    setUnauthorized(false);

    try {
      const data = await getRoles();
      setRoles(data);
    } catch (err) {
      if (err instanceof ApiError && err.status === 401) {
        setUnauthorized(true);
        setError('Sesja wygasła. Zaloguj się ponownie.');
      } else if (err instanceof ApiError && err.status === 403) {
        setForbidden(true);
        setError(getRoleErrorMessage(err));
      } else {
        setError(getRoleErrorMessage(err));
      }
      setRoles([]);
    } finally {
      setIsLoading(false);
    }
  }, [organizationContextKey]);

  useEffect(() => {
    void refetch();
  }, [refetch]);

  return {
    roles,
    isLoading,
    error,
    forbidden,
    unauthorized,
    refetch,
  };
}
