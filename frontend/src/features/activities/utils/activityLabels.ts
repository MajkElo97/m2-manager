import type { ActivityPlanningType, ActivityPriority } from '@/features/activities/types/activity';

const planningTypeLabels: Record<ActivityPlanningType, string> = {
  CYCLIC: 'Cykliczna',
  PERIODIC: 'Okresowa',
  ON_DEMAND: 'Na żądanie',
};

const priorityLabels: Record<ActivityPriority, string> = {
  LOW: 'Niski',
  NORMAL: 'Normalny',
  HIGH: 'Wysoki',
};

export function getPlanningTypeLabel(planningType: ActivityPlanningType): string {
  return planningTypeLabels[planningType];
}

export function getPriorityLabel(priority: ActivityPriority): string {
  return priorityLabels[priority];
}

export function getActiveLabel(active: boolean): string {
  return active ? 'Aktywna' : 'Nieaktywna';
}

export function getActivityOriginLabel(system: boolean): string {
  return system ? 'Systemowa' : 'Własna organizacji';
}
