export type ActivityPlanningType = 'CYCLIC' | 'PERIODIC' | 'ON_DEMAND';

export type ActivityPriority = 'LOW' | 'NORMAL' | 'HIGH';

export interface Activity {
  id: string;
  code: string;
  name: string;
  category: string;
  planningType: ActivityPlanningType;
  defaultPeriod: string | null;
  durationMinutes: number | null;
  priority: ActivityPriority;
  active: boolean;
  system: boolean;
  manageable: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateActivityPayload {
  code?: string;
  name: string;
  category: string;
  planningType: ActivityPlanningType;
  defaultPeriod?: string;
  durationMinutes?: number | null;
  priority: ActivityPriority;
  system?: boolean;
}

export interface UpdateActivityPayload {
  code: string;
  name: string;
  category: string;
  planningType: ActivityPlanningType;
  defaultPeriod?: string;
  durationMinutes?: number | null;
  priority: ActivityPriority;
  active: boolean;
}

export interface ActivityListParams {
  search?: string;
  category?: string;
  planningType?: ActivityPlanningType | null;
  active?: boolean | null;
}
