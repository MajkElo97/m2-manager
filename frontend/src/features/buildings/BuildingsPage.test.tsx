import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { useEffect } from 'react';
import { describe, expect, it, vi, beforeEach, afterEach } from 'vitest';
import { BuildingsPage } from '@/features/buildings/pages/BuildingsPage';
import type { Building } from '@/features/buildings/types/building';
import { usePermissions } from '@/features/permissions/PermissionProvider';
import { renderWithProviders } from '@/test/testUtils';

const sampleBuildings: Building[] = [
  {
    id: '11111111-1111-1111-1111-111111111111',
    code: 'PUSTA64',
    name: 'Pusta 64',
    address: 'ul. Pusta 64',
    city: 'Sosnowiec',
    nip: '6443561947',
    phone: null,
    email: null,
    managerCode: 'ZA0001',
    supervisorCode: 'OP0001',
    employeeCode: 'E0001',
    contractSignedAt: null,
    serviceStartDate: '2025-01-05',
    noticePeriodMonths: 3,
    status: 'ACTIVE',
    notes: null,
    createdAt: '2025-01-01T00:00:00Z',
    updatedAt: '2025-01-01T00:00:00Z',
  },
  {
    id: '22222222-2222-2222-2222-222222222222',
    code: 'PUSTA62',
    name: 'test',
    address: 'test',
    city: 'test',
    nip: null,
    phone: null,
    email: null,
    managerCode: 'ZA0001',
    supervisorCode: 'OP0001',
    employeeCode: 'E0003',
    contractSignedAt: null,
    serviceStartDate: null,
    noticePeriodMonths: 3,
    status: 'INACTIVE',
    notes: null,
    createdAt: '2025-01-01T00:00:00Z',
    updatedAt: '2025-01-01T00:00:00Z',
  },
];

async function waitForBuildingInTable(code: string) {
  await waitFor(() => {
    expect(within(screen.getByRole('table')).getByText(code)).toBeInTheDocument();
  });
}

const allPermissions = [
  'BUILDINGS_VIEW',
  'BUILDINGS_CREATE',
  'BUILDINGS_EDIT',
  'BUILDINGS_DELETE',
];

interface MockFetchOptions {
  buildings?: Building[];
  onRequest?: (url: string, init?: RequestInit) => void;
  listStatus?: number;
  createStatus?: number;
  updateStatus?: number;
  deleteStatus?: number;
}

function createBuildingsFetchMock(options: MockFetchOptions = {}) {
  let buildings = [...(options.buildings ?? sampleBuildings)];

  return vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
    const url = String(input);
    options.onRequest?.(url, init);

    if (url.includes('/api/buildings') && (!init?.method || init.method === 'GET')) {
      if (options.listStatus && options.listStatus !== 200) {
        return Response.json(
          { status: options.listStatus, message: 'Error' },
          { status: options.listStatus },
        );
      }

      const parsedUrl = new URL(url, 'http://localhost');
      const status = parsedUrl.searchParams.get('status');
      const search = parsedUrl.searchParams.get('search')?.toLowerCase();

      let filtered = buildings;
      if (status) {
        filtered = filtered.filter((building) => building.status === status);
      }
      if (search) {
        filtered = filtered.filter(
          (building) =>
            building.code.toLowerCase().includes(search) ||
            building.name.toLowerCase().includes(search) ||
            building.address.toLowerCase().includes(search) ||
            building.city.toLowerCase().includes(search),
        );
      }

      return Response.json(filtered);
    }

    if (url.includes('/api/buildings') && init?.method === 'POST') {
      if (options.createStatus && options.createStatus !== 201) {
        return Response.json(
          { status: options.createStatus, message: 'Conflict' },
          { status: options.createStatus },
        );
      }

      const body = JSON.parse(String(init.body)) as Partial<Building>;
      const created: Building = {
        id: '33333333-3333-3333-3333-333333333333',
        code: body.code ?? 'NEW',
        name: body.name ?? 'New Building',
        address: body.address ?? 'Address',
        city: body.city ?? 'City',
        nip: body.nip ?? null,
        phone: body.phone ?? null,
        email: body.email ?? null,
        managerCode: body.managerCode ?? null,
        supervisorCode: body.supervisorCode ?? null,
        employeeCode: body.employeeCode ?? null,
        contractSignedAt: body.contractSignedAt ?? null,
        serviceStartDate: body.serviceStartDate ?? null,
        noticePeriodMonths: body.noticePeriodMonths ?? 3,
        status: 'ACTIVE',
        notes: body.notes ?? null,
        createdAt: '2025-01-01T00:00:00Z',
        updatedAt: '2025-01-01T00:00:00Z',
      };
      buildings = [...buildings, created];
      return Response.json(created, { status: 201 });
    }

    if (url.match(/\/api\/buildings\/[^/]+$/) && init?.method === 'PUT') {
      if (options.updateStatus && options.updateStatus !== 200) {
        return Response.json(
          { status: options.updateStatus, message: 'Error' },
          { status: options.updateStatus },
        );
      }

      const id = url.split('/').pop()!;
      const body = JSON.parse(String(init.body)) as Partial<Building>;
      buildings = buildings.map((building) =>
        building.id === id ? { ...building, ...body, id: building.id } : building,
      );
      const updated = buildings.find((building) => building.id === id);
      return Response.json(updated);
    }

    if (url.match(/\/api\/buildings\/[^/]+$/) && init?.method === 'DELETE') {
      if (options.deleteStatus && options.deleteStatus !== 204) {
        return Response.json(
          { status: options.deleteStatus, message: 'Error' },
          { status: options.deleteStatus },
        );
      }

      const id = url.split('/').pop()!;
      buildings = buildings.map((building) =>
        building.id === id ? { ...building, status: 'INACTIVE' } : building,
      );
      return new Response(null, { status: 204 });
    }

    return new Response(null, { status: 404 });
  });
}

