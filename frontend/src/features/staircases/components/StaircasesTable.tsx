import { Button } from '@/components/ui/Button';
import type { Staircase } from '@/features/staircases/types/staircase';
import './StaircasesTable.css';

interface StaircasesTableProps {
  staircases: Staircase[];
  canEdit: boolean;
  onEdit: (staircase: Staircase) => void;
  onDelete: (staircase: Staircase) => void;
}

function booleanLabel(value: boolean): string {
  return value ? 'TAK' : 'NIE';
}

export function StaircasesTable({ staircases, canEdit, onEdit, onDelete }: StaircasesTableProps) {
  return (
    <div className="staircases-table-wrapper">
      <table className="staircases-table">
        <thead>
          <tr>
            <th scope="col">Kod</th>
            <th scope="col">Klatka</th>
            <th scope="col">Domofon</th>
            <th scope="col">Klucz</th>
            <th scope="col">Winda</th>
            <th scope="col">Kondygnacje</th>
            <th scope="col">Uwagi</th>
            <th scope="col">Akcje</th>
          </tr>
        </thead>
        <tbody>
          {staircases.map((staircase) => (
            <tr key={staircase.id}>
              <td className="staircases-table__code">{staircase.code}</td>
              <td>{staircase.designation}</td>
              <td>{staircase.intercomCode ?? '—'}</td>
              <td>{booleanLabel(staircase.keyRequired)}</td>
              <td>{booleanLabel(staircase.elevator)}</td>
              <td>{staircase.floors}</td>
              <td>{staircase.notes ?? '—'}</td>
              <td>
                <div className="staircases-table__actions">
                  {canEdit ? (
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
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
