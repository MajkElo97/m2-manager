import { useCallback, useEffect, useState } from 'react';
import { getUsers } from '@/features/users/api/usersApi';
import { getUserErrorMessage } from '@/features/users/usersMessages';
import type { User, UserListParams } from '@/features/users/types/user';
import { ApiError } from '@/services/apiError';

interface UseUsersResult {
  users: User[];
  isLoading: boolean;
  error: string | null;
  forbidden: boolean;
  unauthorized: boolean;
  refetch: () => Promise<void>;
}

export function useUsers(params: UserListParams): UseUsersResult {
  const [users, setUsers] = useState<User[]>([]);
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
      const data = await getUsers(params);
      setUsers(data);
    } catch (err) {
      if (err instanceof ApiError && err.status === 401) {
        setUnauthorized(true);
        setError('Sesja wygasła. Zaloguj się ponownie.');
      } else if (err instanceof ApiError && err.status === 403) {
        setForbidden(true);
        setError(getUserErrorMessage(err));
      } else {
        setError(getUserErrorMessage(err));
      }
      setUsers([]);
    } finally {
      setIsLoading(false);
    }
  }, [params.search, params.active, params.roleId]);

  useEffect(() => {
    void refetch();
  }, [refetch]);

  return {
    users,
    isLoading,
    error,
    forbidden,
    unauthorized,
    refetch,
  };
}
