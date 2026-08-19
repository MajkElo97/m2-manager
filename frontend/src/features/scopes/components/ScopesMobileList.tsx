import { Link } from 'react-router-dom';
import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import type { Activity } from '@/features/activities/types/activity';
import type { Building } from '@/features/buildings/types/building';
import type { Scope } from '@/features/scopes/types/scope';
import {
  getScopePlanningTypeLabel,
  getScopeStatusLabel,
} from '@/features/scopes/utils/scopeLabels';
import './ScopesMobileList.css';

interface ScopesMobileListProps {
  scopes: Scope[];
  canEdit: boolean;
  canDelete?: boolean;
  showBuilding?: boolean;
  buildingsById?: Map<string, Building>;
  activitiesById?: Map<string, Activity>;
  onEdit?: (scope: Scope) => void;
  onDeactivate?: (scope: Scope) => void;
  onBuildingClick?: (buildingId: string) => void;
}

function formatBuildingLabel(building: Building | undefined): string {
  if (!building) {
    return '—';
  }
  return `${building.name} · ${building.code}`;
}

function formatActivityLabel(activity: Activity | undefined): string {
  if (!activity) {
    return '—';
  }
  return `${activity.name} (${activity.code})`;
}

function statusVariant(status: Scope['status']): 'success' | 'neutral' {
  return status === 'ACTIVE' ? 'success' : 'neutral';
}

export function ScopesMobileList({
  scopes,
  canEdit,
  canDelete = false,
  showBuilding = false,
  buildingsById,
  activitiesById,
  onEdit,
  onDeactivate,
  onBuildingClick,
}: ScopesMobileListProps) {
  return (
    <div className="scopes-mobile-list">
      {scopes.map((scope) => {
        const building = buildingsById?.get(scope.buildingId);
        const activity = activitiesById?.get(scope.activityId);

        return (
          <article key={scope.id} className="scopes-mobile-card">
            <div className="scopes-mobile-card__header">
              <div>
                <p className="scopes-mobile-card__code">{scope.code}</p>
                <h3 className="scopes-mobile-card__title">{formatActivityLabel(activity)}</h3>
              </div>
              <Badge variant={statusVariant(scope.status)}>
                {getScopeStatusLabel(scope.status)}
              </Badge>
            </div>

            <dl className="scopes-mobile-card__details">
              {showBuilding ? (
                <div>
                  <dt>Budynek</dt>
                  <dd>
                    {onBuildingClick ? (
                      <button
                        type="button"
                        className="scopes-mobile-card__building-link"
                        onClick={() => onBuildingClick(scope.buildingId)}
                      >
                        {formatBuildingLabel(building)}
                      </button>
                    ) : (
                      <Link to={`/buildings/${scope.buildingId}/scopes`}>
                        {formatBuildingLabel(building)}
                      </Link>
                    )}
                  </dd>
                </div>
              ) : null}
              <div>
                <dt>Typ planowania</dt>
                <dd>{getScopePlanningTypeLabel(scope.planningType)}</dd>
              </div>
              <div>
                <dt>Częstotliwość</dt>
                <dd>{scope.frequency ?? '—'}</dd>
              </div>
              <div>
                <dt>Dzień/dni</dt>
                <dd>{scope.weekdays ?? '—'}</dd>
              </div>
            </dl>

            {canEdit ? (
              <div className="scopes-mobile-card__actions">
                {onEdit ? (
                  <Button variant="secondary" size="sm" onClick={() => onEdit(scope)}>
                    Edytuj
                  </Button>
                ) : null}
                {canDelete && onDeactivate && scope.status === 'ACTIVE' ? (
                  <Button variant="danger" size="sm" onClick={() => onDeactivate(scope)}>
                    Dezaktywuj
                  </Button>
                ) : null}
              </div>
            ) : null}
          </article>
        );
      })}
    </div>
  );
}
