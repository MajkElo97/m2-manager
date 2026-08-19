import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { ExpiryDateBadge } from '@/features/fleet/components/ExpiryDateBadge';
import type { Vehicle } from '@/features/fleet/types/vehicle';
import {
  canDeactivateVehicle,
  formatMileage,
  getEmployeeDisplayName,
  getVehicleDisplayName,
  getVehicleStatusLabel,
  getVehicleTypeLabel,
  vehicleStatusVariant,
} from '@/features/fleet/utils/vehicleLabels';
import './FleetMobileList.css';

interface FleetMobileListProps {
  vehicles: Vehicle[];
  canEdit: boolean;
  canDelete: boolean;
  onEdit: (vehicle: Vehicle) => void;
  onDeactivate: (vehicle: Vehicle) => void;
}

export function FleetMobileList({
  vehicles,
  canEdit,
  canDelete,
  onEdit,
  onDeactivate,
}: FleetMobileListProps) {
  return (
    <div className="buildings-mobile-list">
      {vehicles.map((vehicle) => (
        <article key={vehicle.id} className="buildings-mobile-card">
          <div className="buildings-mobile-card__header">
            <h3 className="buildings-mobile-card__name">
              {vehicle.registrationNumber} — {vehicle.code}
            </h3>
            <Badge variant={vehicleStatusVariant(vehicle.status)}>
              {getVehicleStatusLabel(vehicle.status)}
            </Badge>
          </div>

          <dl className="buildings-mobile-card__details">
            <div>
              <dt>Samochód</dt>
              <dd>
                {getVehicleDisplayName(vehicle.make, vehicle.model, vehicle.productionYear)}
              </dd>
            </div>
            <div>
              <dt>Typ</dt>
              <dd>{getVehicleTypeLabel(vehicle.vehicleType)}</dd>
            </div>
            <div>
              <dt>Pracownik</dt>
              <dd>{getEmployeeDisplayName(vehicle.employeeName, vehicle.employeeCode)}</dd>
            </div>
            <div>
              <dt>OC</dt>
              <dd>
                <ExpiryDateBadge date={vehicle.insuranceEndDate} kind="insurance" />
              </dd>
            </div>
            <div>
              <dt>Przegląd</dt>
              <dd>
                <ExpiryDateBadge date={vehicle.nextInspectionDate} kind="inspection" />
              </dd>
            </div>
            <div>
              <dt>Przebieg</dt>
              <dd>{formatMileage(vehicle.currentMileage)}</dd>
            </div>
          </dl>

          <div className="buildings-mobile-card__actions">
            {canEdit ? (
              <Button variant="secondary" size="sm" onClick={() => onEdit(vehicle)}>
                Edytuj
              </Button>
            ) : null}
            {canDelete && canDeactivateVehicle(vehicle.status) ? (
              <Button variant="danger" size="sm" onClick={() => onDeactivate(vehicle)}>
                Dezaktywuj
              </Button>
            ) : null}
          </div>
        </article>
      ))}
    </div>
  );
}
