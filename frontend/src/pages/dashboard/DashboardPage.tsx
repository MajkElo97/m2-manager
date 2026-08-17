import { Badge } from '@/components/ui/Badge';
import { Card } from '@/components/ui/Card';
import { PageHeader } from '@/components/ui/PageHeader';
import { AppLayoutContainer } from '@/components/layout/AppLayout';
import { dashboardMetricsMock, dashboardTasksMock } from './dashboardMockData';
import './DashboardPage.css';

function taskStatusLabel(status: 'pending' | 'in_progress' | 'done'): string {
  switch (status) {
    case 'in_progress':
      return 'W trakcie';
    case 'done':
      return 'Zakończone';
    default:
      return 'Oczekujące';
  }
}

function taskStatusVariant(status: 'pending' | 'in_progress' | 'done'): 'neutral' | 'warning' | 'success' {
  switch (status) {
    case 'in_progress':
      return 'warning';
    case 'done':
      return 'success';
    default:
      return 'neutral';
  }
}

export function DashboardPage() {
  return (
    <AppLayoutContainer>
      <PageHeader
        title="Dashboard"
        description="Przegląd kluczowych wskaźników operacyjnych. Dane prezentacyjne — do podpięcia API w kolejnych fazach."
      />

      <section className="dashboard-grid" aria-label="Wskaźniki">
        {dashboardMetricsMock.map((metric) => (
          <Card key={metric.id} title={metric.label}>
            <p className="dashboard-metric__value">{metric.value}</p>
            {metric.trend ? <p className="dashboard-metric__trend">{metric.trend}</p> : null}
          </Card>
        ))}
      </section>

      <section className="dashboard-tasks" aria-label="Najbliższe zadania">
        <Card title="Najbliższe zadania">
          <ul className="dashboard-tasks__list">
            {dashboardTasksMock.map((task) => (
              <li key={task.id} className="dashboard-tasks__item">
                <div>
                  <p className="dashboard-tasks__title">{task.title}</p>
                  <p className="dashboard-tasks__date">Termin: {task.dueDate}</p>
                </div>
                <Badge variant={taskStatusVariant(task.status)}>
                  {taskStatusLabel(task.status)}
                </Badge>
              </li>
            ))}
          </ul>
        </Card>
      </section>
    </AppLayoutContainer>
  );
}
