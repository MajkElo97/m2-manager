import type { ScopePlanningType, ScopeStatus } from '@/features/scopes/types/scope';

const planningTypeLabels: Record<ScopePlanningType, string> = {
  WEEKLY: 'Tygodniowy',
  MONTHLY: 'Miesięczny',
  YEARLY: 'Roczny',
  EVENT: 'Zdarzeniowy',
};

const statusLabels: Record<ScopeStatus, string> = {
  ACTIVE: 'Aktywny',
  INACTIVE: 'Nieaktywny',
};

export function getScopePlanningTypeLabel(planningType: ScopePlanningType): string {
  return planningTypeLabels[planningType];
}

export function getScopeStatusLabel(status: ScopeStatus): string {
  return statusLabels[status];
}
