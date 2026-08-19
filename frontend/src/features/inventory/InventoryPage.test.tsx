import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { useEffect } from 'react';
import { Route, Routes } from 'react-router-dom';
import { describe, expect, it, vi, beforeEach, afterEach } from 'vitest';
import { InventoryPage } from '@/features/inventory/pages/InventoryPage';
import type { Chemical } from '@/features/inventory/types/chemical';
import type { Equipment } from '@/features/inventory/types/equipment';
import { usePermissions } from '@/features/permissions/PermissionProvider';
import { renderWithProviders } from '@/test/testUtils';

const equipment: Equipment[] = [
  {
    id: 'e1000000-0000-4000-8000-000000000001',
    code: 'SP0001',
    name: 'Odkurzacz przemysłowy',
    category: 'Sprzątanie',
    manufacturer: 'Karcher',
    model: 'NT 30/1',
    serialNumber: 'SN-001',
    quantity: 2,
    conditionStatus: 'GOOD',
    location: 'Magazyn główny',
    employeeId: 'e0000000-0000-4000-8000-000000000001',
    employeeCode: 'PR0001',
    employeeName: 'Jan Kowalski',
    purchaseDate: '2024-03-15',
    purchaseValue: 2500,
    active: true,
    notes: null,
    createdAt: '2025-01-01T00:00:00Z',
    updatedAt: '2025-01-01T00:00:00Z',
  },
];

const chemicals: Chemical[] = [
  {
    id: 'c1000000-0000-4000-8000-000000000001',
    code: 'CH0001',
    name: 'Płyn do mycia podłóg',
    category: 'Chemia',
    quantity: 3,
    unit: 'LITER',
    minimumStock: 10,
    lowStock: true,
    location: 'Magazyn główny',
    active: true,
    notes: null,
    createdAt: '2025-01-01T00:00:00Z',
    updatedAt: '2025-01-01T00:00:00Z',
  },
  {
    id: 'c1000000-0000-4000-8000-000000000002',
    code: 'CH0002',
    name: 'Worki na śmieci',
    category: 'Materiały',
    quantity: 100,
    unit: 'PIECE',
    minimumStock: 20,
    lowStock: false,
    location: 'Magazyn główny',
    active: true,
    notes: null,
    createdAt: '2025-01-01T00:00:00Z',
    updatedAt: '2025-01-01T00:00:00Z',
  },
];

function createInventoryFetchMock(options: { equipmentStatus?: number } = {}) {
  return vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
    const url = String(input);

    if (url.includes('/api/inventory/equipment') && (!init?.method || init.method === 'GET')) {
      if (options.equipmentStatus === 403) {
        return Response.json({ status: 403, message: 'Forbidden' }, { status: 403 });
      }
      return Response.json(equipment);
    }

    if (url.includes('/api/inventory/chemicals') && (!init?.method || init.method === 'GET')) {
      return Response.json(chemicals);
    }

    if (url.includes('/api/employees') && (!init?.method || init.method === 'GET')) {
      return Response.json([
        {
          id: 'e0000000-0000-4000-8000-000000000001',
          code: 'PR0001',
          firstName: 'Jan',
          lastName: 'Kowalski',
          phone: null,
          email: null,
          googleEmail: null,
          position: 'Sprzątacz',
          role: 'PRACOWNIK',
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
      ]);
    }

    return new Response(null, { status: 404 });
  });
}

function InventoryPageHarness() {
  const { loadPermissions } = usePermissions();

  useEffect(() => {
    void loadPermissions();
  }, [loadPermissions]);

  return (
    <Routes>
      <Route path="/warehouse" element={<InventoryPage />} />
    </Routes>
  );
}

function renderInventoryPage(permissions: string[] = ['WAREHOUSE_VIEW']) {
  return renderWithProviders(<InventoryPageHarness />, {
    routerProps: { initialEntries: ['/warehouse'] },
    permissionsAdapter: {
      loadPermissions: async () => permissions,
    },
  });
}

describe('InventoryPage', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', createInventoryFetchMock());
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('renders page header and equipment list by default', async () => {
    renderInventoryPage();

    expect(await screen.findByRole('heading', { name: 'Magazyn' })).toBeInTheDocument();
    expect(screen.getByText('Zarządzanie sprzętem i chemią.')).toBeInTheDocument();
    expect(within(screen.getByRole('table')).getByText('Odkurzacz przemysłowy')).toBeInTheDocument();
  });

  it('displays purchase date formatting on equipment tab', async () => {
    renderInventoryPage();

    await waitFor(() => {
      expect(within(screen.getByRole('table')).getByText('Odkurzacz przemysłowy')).toBeInTheDocument();
    });

    expect(within(screen.getByRole('table')).getByText('15/03/2024')).toBeInTheDocument();
    expect(within(screen.getByRole('table')).getByText('Jan Kowalski')).toBeInTheDocument();
  });

  it('switches to chemicals tab and shows low stock warning', async () => {
    const user = userEvent.setup();
    renderInventoryPage();

    await waitFor(() => {
      expect(within(screen.getByRole('table')).getByText('Odkurzacz przemysłowy')).toBeInTheDocument();
    });

    await user.click(screen.getByRole('tab', { name: 'Chemia' }));

    await waitFor(() => {
      expect(within(screen.getByRole('table')).getByText('Płyn do mycia podłóg')).toBeInTheDocument();
    });

    expect(within(screen.getByRole('table')).getByText('NISKI STAN')).toBeInTheDocument();
    expect(within(screen.getByRole('table')).getByText('l')).toBeInTheDocument();
    expect(within(screen.getByRole('table')).getByText('szt.')).toBeInTheDocument();
  });

  it('handles API 403 with forbidden message', async () => {
    vi.stubGlobal('fetch', createInventoryFetchMock({ equipmentStatus: 403 }));

    renderInventoryPage();

    expect(await screen.findByText('Nie masz uprawnień do tej operacji.')).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Brak dostępu' })).toBeInTheDocument();
  });
});
