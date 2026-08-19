import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { useEffect } from 'react';
import { Route, Routes } from 'react-router-dom';
import { describe, expect, it, vi, beforeEach, afterEach } from 'vitest';
import { EmployeesPage } from '@/features/employees/pages/EmployeesPage';
import type { Employee } from '@/features/employees/types/employee';
import { usePermissions } from '@/features/permissions/PermissionProvider';
import { renderWithProviders } from '@/test/testUtils';

const employees: Employee[] = [
  {
    id: 'e0000000-0000-4000-8000-000000000001',
    code: 'PR0001',
    firstName: 'Jan',
    lastName: 'Kowalski',
    phone: '500600700',
    email: 'jan@example.com',
    googleEmail: null,
    position: 'Sprzątacz',
    role: 'PRACOWNIK',
    employmentType: 'ZLECENIE',
    employmentStartDate: '2024-01-15',
    remunerationAmount: 20,
    remunerationUnit: 'HOURLY',
    remunerationNet: true,
    calendarColor: '#FF5733',
    notes: null,
    active: true,
    createdAt: '2025-01-01T00:00:00Z',
    updatedAt: '2025-01-01T00:00:00Z',
  },
  {
    id: 'e0000000-0000-4000-8000-000000000002',
    code: 'PR0002',
    firstName: 'Anna',
    lastName: 'Nowak',
    phone: null,
    email: 'anna@example.com',
    googleEmail: null,
    position: 'Admin',
    role: 'ADMIN',
    employmentType: null,
    employmentStartDate: null,
    remunerationAmount: null,
    remunerationUnit: null,
    remunerationNet: null,
    calendarColor: null,
    notes: null,
    active: true,
    createdAt: '2025-01-01T00:00:00Z',
    updatedAt: '2025-01-01T00:00:00Z',
  },
];

function createEmployeesFetchMock(options: { listStatus?: number } = {}) {
  return vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
    const url = String(input);

    if (url.includes('/api/employees') && (!init?.method || init.method === 'GET')) {
      if (options.listStatus === 403) {
        return Response.json({ status: 403, message: 'Forbidden' }, { status: 403 });
      }

      const searchParams = new URL(url, 'http://localhost').searchParams;
      let result = employees;

      const role = searchParams.get('role');
      if (role) {
        result = result.filter((employee) => employee.role === role);
      }

      return Response.json(result);
    }

    return new Response(null, { status: 404 });
  });
}

function EmployeesPageHarness() {
  const { loadPermissions } = usePermissions();

  useEffect(() => {
    void loadPermissions();
  }, [loadPermissions]);

  return (
    <Routes>
      <Route path="/employees" element={<EmployeesPage />} />
    </Routes>
  );
}

function renderEmployeesPage(permissions: string[] = ['EMPLOYEES_VIEW']) {
  return renderWithProviders(<EmployeesPageHarness />, {
    routerProps: { initialEntries: ['/employees'] },
    permissionsAdapter: {
      loadPermissions: async () => permissions,
    },
  });
}

describe('EmployeesPage', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', createEmployeesFetchMock());
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('renders page header and employee list', async () => {
    renderEmployeesPage();

    expect(await screen.findByRole('heading', { name: 'Pracownicy' })).toBeInTheDocument();
    expect(within(screen.getByRole('table')).getByText('Jan Kowalski')).toBeInTheDocument();
    expect(within(screen.getByRole('table')).getByText('Anna Nowak')).toBeInTheDocument();
  });

  it('displays remuneration and employment date formatting', async () => {
    renderEmployeesPage();

    await waitFor(() => {
      expect(within(screen.getByRole('table')).getByText('Jan Kowalski')).toBeInTheDocument();
    });

    expect(within(screen.getByRole('table')).getByText('20 zł/h netto')).toBeInTheDocument();
    expect(within(screen.getByRole('table')).getByText('15/01/2024')).toBeInTheDocument();
    expect(within(screen.getByRole('table')).getByText('#FF5733')).toBeInTheDocument();
  });

  it('filters by role', async () => {
    const user = userEvent.setup();
    renderEmployeesPage();

    await waitFor(() => {
      expect(within(screen.getByRole('table')).getByText('Jan Kowalski')).toBeInTheDocument();
    });

    await user.selectOptions(screen.getByLabelText('Filtr roli'), 'ADMIN');

    expect(within(screen.getByRole('table')).getByText('Anna Nowak')).toBeInTheDocument();
    expect(within(screen.getByRole('table')).queryByText('Jan Kowalski')).not.toBeInTheDocument();
  });

  it('handles API 403 with forbidden message', async () => {
    vi.stubGlobal('fetch', createEmployeesFetchMock({ listStatus: 403 }));

    renderEmployeesPage();

    expect(await screen.findByText('Nie masz uprawnień do tej operacji.')).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Brak dostępu' })).toBeInTheDocument();
  });
});
