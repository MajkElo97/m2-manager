export type ScopePlanningType = 'WEEKLY' | 'MONTHLY' | 'YEARLY' | 'EVENT';

export type ScopeStatus = 'ACTIVE' | 'INACTIVE';

export interface Scope {
  id: string;
  code: string;
  buildingId: string;
  activityId: string;
  planningType: ScopePlanningType;
  frequency: number | null;
  weekdays: string | null;
  notes: string | null;
  status: ScopeStatus;
  createdAt: string;
  updatedAt: string;
}

export interface CreateScopePayload {
  code: string;
  buildingId: string;
  activityId: string;
  planningType: ScopePlanningType;
  frequency?: number | null;
  weekdays?: string;
  notes?: string;
}

export interface UpdateScopePayload extends CreateScopePayload {
  status: ScopeStatus;
}

export interface ScopeListParams {
  buildingId?: string;
  activityId?: string;
  planningType?: ScopePlanningType | null;
  status?: ScopeStatus | null;
  search?: string;
}
