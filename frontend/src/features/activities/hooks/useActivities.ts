import { useCallback, useEffect, useState } from 'react';
import { getActivities } from '@/features/activities/api/activitiesApi';
import { getActivityErrorMessage } from '@/features/activities/activitiesMessages';
import type { Activity, ActivityListParams } from '@/features/activities/types/activity';
import { useOrganizationContextKey } from '@/features/auth/useOrganizationContextKey';
import { isTenantScopeActive } from '@/features/auth/tenantScope';
import { ApiError } from '@/services/apiError';

interface UseActivitiesResult {
  activities: Activity[];
  isLoading: boolean;
  error: string | null;
  forbidden: boolean;
  unauthorized: boolean;
  refetch: () => Promise<void>;
}

export function useActivities(params: ActivityListParams): UseActivitiesResult {
  const organizationContextKey = useOrganizationContextKey();
  const [activities, setActivities] = useState<Activity[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [forbidden, setForbidden] = useState(false);
  const [unauthorized, setUnauthorized] = useState(false);

  const refetch = useCallback(async () => {
    if (!isTenantScopeActive(organizationContextKey)) {
      setActivities([]);
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
      const data = await getActivities(params);
      setActivities(data);
    } catch (err) {
      if (err instanceof ApiError && err.status === 401) {
        setUnauthorized(true);
        setError('Sesja wygasła. Zaloguj się ponownie.');
      } else if (err instanceof ApiError && err.status === 403) {
        setForbidden(true);
        setError(getActivityErrorMessage(err));
      } else {
        setError(getActivityErrorMessage(err));
      }
      setActivities([]);
    } finally {
      setIsLoading(false);
    }
  }, [params.search, params.category, params.planningType, params.active, organizationContextKey]);

  useEffect(() => {
    void refetch();
  }, [refetch]);

  return {
    activities,
    isLoading,
    error,
    forbidden,
    unauthorized,
    refetch,
  };
}
