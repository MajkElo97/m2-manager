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
import './ScopesTable.css';

interface ScopesTableProps {
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

export function ScopesTable({
  scopes,
  canEdit,
  canDelete = false,
  showBuilding = false,
  buildingsById,
  activitiesById,
  onEdit,
  onDeactivate,
  onBuildingClick,
}: ScopesTableProps) {
  return (
    <div className="scopes-table-wrapper">
      <table className="scopes-table">
        <thead>
          <tr>
            <th scope="col">Kod</th>
            {showBuilding ? <th scope="col">Budynek</th> : null}
            <th scope="col">Czynność</th>
            <th scope="col">Typ planowania</th>
            <th scope="col">Częstotliwość</th>
            <th scope="col">Dzień/dni</th>
            <th scope="col">Uwagi</th>
            <th scope="col">Status</th>
            {canEdit ? <th scope="col">Akcje</th> : null}
          </tr>
        </thead>
        <tbody>
          {scopes.map((scope) => {
            const building = buildingsById?.get(scope.buildingId);
            const activity = activitiesById?.get(scope.activityId);

            return (
              <tr key={scope.id}>
                <td className="scopes-table__code">{scope.code}</td>
                {showBuilding ? (
                  <td>
                    {onBuildingClick ? (
                      <button
                        type="button"
                        className="scopes-table__building-link"
                        onClick={() => onBuildingClick(scope.buildingId)}
                      >
                        {formatBuildingLabel(building)}
                      </button>
                    ) : (
                      <Link
                        to={`/buildings/${scope.buildingId}/scopes`}
                        className="scopes-table__building-link"
                      >
                        {formatBuildingLabel(building)}
                      </Link>
                    )}
                  </td>
                ) : null}
                <td>{formatActivityLabel(activity)}</td>
                <td>{getScopePlanningTypeLabel(scope.planningType)}</td>
                <td>{scope.frequency ?? '—'}</td>
                <td>{scope.weekdays ?? '—'}</td>
                <td>{scope.notes ?? '—'}</td>
                <td>
                  <Badge variant={statusVariant(scope.status)}>
                    {getScopeStatusLabel(scope.status)}
                  </Badge>
                </td>
                {canEdit ? (
                  <td>
                    <div className="scopes-table__actions">
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
                      {!onEdit && !onDeactivate ? (
                        <span className="scopes-table__empty-cell">—</span>
                      ) : null}
                    </div>
                  </td>
                ) : null}
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
