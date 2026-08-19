import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { useEffect } from 'react';
import { Route, Routes } from 'react-router-dom';
import { describe, expect, it, vi, beforeEach, afterEach } from 'vitest';
import { StaircasesPage } from '@/features/staircases/pages/StaircasesPage';
import type { Building } from '@/features/buildings/types/building';
import type { Staircase } from '@/features/staircases/types/staircase';
import { usePermissions } from '@/features/permissions/PermissionProvider';
import { renderWithProviders } from '@/test/testUtils';

const buildingAId = '11111111-1111-1111-1111-111111111111';
const buildingBId = '22222222-2222-2222-2222-222222222222';

const buildings: Building[] = [
  {
    id: buildingAId,
    code: 'PUSTA64',
    name: 'Pusta 64',
    address: 'ul. Pusta 64',
    city: 'Warszawa',
    nip: null,
    phone: null,
    email: null,
    managerCode: null,
    supervisorCode: null,
    employeeCode: null,
    contractSignedAt: null,
    serviceStartDate: null,
    noticePeriodMonths: 3,
    status: 'ACTIVE',
    notes: null,
    createdAt: '2025-01-01T00:00:00Z',
    updatedAt: '2025-01-01T00:00:00Z',
  },
  {
    id: buildingBId,
    code: 'KASPRZAKA6',
    name: 'Kasprzaka 6',
    address: 'ul. Kasprzaka 6',
    city: 'Dąbrowa Górnicza',
    nip: null,
    phone: null,
    email: null,
    managerCode: null,
    supervisorCode: null,
    employeeCode: null,
    contractSignedAt: null,
    serviceStartDate: null,
    noticePeriodMonths: 3,
    status: 'ACTIVE',
    notes: null,
    createdAt: '2025-01-01T00:00:00Z',
    updatedAt: '2025-01-01T00:00:00Z',
  },
];

const staircases: Staircase[] = [
  {
    id: 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    buildingId: buildingAId,
    code: 'KL0001',
    designation: '1',
    intercomCode: '#2258',
    keyRequired: true,
    elevator: false,
    floors: 4,
    notes: null,
    createdAt: '2025-01-01T00:00:00Z',
    updatedAt: '2025-01-01T00:00:00Z',
  },
  {
    id: 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    buildingId: buildingBId,
    code: 'KL0003',
    designation: '1',
    intercomCode: '0610#',
    keyRequired: false,
    elevator: true,
    floors: 5,
    notes: null,
    createdAt: '2025-01-01T00:00:00Z',
    updatedAt: '2025-01-01T00:00:00Z',
  },
];

function createGlobalStaircasesFetchMock(options: { listStatus?: number } = {}) {
  return vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
    const url = String(input);

    if (url.includes('/api/buildings') && (!init?.method || init.method === 'GET')) {
      return Response.json(buildings);
    }

    if (url.endsWith('/api/staircases') && (!init?.method || init.method === 'GET')) {
      if (options.listStatus === 403) {
        return Response.json({ status: 403, message: 'Forbidden' }, { status: 403 });
      }
      return Response.json(staircases);
    }

    return new Response(null, { status: 404 });
  });
}

function StaircasesPageHarness() {
  const { loadPermissions } = usePermissions();

  useEffect(() => {
    void loadPermissions();
  }, [loadPermissions]);

  return (
    <Routes>
      <Route path="/staircases" element={<StaircasesPage />} />
      <Route path="/buildings/:buildingId/staircases" element={<div>Building staircases</div>} />
    </Routes>
  );
}

function renderGlobalStaircasesPage(permissions: string[] = ['STAIRCASES_VIEW', 'BUILDINGS_VIEW']) {
  return renderWithProviders(<StaircasesPageHarness />, {
    routerProps: { initialEntries: ['/staircases'] },
    permissionsAdapter: {
      loadPermissions: async () => permissions,
    },
  });
}