function BuildingsPageHarness() {
  const { loadPermissions } = usePermissions();

  useEffect(() => {
    void loadPermissions();
  }, [loadPermissions]);

  return <BuildingsPage />;
}

function renderBuildingsPage(permissions: string[] = allPermissions) {
  return renderWithProviders(<BuildingsPageHarness />, {
    permissionsAdapter: {
      loadPermissions: async () => permissions,
    },
  });
}

describe('BuildingsPage', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', createBuildingsFetchMock());
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('loads and displays buildings list', async () => {
    renderBuildingsPage();

    expect(await screen.findByRole('heading', { name: 'Budynki' })).toBeInTheDocument();
    await waitForBuildingInTable('PUSTA64');
    expect(within(screen.getByRole('table')).getByText('Pusta 64')).toBeInTheDocument();
  });

  it('filters buildings by search query', async () => {
    const user = userEvent.setup();
    const onRequest = vi.fn();
    vi.stubGlobal('fetch', createBuildingsFetchMock({ onRequest }));

    renderBuildingsPage();

    await waitForBuildingInTable('PUSTA64');
    await user.type(screen.getByLabelText('Szukaj'), 'Kasprzaka');

    await waitFor(() => {
      expect(onRequest).toHaveBeenCalledWith(
        expect.stringContaining('search=Kasprzaka'),
        expect.any(Object),
      );
    });
  });

  it('filters buildings by status', async () => {
    const user = userEvent.setup();
    const onRequest = vi.fn();
    vi.stubGlobal('fetch', createBuildingsFetchMock({ onRequest }));

    renderBuildingsPage();

    await waitForBuildingInTable('PUSTA64');
    await user.selectOptions(screen.getByLabelText('Filtr statusu'), 'INACTIVE');

    await waitFor(() => {
      expect(onRequest).toHaveBeenCalledWith(
        expect.stringContaining('status=INACTIVE'),
        expect.any(Object),
      );
    });
  });

  it('shows add button when user has CREATE permission', async () => {
    renderBuildingsPage(allPermissions);

    expect(await screen.findByRole('button', { name: /Dodaj budynek/i })).toBeInTheDocument();
  });

  it('hides add button when user lacks CREATE permission', async () => {
    renderBuildingsPage(['BUILDINGS_VIEW', 'BUILDINGS_EDIT', 'BUILDINGS_DELETE']);

    await waitForBuildingInTable('PUSTA64');
    expect(screen.queryByRole('button', { name: /Dodaj budynek/i })).not.toBeInTheDocument();
  });

  it('shows edit action when user has EDIT permission', async () => {
    renderBuildingsPage(['BUILDINGS_VIEW', 'BUILDINGS_EDIT']);

    expect(await screen.findByRole('button', { name: 'Edytuj' })).toBeInTheDocument();
  });

  it('shows deactivate action when user has DELETE permission', async () => {
    renderBuildingsPage(['BUILDINGS_VIEW', 'BUILDINGS_DELETE']);

    expect(await screen.findByRole('button', { name: 'Dezaktywuj' })).toBeInTheDocument();
  });

  it('validates create form required fields', async () => {
    const user = userEvent.setup();
    renderBuildingsPage();

    await waitForBuildingInTable('PUSTA64');
    await user.click(screen.getByRole('button', { name: /Dodaj budynek/i }));

    const dialog = await screen.findByRole('dialog');
    await user.click(within(dialog).getByRole('button', { name: 'Dodaj budynek' }));

    expect(await screen.findByText('Podaj kod budynku.')).toBeInTheDocument();
    expect(screen.getByText('Podaj nazwę budynku.')).toBeInTheDocument();
    expect(screen.getByText('Podaj adres.')).toBeInTheDocument();
    expect(screen.getByText('Podaj miasto.')).toBeInTheDocument();
  });

  it('creates building successfully', async () => {
    const user = userEvent.setup();
    renderBuildingsPage();

    await waitForBuildingInTable('PUSTA64');
    await user.click(screen.getByRole('button', { name: /Dodaj budynek/i }));

    const dialog = await screen.findByRole('dialog');
    await user.type(within(dialog).getByLabelText('Kod'), 'NEWCODE');
    await user.type(within(dialog).getByLabelText('Nazwa'), 'Nowy budynek');
    await user.type(within(dialog).getByLabelText('Adres'), 'ul. Testowa 1');
    await user.type(within(dialog).getByLabelText('Miasto'), 'Katowice');
    await user.click(within(dialog).getByRole('button', { name: 'Dodaj budynek' }));

    expect(await screen.findByText('Budynek został dodany.')).toBeInTheDocument();
  });

  it('updates building successfully', async () => {
    const user = userEvent.setup();
    renderBuildingsPage();

    await waitForBuildingInTable('PUSTA64');
    await user.click(screen.getByRole('button', { name: 'Edytuj' }));

    const dialog = await screen.findByRole('dialog');
    const nameInput = within(dialog).getByLabelText('Nazwa');
    await user.clear(nameInput);
    await user.type(nameInput, 'Pusta 64 — zaktualizowana');
    await user.click(within(dialog).getByRole('button', { name: 'Zapisz zmiany' }));

    expect(await screen.findByText('Budynek został zaktualizowany.')).toBeInTheDocument();
  });

  it('shows deactivate confirmation dialog', async () => {
    const user = userEvent.setup();
    renderBuildingsPage();

    await waitForBuildingInTable('PUSTA64');
    await user.click(screen.getByRole('button', { name: 'Dezaktywuj' }));

    expect(
      await screen.findByText('Czy na pewno chcesz dezaktywować budynek?'),
    ).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Anuluj' })).toBeInTheDocument();
  });

  it('deactivates building and refreshes list', async () => {
    const user = userEvent.setup();
    renderBuildingsPage();

    await waitForBuildingInTable('PUSTA64');
    await user.click(screen.getByRole('button', { name: 'Dezaktywuj' }));

    const dialog = await screen.findByRole('dialog', { name: 'Dezaktywacja budynku' });
    await user.click(within(dialog).getByRole('button', { name: 'Dezaktywuj' }));

    expect(await screen.findByText('Budynek został dezaktywowany.')).toBeInTheDocument();
  });

  it('handles API 401 with session expired message', async () => {
    vi.stubGlobal(
      'fetch',
      createBuildingsFetchMock({ listStatus: 401 }),
    );

    renderBuildingsPage();

    expect(await screen.findByText('Sesja wygasła. Zaloguj się ponownie.')).toBeInTheDocument();
  });

  it('handles API 403 with forbidden message', async () => {
    vi.stubGlobal(
      'fetch',
      createBuildingsFetchMock({ listStatus: 403 }),
    );

    renderBuildingsPage();

    expect(await screen.findByText('Nie masz uprawnień do tej operacji.')).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Brak dostępu' })).toBeInTheDocument();
  });

  it('renders empty state when no buildings match criteria', async () => {
    vi.stubGlobal('fetch', createBuildingsFetchMock({ buildings: [] }));

    renderBuildingsPage();

    expect(await screen.findByText('BRAK BUDYNKÓW')).toBeInTheDocument();
    expect(
      screen.getByText('Nie znaleziono budynków spełniających kryteria.'),
    ).toBeInTheDocument();
  });

  it('renders error state on server failure', async () => {
    vi.stubGlobal(
      'fetch',
      createBuildingsFetchMock({ listStatus: 500 }),
    );

    renderBuildingsPage();

    expect(
      await screen.findByText('Nie udało się wykonać operacji. Spróbuj ponownie.'),
    ).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Spróbuj ponownie' })).toBeInTheDocument();
  });
});
