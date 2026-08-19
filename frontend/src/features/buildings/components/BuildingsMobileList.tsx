import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import type { Building } from '@/features/buildings/types/building';
import { formatDateDisplay } from '@/utils/dateFormat';
import './BuildingsMobileList.css';

interface BuildingsMobileListProps {
  buildings: Building[];
  canViewStaircases: boolean;
  canViewScopes: boolean;
  canEdit: boolean;
  canDelete: boolean;
  onStaircases: (building: Building) => void;
  onScopes: (building: Building) => void;
  onEdit: (building: Building) => void;
  onDeactivate: (building: Building) => void;
}

function statusLabel(status: Building['status']): string {
  return status === 'ACTIVE' ? 'Aktywny' : 'Nieaktywny';
}

function statusVariant(status: Building['status']): 'success' | 'neutral' {
  return status === 'ACTIVE' ? 'success' : 'neutral';
}

export function BuildingsMobileList({
  buildings,
  canViewStaircases,
  canViewScopes,
  canEdit,
  canDelete,
  onStaircases,
  onScopes,
  onEdit,
  onDeactivate,
}: BuildingsMobileListProps) {
  return (
    <div className="buildings-mobile-list">
      {buildings.map((building) => (
        <article key={building.id} className="buildings-mobile-card">
          <div className="buildings-mobile-card__header">
            <div>
              <p className="buildings-mobile-card__code">{building.code}</p>
              <h3 className="buildings-mobile-card__name">{building.name}</h3>
            </div>
            <Badge variant={statusVariant(building.status)}>
              {statusLabel(building.status)}
            </Badge>
          </div>

          <dl className="buildings-mobile-card__details">
            <div>
              <dt>Adres</dt>
              <dd>
                {building.address}, {building.city}
              </dd>
            </div>
            <div>
              <dt>Start obsługi</dt>
              <dd>{formatDateDisplay(building.serviceStartDate)}</dd>
            </div>
            <div>
              <dt>Wypowiedzenie</dt>
              <dd>{building.noticePeriodMonths} mies.</dd>
            </div>
          </dl>

          <div className="buildings-mobile-card__actions">
            {canViewStaircases ? (
              <Button variant="secondary" size="sm" onClick={() => onStaircases(building)}>
                Klatki
              </Button>
            ) : null}
            {canViewScopes ? (
              <Button variant="secondary" size="sm" onClick={() => onScopes(building)}>
                Zakresy
              </Button>
            ) : null}
            {canEdit ? (
              <Button variant="secondary" size="sm" onClick={() => onEdit(building)}>
                Edytuj
              </Button>
            ) : null}
            {canDelete && building.status === 'ACTIVE' ? (
              <Button variant="danger" size="sm" onClick={() => onDeactivate(building)}>
                Dezaktywuj
              </Button>
            ) : null}
          </div>
        </article>
      ))}
    </div>
  );
}
