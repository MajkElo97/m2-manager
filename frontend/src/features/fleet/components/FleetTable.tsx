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
import './FleetTable.css';

interface FleetTableProps {
  vehicles: Vehicle[];
  canEdit: boolean;
  canDelete: boolean;
  onEdit: (vehicle: Vehicle) => void;
  onDeactivate: (vehicle: Vehicle) => void;
}

export function FleetTable({
  vehicles,
  canEdit,
  canDelete,
  onEdit,
  onDeactivate,
}: FleetTableProps) {
  return (
    <div className="buildings-table-wrapper">
      <table className="buildings-table">
        <thead>
          <tr>
            <th scope="col">Kod</th>
            <th scope="col">Rejestracja</th>
            <th scope="col">Samochód</th>
            <th scope="col">Typ</th>
            <th scope="col">Pracownik</th>
            <th scope="col">OC</th>
            <th scope="col">Przegląd</th>
            <th scope="col">Przebieg</th>
            <th scope="col">Status</th>
            <th scope="col">Akcje</th>
          </tr>
        </thead>
        <tbody>
          {vehicles.map((vehicle) => (
            <tr key={vehicle.id}>
              <td className="buildings-table__code">{vehicle.code}</td>
              <td>{vehicle.registrationNumber}</td>
              <td>
                {getVehicleDisplayName(vehicle.make, vehicle.model, vehicle.productionYear)}
              </td>
              <td>{getVehicleTypeLabel(vehicle.vehicleType)}</td>
              <td>{getEmployeeDisplayName(vehicle.employeeName, vehicle.employeeCode)}</td>
              <td>
                <ExpiryDateBadge date={vehicle.insuranceEndDate} kind="insurance" />
              </td>
              <td>
                <ExpiryDateBadge date={vehicle.nextInspectionDate} kind="inspection" />
              </td>
              <td>{formatMileage(vehicle.currentMileage)}</td>
              <td>
                <Badge variant={vehicleStatusVariant(vehicle.status)}>
                  {getVehicleStatusLabel(vehicle.status)}
                </Badge>
              </td>
              <td>
                <div className="buildings-table__actions">
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
                  {!canEdit && !canDelete ? (
                    <span className="buildings-table__empty-cell">—</span>
                  ) : null}
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
