import { describe, expect, it } from 'vitest';
import {
  hasModuleAdmin,
  hasPermission,
} from '@/features/permissions/permissionUtils';

describe('permissionUtils', () => {
  it('hides unauthorized navigation modules', () => {
    const permissions = new Set(['DASHBOARD_VIEW', 'BUILDINGS_VIEW']);

    expect(hasPermission(permissions, 'BUILDINGS_VIEW')).toBe(true);
    expect(hasPermission(permissions, 'FINANCES_VIEW')).toBe(false);
    expect(hasModuleAdmin(permissions, 'FINANCES')).toBe(false);
  });

  it('treats module admin as override', () => {
    const permissions = new Set(['BUILDINGS_ADMIN']);

    expect(hasPermission(permissions, 'BUILDINGS_DELETE')).toBe(true);
  });
});
