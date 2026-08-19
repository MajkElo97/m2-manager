import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import type { Activity } from '@/features/activities/types/activity';
import {
  getActiveLabel,
  getPlanningTypeLabel,
  getPriorityLabel,
} from '@/features/activities/utils/activityLabels';
import './ActivitiesMobileList.css';

interface ActivitiesMobileListProps {
  activities: Activity[];
  canEdit: boolean;
  canDelete: boolean;
  onEdit: (activity: Activity) => void;
  onDeactivate: (activity: Activity) => void;
}

function activeVariant(active: boolean): 'success' | 'neutral' {
  return active ? 'success' : 'neutral';
}

export function ActivitiesMobileList({
  activities,
  canEdit,
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
            <Badge variant={activeVariant(activity.active)}>
              {getActiveLabel(activity.active)}
            </Badge>
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

          <div className="activities-mobile-card__actions">
            {canEdit ? (
              <Button variant="secondary" size="sm" onClick={() => onEdit(activity)}>
                Edytuj
              </Button>
            ) : null}
            {canDelete && activity.active ? (
              <Button variant="danger" size="sm" onClick={() => onDeactivate(activity)}>
                Dezaktywuj
              </Button>
            ) : null}
          </div>
        </article>
      ))}
    </div>
  );
}
