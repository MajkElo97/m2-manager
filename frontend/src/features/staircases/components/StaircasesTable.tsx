import { Link } from 'react-router-dom';
import { Button } from '@/components/ui/Button';
import type { Building } from '@/features/buildings/types/building';
import type { Staircase } from '@/features/staircases/types/staircase';
import './StaircasesTable.css';

interface StaircasesTableProps {
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

export function StaircasesTable({
  staircases,
  canEdit,
  showBuilding = false,
  buildingsById,
  onEdit,
  onDelete,
  onBuildingClick,
}: StaircasesTableProps) {
  return (
    <div className="staircases-table-wrapper">
      <table className="staircases-table">
        <thead>
          <tr>
            <th scope="col">Kod</th>
            {showBuilding ? <th scope="col">Budynek</th> : null}
            <th scope="col">Klatka</th>
            <th scope="col">Kod domofonu</th>
            <th scope="col">Klucz wymagany</th>
            <th scope="col">Winda</th>
            <th scope="col">Kondygnacje</th>
            <th scope="col">Uwagi</th>
            {canEdit ? <th scope="col">Akcje</th> : null}
          </tr>
        </thead>
        <tbody>
          {staircases.map((staircase) => {
            const building = buildingsById?.get(staircase.buildingId);

            return (
              <tr key={staircase.id}>
                <td className="staircases-table__code">{staircase.code}</td>
                {showBuilding ? (
                  <td>
                    {onBuildingClick ? (
                      <button
                        type="button"
                        className="staircases-table__building-link"
                        onClick={() => onBuildingClick(staircase.buildingId)}
                      >
                        {formatBuildingLabel(building)}
                      </button>
                    ) : (
                      <Link
                        to={`/buildings/${staircase.buildingId}/staircases`}
                        className="staircases-table__building-link"
                      >
                        {formatBuildingLabel(building)}
                      </Link>
                    )}
                  </td>
                ) : null}
                <td>{staircase.designation}</td>
                <td>{staircase.intercomCode ?? '—'}</td>
                <td>{booleanLabel(staircase.keyRequired)}</td>
                <td>{booleanLabel(staircase.elevator)}</td>
                <td>{staircase.floors}</td>
                <td>{staircase.notes ?? '—'}</td>
                {canEdit ? (
                  <td>
                    <div className="staircases-table__actions">
                      {onEdit && onDelete ? (
                        <>
                          <Button variant="secondary" size="sm" onClick={() => onEdit(staircase)}>
                            Edytuj
                          </Button>
                          <Button variant="danger" size="sm" onClick={() => onDelete(staircase)}>
                            Usuń
                          </Button>
                        </>
                      ) : (
                        <span className="staircases-table__empty-cell">—</span>
                      )}
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
