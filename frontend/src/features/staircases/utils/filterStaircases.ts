import type { Building } from '@/features/buildings/types/building';
import type { Staircase } from '@/features/staircases/types/staircase';

export type BooleanFilterValue = 'ALL' | 'YES' | 'NO';

export interface StaircaseFilterParams {
  search: string;
  buildingId: string;
  elevator: BooleanFilterValue;
  keyRequired: BooleanFilterValue;
}

function matchesBooleanFilter(value: boolean, filter: BooleanFilterValue): boolean {
  if (filter === 'ALL') {
    return true;
  }
  return filter === 'YES' ? value : !value;
}

export function filterStaircases(
  staircases: Staircase[],
  buildingsById: Map<string, Building>,
  params: StaircaseFilterParams,
): Staircase[] {
  const search = params.search.trim().toLowerCase();

  return staircases.filter((staircase) => {
    if (params.buildingId !== 'ALL' && staircase.buildingId !== params.buildingId) {
      return false;
    }

    if (!matchesBooleanFilter(staircase.elevator, params.elevator)) {
      return false;
    }

    if (!matchesBooleanFilter(staircase.keyRequired, params.keyRequired)) {
      return false;
    }

    if (!search) {
      return true;
    }

    const building = buildingsById.get(staircase.buildingId);
    const haystack = [
      staircase.code,
      staircase.designation,
      building?.code,
      building?.name,
      building?.address,
      building?.city,
    ]
      .filter(Boolean)
      .join(' ')
      .toLowerCase();

    return haystack.includes(search);
  });
}
