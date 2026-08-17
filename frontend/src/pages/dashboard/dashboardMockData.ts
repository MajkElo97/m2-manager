export interface DashboardMetric {
  id: string;
  label: string;
  value: string;
  trend?: string;
}

export interface DashboardTask {
  id: string;
  title: string;
  dueDate: string;
  status: 'pending' | 'in_progress' | 'done';
}

export const dashboardMetricsMock: DashboardMetric[] = [
  { id: 'revenue', label: 'Przychód wspólnot', value: '128 450 zł', trend: '+4,2% m/m' },
  { id: 'buildings', label: 'Aktywne budynki', value: '37', trend: '+2 w tym miesiącu' },
  { id: 'employees', label: 'Pracownicy', value: '124', trend: '8 na urlopie' },
  { id: 'tasks', label: 'Zadania do wykonania', value: '18', trend: '5 pilnych' },
];

export const dashboardTasksMock: DashboardTask[] = [
  {
    id: '1',
    title: 'Przegląd windy — ul. Długa 12',
    dueDate: '2026-08-18',
    status: 'pending',
  },
  {
    id: '2',
    title: 'Rozliczenie mediów — Osiedle Słoneczne',
    dueDate: '2026-08-19',
    status: 'in_progress',
  },
  {
    id: '3',
    title: 'Kontrola instalacji — Budynek A',
    dueDate: '2026-08-20',
    status: 'pending',
  },
  {
    id: '4',
    title: 'Aktualizacja harmonogramu sprzątania',
    dueDate: '2026-08-21',
    status: 'pending',
  },
];
