import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import type { Activity } from '@/features/activities/types/activity';
import {
  getActiveLabel,
  getActivityOriginLabel,
  getPlanningTypeLabel,
  getPriorityLabel,
} from '@/features/activities/utils/activityLabels';
import './ActivitiesTable.css';

interface ActivitiesTableProps {
  activities: Activity[];
  canDelete: boolean;
  onEdit: (activity: Activity) => void;
  onDeactivate: (activity: Activity) => void;
}

function activeVariant(active: boolean): 'success' | 'neutral' {
  return active ? 'success' : 'neutral';
}

function originVariant(system: boolean): 'success' | 'neutral' {
  return system ? 'neutral' : 'success';
}

export function ActivitiesTable({
  activities,
  canDelete,
  onEdit,
  onDeactivate,
}: ActivitiesTableProps) {
  return (
    <div className="activities-table-wrapper">
      <table className="activities-table">
        <thead>
          <tr>
            <th scope="col">Kod</th>
            <th scope="col">Nazwa</th>
            <th scope="col">Pochodzenie</th>
            <th scope="col">Kategoria</th>
            <th scope="col">Typ planowania</th>
            <th scope="col">Domyślny okres</th>
            <th scope="col">Czas (min)</th>
            <th scope="col">Priorytet</th>
            <th scope="col">Status</th>
            <th scope="col">Akcje</th>
          </tr>
        </thead>
        <tbody>
          {activities.map((activity) => (
            <tr key={activity.id}>
              <td className="activities-table__code">{activity.code}</td>
              <td>{activity.name}</td>
              <td>
                <Badge variant={originVariant(activity.system)}>
                  {getActivityOriginLabel(activity.system)}
                </Badge>
              </td>
              <td>{activity.category}</td>
              <td>{getPlanningTypeLabel(activity.planningType)}</td>
              <td>{activity.defaultPeriod ?? '—'}</td>
              <td>{activity.durationMinutes ?? '—'}</td>
              <td>{getPriorityLabel(activity.priority)}</td>
              <td>
                <Badge variant={activeVariant(activity.active)}>
                  {getActiveLabel(activity.active)}
                </Badge>
              </td>
              <td>
                <div className="activities-table__actions">
                  {activity.manageable ? (
                    <Button variant="secondary" size="sm" onClick={() => onEdit(activity)}>
                      Edytuj
                    </Button>
                  ) : null}
                  {canDelete && activity.manageable && activity.active ? (
                    <Button variant="danger" size="sm" onClick={() => onDeactivate(activity)}>
                      Dezaktywuj
                    </Button>
                  ) : null}
                  {!activity.manageable ? (
                    <span className="activities-table__empty-cell">—</span>
                  ) : null}
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
