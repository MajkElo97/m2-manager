import type { VehicleStatus, VehicleType } from '@/features/fleet/types/vehicle';
import { displayValue } from '@/features/employees/utils/employeeLabels';

export function getVehicleStatusLabel(status: VehicleStatus): string {
  switch (status) {
    case 'ACTIVE':
      return 'Aktywny';
    case 'IN_SERVICE':
      return 'W serwisie';
    case 'INACTIVE':
      return 'Nieaktywny';
    case 'SOLD':
      return 'Sprzedany';
    default:
      return status;
  }
}

export function getVehicleTypeLabel(type: VehicleType): string {
  switch (type) {
    case 'PASSENGER':
      return 'Osobowy';
    case 'DELIVERY':
      return 'Dostawczy';
    case 'VAN':
      return 'Van';
    case 'OTHER':
      return 'Inny';
    default:
      return type;
  }
}

export function getVehicleDisplayName(
  make: string,
  model: string,
  productionYear: number | null,
): string {
  const name = `${make} ${model}`.trim();
  if (productionYear != null) {
    return `${name} (${productionYear})`;
  }
  return name;
}

export function formatMileage(mileage: number | null | undefined): string {
  if (mileage == null) {
    return '—';
  }

  return `${mileage.toLocaleString('pl-PL')} km`;
}

export function getEmployeeDisplayName(
  employeeName: string | null,
  employeeCode: string | null,
): string {
  if (employeeName) {
    return employeeCode ? `${employeeName} (${employeeCode})` : employeeName;
  }

  return displayValue(employeeCode);
}

export function vehicleStatusVariant(status: VehicleStatus): 'success' | 'warning' | 'neutral' {
  switch (status) {
    case 'ACTIVE':
      return 'success';
    case 'IN_SERVICE':
      return 'warning';
    default:
      return 'neutral';
  }
}

export function canDeactivateVehicle(status: VehicleStatus): boolean {
  return status !== 'INACTIVE';
}
