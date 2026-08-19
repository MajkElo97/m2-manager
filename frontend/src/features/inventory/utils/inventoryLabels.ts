import type { ChemicalUnit } from '@/features/inventory/types/chemical';
import type { EquipmentCondition } from '@/features/inventory/types/equipment';

export function displayValue(value: string | null | undefined): string {
  if (value == null || value.trim() === '') {
    return '—';
  }
  return value;
}

export function getActiveLabel(active: boolean): string {
  return active ? 'Aktywny' : 'Nieaktywny';
}

export function getEquipmentConditionLabel(condition: EquipmentCondition): string {
  switch (condition) {
    case 'GOOD':
      return 'Dobry';
    case 'USED':
      return 'Używany';
    case 'DAMAGED':
      return 'Uszkodzony';
    case 'OUT_OF_SERVICE':
      return 'Wyłączony z użytku';
    default:
      return condition;
  }
}

export function getChemicalUnitLabel(unit: ChemicalUnit): string {
  switch (unit) {
    case 'LITER':
      return 'l';
    case 'KILOGRAM':
      return 'kg';
    case 'PIECE':
      return 'szt.';
    case 'PACK':
      return 'opak.';
    case 'OTHER':
      return 'inne';
    default:
      return unit;
  }
}

export function formatQuantity(value: number | null | undefined): string {
  if (value == null) {
    return '—';
  }
  return String(value);
}

export function formatMinimumStock(value: number | null | undefined): string {
  if (value == null) {
    return '—';
  }
  return String(value);
}

export function getEmployeeDisplayName(
  employeeName: string | null,
  employeeCode: string | null,
): string {
  if (employeeName?.trim()) {
    return employeeName;
  }
  if (employeeCode?.trim()) {
    return employeeCode;
  }
  return '—';
}

export function isLowStock(quantity: number, minimumStock: number | null): boolean {
  if (minimumStock == null) {
    return false;
  }
  return quantity < minimumStock;
}

export function getStockStatusLabel(lowStock: boolean): string {
  return lowStock ? 'NISKI STAN' : 'W normie';
}
