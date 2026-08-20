import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import type { Activity } from '@/features/activities/types/activity';
import {
  getActiveLabel,
  getActivityOriginLabel,
  getPlanningTypeLabel,
  getPriorityLabel,
} from '@/features/activities/utils/activityLabels';
import './ActivitiesMobileList.css';

interface ActivitiesMobileListProps {
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

export function ActivitiesMobileList({
  activities,
  canDelete,
  onEdit,
  onDeactivate,
}: ActivitiesMobileListProps) {
  return (
    <div className="activities-mobile-list">
      {activities.map((activity) => (
        <article key={activity.id} className="activities-mobile-card">
          <div className="activities-mobile-card__header">
            <div>
              <p className="activities-mobile-card__code">{activity.code}</p>
              <h3 className="activities-mobile-card__name">{activity.name}</h3>
            </div>
            <div className="activities-mobile-card__badges">
              <Badge variant={originVariant(activity.system)}>
                {getActivityOriginLabel(activity.system)}
              </Badge>
              <Badge variant={activeVariant(activity.active)}>
                {getActiveLabel(activity.active)}
              </Badge>
            </div>
          </div>

          <dl className="activities-mobile-card__details">
            <div>
              <dt>Kategoria</dt>
              <dd>{activity.category}</dd>
            </div>
            <div>
              <dt>Typ planowania</dt>
              <dd>{getPlanningTypeLabel(activity.planningType)}</dd>
            </div>
            <div>
              <dt>Priorytet</dt>
              <dd>{getPriorityLabel(activity.priority)}</dd>
            </div>
            <div>
              <dt>Czas (min)</dt>
              <dd>{activity.durationMinutes ?? '—'}</dd>
            </div>
          </dl>

          {activity.manageable ? (
            <div className="activities-mobile-card__actions">
              <Button variant="secondary" size="sm" onClick={() => onEdit(activity)}>
                Edytuj
              </Button>
              {canDelete && activity.active ? (
                <Button variant="danger" size="sm" onClick={() => onDeactivate(activity)}>
                  Dezaktywuj
                </Button>
              ) : null}
            </div>
          ) : null}
        </article>
      ))}
    </div>
  );
}
