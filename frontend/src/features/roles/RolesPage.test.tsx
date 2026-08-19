import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { useEffect } from 'react';
import { Route, Routes } from 'react-router-dom';
import { describe, expect, it, vi, beforeEach, afterEach } from 'vitest';
import { RolesPage } from '@/features/roles/pages/RolesPage';
import { SYSTEM_ROLE_READONLY_HINT } from '@/features/roles/rolesMessages';
import type { Role } from '@/features/roles/types/role';
import type { Permission } from '@/features/roles/types/permission';
import { usePermissions } from '@/features/permissions/PermissionProvider';
import { renderWithProviders } from '@/test/testUtils';

const roles: Role[] = [
  {
    id: 'r0000000-0000-4000-8000-000000000001',
    name: 'SUPER_ADMIN',
    description: 'Super administrator',
    systemRole: true,
    active: true,
    userCount: 1,
    permissionCount: 85,
  },
  {
    id: 'r0000000-0000-4000-8000-000000000002',
    name: 'BIURO',
    description: 'Biuro',
    systemRole: false,
    active: true,
    userCount: 3,
    permissionCount: 12,
  },
];

const permissions: Permission[] = [
  {
    code: 'DASHBOARD_VIEW',
    module: 'DASHBOARD',
    action: 'VIEW',
    description: 'VIEW DASHBOARD module',
  },
  {
    code: 'DASHBOARD_CREATE',
    module: 'DASHBOARD',
    action: 'CREATE',
    description: 'CREATE DASHBOARD module',
  },
  {
    code: 'DASHBOARD_EDIT',
    module: 'DASHBOARD',
    action: 'EDIT',
    description: 'EDIT DASHBOARD module',
  },
  {
    code: 'DASHBOARD_DELETE',
    module: 'DASHBOARD',
    action: 'DELETE',
    description: 'DELETE DASHBOARD module',
  },
  {
    code: 'DASHBOARD_ADMIN',
    module: 'DASHBOARD',
    action: 'ADMIN',
    description: 'ADMIN DASHBOARD module',
  },
];

function createRolesFetchMock(options: { listStatus?: number } = {}) {
  return vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
    const url = String(input);

    if (url.includes('/api/roles') && url.includes('/permissions') && (!init?.method || init.method === 'GET')) {
      return Response.json(permissions.filter((permission) => permission.action === 'VIEW'));
    }

    if (url.includes('/api/roles') && (!init?.method || init.method === 'GET')) {
      if (options.listStatus === 403) {
        return Response.json({ status: 403, message: 'Forbidden' }, { status: 403 });
      }

      return Response.json(roles);
    }

    if (url.includes('/api/permissions') && (!init?.method || init.method === 'GET')) {
      return Response.json(permissions);
    }

    return new Response(null, { status: 404 });
  });
}

function RolesPageHarness() {
  const { loadPermissions } = usePermissions();

  useEffect(() => {
    void loadPermissions();
  }, [loadPermissions]);

  return (
    <Routes>
      <Route path="/roles" element={<RolesPage />} />
    </Routes>
  );
}

function renderRolesPage(permissionsList: string[] = ['ROLES_VIEW', 'ROLES_EDIT']) {
  return renderWithProviders(<RolesPageHarness />, {
    routerProps: { initialEntries: ['/roles'] },
    permissionsAdapter: {
      loadPermissions: async () => permissionsList,
    },
  });
}

describe('RolesPage', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', createRolesFetchMock());
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('renders page header and role list', async () => {
    renderRolesPage();

    expect(await screen.findByRole('heading', { name: 'Role' })).toBeInTheDocument();
    expect(within(screen.getByRole('table')).getByText('SUPER_ADMIN')).toBeInTheDocument();
    expect(within(screen.getByRole('table')).getByText('BIURO')).toBeInTheDocument();
    expect(within(screen.getByRole('table')).getByText('Systemowa')).toBeInTheDocument();
    expect(within(screen.getByRole('table')).getByText('Własna')).toBeInTheDocument();
  });

  it('shows system role protection hint in read-only form', async () => {
    const user = userEvent.setup();
    renderRolesPage();

    await waitFor(() => {
      expect(within(screen.getByRole('table')).getByText('SUPER_ADMIN')).toBeInTheDocument();
    });

    const editButtons = within(screen.getByRole('table')).getAllByRole('button', { name: 'Edytuj' });
    await user.click(editButtons[0]);

    expect(await screen.findByText(SYSTEM_ROLE_READONLY_HINT)).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Zapisz zmiany' })).not.toBeInTheDocument();
    expect(screen.getAllByRole('button', { name: 'Zamknij' }).length).toBeGreaterThan(0);
  });

  it('handles API 403 with forbidden message', async () => {
    vi.stubGlobal('fetch', createRolesFetchMock({ listStatus: 403 }));

    renderRolesPage();

    expect(await screen.findByText('Nie masz uprawnień do tej operacji.')).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Brak dostępu' })).toBeInTheDocument();
  });
});
