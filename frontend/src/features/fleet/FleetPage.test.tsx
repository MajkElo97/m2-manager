import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { useEffect } from 'react';
import { Route, Routes } from 'react-router-dom';
import { describe, expect, it, vi, beforeEach, afterEach } from 'vitest';
import { FleetPage } from '@/features/fleet/pages/FleetPage';
import type { Vehicle } from '@/features/fleet/types/vehicle';
import { usePermissions } from '@/features/permissions/PermissionProvider';
import { renderWithProviders } from '@/test/testUtils';

const vehicles: Vehicle[] = [
  {
    id: 'f4000000-0000-4000-8000-000000000001',
    code: 'FL0001',
    registrationNumber: 'SK 12345',
    make: 'Ford',
    model: 'Transit',
    productionYear: 2020,
    vin: 'VIN123456789',
    vehicleType: 'VAN',
    employeeId: 'e0000000-0000-4000-8000-000000000001',
    employeeCode: 'PR0001',
    employeeName: 'Jan Kowalski',
    status: 'ACTIVE',
    insuranceStartDate: '2025-01-01',
    insuranceEndDate: '2027-03-15',
    insurer: 'Demo Insurer',
    insurancePolicyNumber: 'POL-001',
    lastInspectionDate: '2026-01-10',
    nextInspectionDate: '2027-02-10',
    lastInspectionMileage: 120000,
    purchaseDate: '2020-06-01',
    currentMileage: 124500,
    notes: null,
    createdAt: '2025-01-01T00:00:00Z',
    updatedAt: '2025-01-01T00:00:00Z',
  },
  {
    id: 'f4000000-0000-4000-8000-000000000002',
    code: 'FL0002',
    registrationNumber: 'KR 99999',
    make: 'Toyota',
    model: 'Corolla',
    productionYear: 2019,
    vin: null,
    vehicleType: 'PASSENGER',
    employeeId: null,
    employeeCode: null,
    employeeName: null,
    status: 'IN_SERVICE',
    insuranceStartDate: null,
    insuranceEndDate: '2026-08-25',
    insurer: null,
    insurancePolicyNumber: null,
    lastInspectionDate: null,
    nextInspectionDate: '2026-08-01',
    lastInspectionMileage: null,
    purchaseDate: null,
    currentMileage: 85000,
    notes: null,
    createdAt: '2025-01-01T00:00:00Z',
    updatedAt: '2025-01-01T00:00:00Z',
  },
];

const employees = [
  {
    id: 'e0000000-0000-4000-8000-000000000001',
    code: 'PR0001',
    firstName: 'Jan',
    lastName: 'Kowalski',
    phone: null,
    email: null,
    googleEmail: null,
    position: null,
    role: 'PRACOWNIK' as const,
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

function createFleetFetchMock(options: { listStatus?: number } = {}) {
  return vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
    const url = String(input);

    if (url.includes('/api/employees') && (!init?.method || init.method === 'GET')) {
      return Response.json(employees);
    }

    if (url.includes('/api/fleet') && (!init?.method || init.method === 'GET')) {
      if (options.listStatus === 403) {
        return Response.json({ status: 403, message: 'Forbidden' }, { status: 403 });
      }

      const searchParams = new URL(url, 'http://localhost').searchParams;
      let result = vehicles;

      const status = searchParams.get('status');
      if (status) {
        result = result.filter((vehicle) => vehicle.status === status);
      }

      const employeeId = searchParams.get('employeeId');
      if (employeeId) {
        result = result.filter((vehicle) => vehicle.employeeId === employeeId);
      }

      return Response.json(result);
    }

    return new Response(null, { status: 404 });
  });
}

function FleetPageHarness() {
  const { loadPermissions } = usePermissions();

  useEffect(() => {
    void loadPermissions();
  }, [loadPermissions]);

  return (
    <Routes>
      <Route path="/fleet" element={<FleetPage />} />
    </Routes>
  );
}

function renderFleetPage(permissions: string[] = ['FLEET_VIEW']) {
  return renderWithProviders(<FleetPageHarness />, {
    routerProps: { initialEntries: ['/fleet'] },
    permissionsAdapter: {
      loadPermissions: async () => permissions,
    },
  });
}

describe('FleetPage', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', createFleetFetchMock());
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('renders page header and vehicle list', async () => {
    renderFleetPage();

    expect(await screen.findByRole('heading', { name: 'Flota' })).toBeInTheDocument();
    expect(within(screen.getByRole('table')).getByText('FL0001')).toBeInTheDocument();
    expect(within(screen.getByRole('table')).getByText('SK 12345')).toBeInTheDocument();
  });

  it('displays vehicle details and date formatting', async () => {
    renderFleetPage();

    await waitFor(() => {
      expect(within(screen.getByRole('table')).getByText('Ford Transit (2020)')).toBeInTheDocument();
    });

    expect(within(screen.getByRole('table')).getByText('15/03/2027')).toBeInTheDocument();
    expect(within(screen.getByRole('table')).getByText('10/02/2027')).toBeInTheDocument();
    expect(within(screen.getByRole('table')).getByText('124 500 km')).toBeInTheDocument();
    expect(within(screen.getByRole('table')).getByText('Jan Kowalski (PR0001)')).toBeInTheDocument();
  });

  it('filters by status', async () => {
    const user = userEvent.setup();
    renderFleetPage();

    await waitFor(() => {
      expect(within(screen.getByRole('table')).getByText('FL0001')).toBeInTheDocument();
    });

    await user.selectOptions(screen.getByLabelText('Filtr statusu'), 'IN_SERVICE');

    expect(within(screen.getByRole('table')).getByText('KR 99999')).toBeInTheDocument();
    expect(within(screen.getByRole('table')).queryByText('FL0001')).not.toBeInTheDocument();
  });

  it('handles API 403 with forbidden message', async () => {
    vi.stubGlobal('fetch', createFleetFetchMock({ listStatus: 403 }));

    renderFleetPage();

    expect(await screen.findByText('Nie masz uprawnień do tej operacji.')).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Brak dostępu' })).toBeInTheDocument();
  });
});
