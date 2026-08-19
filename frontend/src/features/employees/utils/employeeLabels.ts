import type {
  EmployeeRole,
  EmploymentType,
  RemunerationUnit,
} from '@/features/employees/types/employee';

export function displayValue(value: string | null | undefined): string {
  if (value == null || value.trim() === '') {
    return '—';
  }
  return value;
}

export function getFullName(firstName: string | null, lastName: string | null): string {
  const name = [firstName, lastName].filter(Boolean).join(' ');
  return name || '—';
}

export function getEmployeeRoleLabel(role: EmployeeRole): string {
  switch (role) {
    case 'PRACOWNIK':
      return 'Pracownik';
    case 'ADMIN':
      return 'Admin';
    default:
      return role;
  }
}

export function getEmploymentTypeLabel(type: EmploymentType | null): string {
  if (!type) {
    return '—';
  }
  switch (type) {
    case 'ZLECENIE':
      return 'Zlecenie';
    default:
      return type;
  }
}

export function getRemunerationUnitLabel(unit: RemunerationUnit | null): string {
  if (!unit) {
    return '';
  }
  switch (unit) {
    case 'HOURLY':
      return 'zł/h';
    default:
      return '';
  }
}

export function formatRemuneration(
  amount: number | null,
  unit: RemunerationUnit | null,
  net: boolean | null,
): string {
  if (amount == null) {
    return '—';
  }

  const unitLabel = getRemunerationUnitLabel(unit);
  const netLabel = net === true ? 'netto' : net === false ? 'brutto' : '';
  const parts = [`${amount}`, unitLabel, netLabel].filter(Boolean);
  return parts.join(' ');
}

export function getActiveLabel(active: boolean): string {
  return active ? 'Aktywny' : 'Nieaktywny';
}
