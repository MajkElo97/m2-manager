import { displayValue, getFullName } from '@/features/employees/utils/employeeLabels';
import type { User } from '@/features/users/types/user';

export { displayValue, getFullName };

export function formatUserRoles(user: User): string {
  if (user.roles.length === 0) {
    return '—';
  }
  return user.roles.map((role) => role.name).join(', ');
}

export function formatLinkedEmployee(user: User): string {
  if (user.employeeDisplayName) {
    return user.employeeCode
      ? `${user.employeeDisplayName} (${user.employeeCode})`
      : user.employeeDisplayName;
  }
  return '—';
}

export function getRoleTypeLabel(systemRole: boolean): string {
  return systemRole ? 'Systemowa' : 'Własna';
}
