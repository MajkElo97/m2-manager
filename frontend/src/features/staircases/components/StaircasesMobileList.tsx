import { Button } from '@/components/ui/Button';
import type { Staircase } from '@/features/staircases/types/staircase';
import './StaircasesMobileList.css';

interface StaircasesMobileListProps {
  staircases: Staircase[];
  canEdit: boolean;
  onEdit: (staircase: Staircase) => void;
  onDelete: (staircase: Staircase) => void;
}

function booleanLabel(value: boolean): string {
  return value ? 'TAK' : 'NIE';
}

export function StaircasesMobileList({
  staircases,
  canEdit,
  onEdit,
  onDelete,
}: StaircasesMobileListProps) {
  return (
    <div className="staircases-mobile-list">
      {staircases.map((staircase) => (
        <article key={staircase.id} className="staircases-mobile-card">
          <div className="staircases-mobile-card__header">
            <div>
              <p className="staircases-mobile-card__code">{staircase.code}</p>
              <h3 className="staircases-mobile-card__designation">Klatka {staircase.designation}</h3>
            </div>
          </div>

          <dl className="staircases-mobile-card__details">
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

          {canEdit ? (
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
      ))}
    </div>
  );
}
