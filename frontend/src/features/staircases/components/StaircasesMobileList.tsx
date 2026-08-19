import { Link } from 'react-router-dom';
import { Button } from '@/components/ui/Button';
import type { Building } from '@/features/buildings/types/building';
import type { Staircase } from '@/features/staircases/types/staircase';
import './StaircasesMobileList.css';

interface StaircasesMobileListProps {
  staircases: Staircase[];
  canEdit: boolean;
  showBuilding?: boolean;
  buildingsById?: Map<string, Building>;
  onEdit?: (staircase: Staircase) => void;
  onDelete?: (staircase: Staircase) => void;
  onBuildingClick?: (buildingId: string) => void;
}

function booleanLabel(value: boolean): string {
  return value ? 'TAK' : 'NIE';
}

function formatBuildingLabel(building: Building | undefined): string {
  if (!building) {
    return '—';
  }
  return `${building.name} · ${building.code}`;
}

export function StaircasesMobileList({
  staircases,
  canEdit,
  showBuilding = false,
  buildingsById,
  onEdit,
  onDelete,
  onBuildingClick,
}: StaircasesMobileListProps) {
  return (
    <div className="staircases-mobile-list">
      {staircases.map((staircase) => {
        const building = buildingsById?.get(staircase.buildingId);

        return (
          <article key={staircase.id} className="staircases-mobile-card">
            <div className="staircases-mobile-card__header">
              <div>
                <p className="staircases-mobile-card__code">{staircase.code}</p>
                <h3 className="staircases-mobile-card__designation">Klatka {staircase.designation}</h3>
              </div>
            </div>

            <dl className="staircases-mobile-card__details">
              {showBuilding ? (
                <div>
                  <dt>Budynek</dt>
                  <dd>
                    {onBuildingClick ? (
                      <button
                        type="button"
                        className="staircases-mobile-card__building-link"
                        onClick={() => onBuildingClick(staircase.buildingId)}
                      >
                        {formatBuildingLabel(building)}
                      </button>
                    ) : (
                      <Link
                        to={`/buildings/${staircase.buildingId}/staircases`}
                        className="staircases-mobile-card__building-link"
                      >
                        {formatBuildingLabel(building)}
                      </Link>
                    )}
                  </dd>
                </div>
              ) : null}
              <div>
                <dt>Domofon</dt>
                <dd>{staircase.intercomCode ?? '—'}</dd>
              </div>
              <div>
                <dt>Klucz</dt>
                <dd>{booleanLabel(staircase.keyRequired)}</dd>
              </div>
              <div>
                <dt>Winda</dt>
                <dd>{booleanLabel(staircase.elevator)}</dd>
              </div>
              <div>
                <dt>Kondygnacje</dt>
                <dd>{staircase.floors}</dd>
              </div>
            </dl>

            {canEdit && onEdit && onDelete ? (
              <div className="staircases-mobile-card__actions">
                <Button variant="secondary" size="sm" onClick={() => onEdit(staircase)}>
                  Edytuj
                </Button>
                <Button variant="danger" size="sm" onClick={() => onDelete(staircase)}>
                  Usuń
                </Button>
              </div>
            ) : null}
          </article>
        );
      })}
    </div>
  );
}
