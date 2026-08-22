import { useCallback, useEffect, useState } from 'react';
import { getOrganizations } from '@/features/organizations/api/organizationsApi';
import type { OrganizationListItem, OrganizationListParams } from '@/features/organizations/types/organization';
import { ApiError } from '@/services/apiError';

export function useOrganizations(params: OrganizationListParams) {
  const [organizations, setOrganizations] = useState<OrganizationListItem[]>([]);
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
      const data = await getOrganizations(params);
      setOrganizations(data);
    } catch (err) {
      if (err instanceof ApiError) {
        if (err.status === 403) {
          setForbidden(true);
        } else if (err.status === 401) {
          setUnauthorized(true);
        } else {
          setError(err.message);
        }
      } else {
        setError('Nie udało się pobrać organizacji.');
      }
    } finally {
      setIsLoading(false);
    }
  }, [params]);

  useEffect(() => {
    void refetch();
  }, [refetch]);

  return { organizations, isLoading, error, forbidden, unauthorized, refetch };
}
