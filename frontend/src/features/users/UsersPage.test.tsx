import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { useEffect } from 'react';
import { Route, Routes } from 'react-router-dom';
import { describe, expect, it, vi, beforeEach, afterEach } from 'vitest';
import { UsersPage } from '@/features/users/pages/UsersPage';
import type { User } from '@/features/users/types/user';
import type { Role } from '@/features/roles/types/role';
import { usePermissions } from '@/features/permissions/PermissionProvider';
import { renderWithProviders } from '@/test/testUtils';

const roles: Role[] = [
  {
    id: 'r0000000-0000-4000-8000-000000000001',
    name: 'ADMIN',
    description: 'Administrator',
    systemRole: true,
    active: true,
    userCount: 1,
    permissionCount: 10,
  },
  {
    id: 'r0000000-0000-4000-8000-000000000002',
    name: 'BIURO',
    description: 'Biuro',
    systemRole: false,
    active: true,
    userCount: 2,
    permissionCount: 5,
  },
];

const users: User[] = [
  {
    id: 'u0000000-0000-4000-8000-000000000001',
    firstName: 'Jan',
    lastName: 'Kowalski',
    email: 'jan@example.com',
    active: true,
    roles: [{ id: roles[0].id, name: 'ADMIN', systemRole: true }],
    employeeId: null,
    employeeCode: null,
    employeeDisplayName: null,
    createdAt: '2025-01-01T00:00:00Z',
    updatedAt: '2025-01-01T00:00:00Z',
  },
  {
    id: 'u0000000-0000-4000-8000-000000000002',
    firstName: 'Anna',
    lastName: 'Nowak',
    email: 'anna@example.com',
    active: true,
    roles: [{ id: roles[1].id, name: 'BIURO', systemRole: false }],
    employeeId: 'e0000000-0000-4000-8000-000000000001',
    employeeCode: 'PR0001',
    employeeDisplayName: 'Anna Nowak',
    createdAt: '2025-01-01T00:00:00Z',
    updatedAt: '2025-01-01T00:00:00Z',
  },
];

function createUsersFetchMock(options: { listStatus?: number } = {}) {
  return vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
    const url = String(input);

    if (url.includes('/api/users') && (!init?.method || init.method === 'GET')) {
      if (options.listStatus === 403) {
        return Response.json({ status: 403, message: 'Forbidden' }, { status: 403 });
      }

      const searchParams = new URL(url, 'http://localhost').searchParams;
      let result = users;

      const roleId = searchParams.get('roleId');
      if (roleId) {
        result = result.filter((user) => user.roles.some((role) => role.id === roleId));
      }

      return Response.json(result);
    }

    if (url.includes('/api/roles') && (!init?.method || init.method === 'GET')) {
      return Response.json(roles);
    }

    if (url.includes('/api/employees') && (!init?.method || init.method === 'GET')) {
      return Response.json([]);
    }

    return new Response(null, { status: 404 });
  });
}

function UsersPageHarness() {
  const { loadPermissions } = usePermissions();

  useEffect(() => {
    void loadPermissions();
  }, [loadPermissions]);

  return (
    <Routes>
      <Route path="/users" element={<UsersPage />} />
    </Routes>
  );
}

function renderUsersPage(permissions: string[] = ['USERS_VIEW']) {
  return renderWithProviders(<UsersPageHarness />, {
    routerProps: { initialEntries: ['/users'] },
    permissionsAdapter: {
      loadPermissions: async () => permissions,
    },
  });
}

describe('UsersPage', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', createUsersFetchMock());
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('renders page header and user list', async () => {
    renderUsersPage();

    expect(await screen.findByRole('heading', { name: 'Użytkownicy' })).toBeInTheDocument();
    expect(within(screen.getByRole('table')).getByText('Jan Kowalski')).toBeInTheDocument();
    expect(within(screen.getByRole('table')).getByText('Anna Nowak')).toBeInTheDocument();
  });

  it('filters by role', async () => {
    const user = userEvent.setup();
    renderUsersPage();

    await waitFor(() => {
      expect(within(screen.getByRole('table')).getByText('Jan Kowalski')).toBeInTheDocument();
    });

    await user.selectOptions(screen.getByLabelText('Filtr roli'), roles[1].id);

    expect(within(screen.getByRole('table')).getByText('Anna Nowak')).toBeInTheDocument();
    expect(within(screen.getByRole('table')).queryByText('Jan Kowalski')).not.toBeInTheDocument();
  });

  it('handles API 403 with forbidden message', async () => {
    vi.stubGlobal('fetch', createUsersFetchMock({ listStatus: 403 }));

    renderUsersPage();

    expect(await screen.findByText('Nie masz uprawnień do tej operacji.')).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Brak dostępu' })).toBeInTheDocument();
  });
});
