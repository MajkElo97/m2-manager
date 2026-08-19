import type { Activity } from '@/features/activities/types/activity';
import type { Building } from '@/features/buildings/types/building';
import type { Scope, ScopePlanningType, ScopeStatus } from '@/features/scopes/types/scope';

export interface ScopeFilterParams {
  search: string;
  buildingId: string;
  activityId: string;
  planningType: ScopePlanningType | 'ALL';
  status: ScopeStatus | 'ALL';
}

export function filterScopes(
  scopes: Scope[],
  buildingsById: Map<string, Building>,
  activitiesById: Map<string, Activity>,
  params: ScopeFilterParams,
): Scope[] {
  const search = params.search.trim().toLowerCase();

  return scopes.filter((scope) => {
    if (params.buildingId !== 'ALL' && scope.buildingId !== params.buildingId) {
      return false;
    }

    if (params.activityId !== 'ALL' && scope.activityId !== params.activityId) {
      return false;
    }

    if (params.planningType !== 'ALL' && scope.planningType !== params.planningType) {
      return false;
    }

    if (params.status !== 'ALL' && scope.status !== params.status) {
      return false;
    }

    if (!search) {
      return true;
    }

    const building = buildingsById.get(scope.buildingId);
    const activity = activitiesById.get(scope.activityId);
    const haystack = [
      scope.code,
      scope.weekdays,
      scope.notes,
      building?.code,
      building?.name,
      activity?.code,
      activity?.name,
    ]
      .filter(Boolean)
      .join(' ')
      .toLowerCase();

    return haystack.includes(search);
  });
}