describe('StaircasesPage', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', createGlobalStaircasesFetchMock());
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('renders page header and organization subtitle', async () => {
    renderGlobalStaircasesPage();

    expect(await screen.findByRole('heading', { name: 'Klatki schodowe' })).toBeInTheDocument();
    expect(screen.getByText('Zarządzanie klatkami schodowymi w organizacji.')).toBeInTheDocument();
  });

  it('loads all staircases with building column', async () => {
    renderGlobalStaircasesPage();

    await waitFor(() => {
      expect(within(screen.getByRole('table')).getByText('KL0001')).toBeInTheDocument();
    });

    expect(within(screen.getByRole('table')).getByText('KL0003')).toBeInTheDocument();
    expect(within(screen.getByRole('table')).getByText(/Pusta 64 · PUSTA64/)).toBeInTheDocument();
    expect(within(screen.getByRole('table')).getByText(/Kasprzaka 6 · KASPRZAKA6/)).toBeInTheDocument();
  });

  it('does not show add staircase button', async () => {
    renderGlobalStaircasesPage(['STAIRCASES_VIEW', 'BUILDINGS_EDIT']);

    await waitFor(() => {
      expect(within(screen.getByRole('table')).getByText('KL0001')).toBeInTheDocument();
    });

    expect(screen.queryByRole('button', { name: /Dodaj klatkę/i })).not.toBeInTheDocument();
  });

  it('filters by building', async () => {
    const user = userEvent.setup();
    renderGlobalStaircasesPage();

    await waitFor(() => {
      expect(within(screen.getByRole('table')).getByText('KL0001')).toBeInTheDocument();
    });

    await user.selectOptions(screen.getByLabelText('Filtr budynku'), buildingAId);

    expect(within(screen.getByRole('table')).getByText('KL0001')).toBeInTheDocument();
    expect(within(screen.getByRole('table')).queryByText('KL0003')).not.toBeInTheDocument();
  });

  it('filters by search query across building code', async () => {
    const user = userEvent.setup();
    renderGlobalStaircasesPage();

    await waitFor(() => {
      expect(within(screen.getByRole('table')).getByText('KL0001')).toBeInTheDocument();
    });

    await user.type(screen.getByLabelText('Szukaj'), 'PUSTA64');

    expect(within(screen.getByRole('table')).getByText('KL0001')).toBeInTheDocument();
    expect(within(screen.getByRole('table')).queryByText('KL0003')).not.toBeInTheDocument();
  });

  it('filters by elevator and key required', async () => {
    const user = userEvent.setup();
    renderGlobalStaircasesPage();

    await waitFor(() => {
      expect(within(screen.getByRole('table')).getByText('KL0001')).toBeInTheDocument();
    });

    await user.selectOptions(screen.getByLabelText('Filtr windy'), 'YES');

    expect(within(screen.getByRole('table')).queryByText('KL0001')).not.toBeInTheDocument();
    expect(within(screen.getByRole('table')).getByText('KL0003')).toBeInTheDocument();
  });

  it('navigates to building staircases when building is clicked', async () => {
    const user = userEvent.setup();
    renderGlobalStaircasesPage();

    await waitFor(() => {
      expect(within(screen.getByRole('table')).getByText('KL0001')).toBeInTheDocument();
    });

    await user.click(screen.getByRole('button', { name: /Pusta 64 · PUSTA64/i }));

    expect(await screen.findByText('Building staircases')).toBeInTheDocument();
  });

  it('shows empty state when no matches', async () => {
    const user = userEvent.setup();
    renderGlobalStaircasesPage();

    await waitFor(() => {
      expect(within(screen.getByRole('table')).getByText('KL0001')).toBeInTheDocument();
    });

    await user.type(screen.getByLabelText('Szukaj'), 'brak-wynikow');

    expect(await screen.findByText('BRAK KLATEK SCHODOWYCH')).toBeInTheDocument();
  });

  it('handles API 403 with forbidden message', async () => {
    vi.stubGlobal('fetch', createGlobalStaircasesFetchMock({ listStatus: 403 }));

    renderGlobalStaircasesPage();

    expect(await screen.findByText('Nie masz uprawnień do tej operacji.')).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Brak dostępu' })).toBeInTheDocument();
  });
});
