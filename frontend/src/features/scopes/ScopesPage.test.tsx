import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { useEffect } from 'react';
import { Route, Routes } from 'react-router-dom';
import { describe, expect, it, vi, beforeEach, afterEach } from 'vitest';
import type { Activity } from '@/features/activities/types/activity';
import type { Building } from '@/features/buildings/types/building';
import { ScopesPage } from '@/features/scopes/pages/ScopesPage';
import type { Scope } from '@/features/scopes/types/scope';
import { usePermissions } from '@/features/permissions/PermissionProvider';
import { renderWithProviders } from '@/test/testUtils';

const buildingAId = '11111111-1111-1111-1111-111111111111';
const buildingBId = '22222222-2222-2222-2222-222222222222';
const activityAId = 'f0000000-0000-4000-8000-000000000001';

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

const activities: Activity[] = [
  {
    id: activityAId,
    code: 'CZ0001',
    name: 'Tereny zewnętrzne',
    category: 'Sprzątanie',
    planningType: 'CYCLIC',
    defaultPeriod: null,
    durationMinutes: 30,
    priority: 'NORMAL',
    active: true,
    createdAt: '2025-01-01T00:00:00Z',
    updatedAt: '2025-01-01T00:00:00Z',
  },
];

const scopes: Scope[] = [
  {
    id: 'g0000000-0000-4000-8000-000000000001',
    code: 'ZP0001',
    buildingId: buildingAId,
    activityId: activityAId,
    planningType: 'WEEKLY',
    frequency: 1,
    weekdays: 'Wtorek',
    notes: null,
    status: 'ACTIVE',
    createdAt: '2025-01-01T00:00:00Z',
    updatedAt: '2025-01-01T00:00:00Z',
  },
  {
    id: 'g0000000-0000-4000-8000-000000000008',
    code: 'ZP0008',
    buildingId: buildingBId,
    activityId: activityAId,
    planningType: 'WEEKLY',
    frequency: 1,
    weekdays: 'Czwartek',
    notes: null,
    status: 'ACTIVE',
    createdAt: '2025-01-01T00:00:00Z',
    updatedAt: '2025-01-01T00:00:00Z',
  },
];

function createGlobalScopesFetchMock(options: { listStatus?: number } = {}) {
  return vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
    const url = String(input);

    if (url.includes('/api/buildings') && (!init?.method || init.method === 'GET')) {
      return Response.json(buildings);
    }

    if (url.includes('/api/activities') && (!init?.method || init.method === 'GET')) {
      return Response.json(activities);
    }

    if (url.includes('/api/scopes') && (!init?.method || init.method === 'GET')) {
      if (options.listStatus === 403) {
        return Response.json({ status: 403, message: 'Forbidden' }, { status: 403 });
      }
      return Response.json(scopes);
    }

    return new Response(null, { status: 404 });
  });
}

function ScopesPageHarness() {
  const { loadPermissions } = usePermissions();

  useEffect(() => {
    void loadPermissions();
  }, [loadPermissions]);

  return (
    <Routes>
      <Route path="/scopes" element={<ScopesPage />} />
      <Route path="/buildings/:buildingId/scopes" element={<div>Building scopes</div>} />
    </Routes>
  );
}

function renderGlobalScopesPage(permissions: string[] = ['SCOPES_VIEW', 'BUILDINGS_VIEW', 'ACTIVITIES_VIEW']) {
  return renderWithProviders(<ScopesPageHarness />, {
    routerProps: { initialEntries: ['/scopes'] },
    permissionsAdapter: {
      loadPermissions: async () => permissions,
    },
  });
}

describe('ScopesPage', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', createGlobalScopesFetchMock());
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('renders page header and organization subtitle', async () => {
    renderGlobalScopesPage();

    expect(await screen.findByRole('heading', { name: 'Zakresy' })).toBeInTheDocument();
    expect(screen.getByText('Zarządzanie zakresami czynności w organizacji.')).toBeInTheDocument();
  });

  it('loads all scopes with building and activity columns', async () => {
    renderGlobalScopesPage();

    await waitFor(() => {
      expect(within(screen.getByRole('table')).getByText('ZP0001')).toBeInTheDocument();
    });

    expect(within(screen.getByRole('table')).getByText('ZP0008')).toBeInTheDocument();
    expect(within(screen.getByRole('table')).getByText(/Pusta 64 · PUSTA64/)).toBeInTheDocument();
    expect(within(screen.getByRole('table')).getAllByText(/Tereny zewnętrzne \(CZ0001\)/)).toHaveLength(2);
  });

  it('filters by building', async () => {
    const user = userEvent.setup();
    renderGlobalScopesPage();

    await waitFor(() => {
      expect(within(screen.getByRole('table')).getByText('ZP0001')).toBeInTheDocument();
    });

    await user.selectOptions(screen.getByLabelText('Filtr budynku'), buildingAId);

    expect(within(screen.getByRole('table')).getByText('ZP0001')).toBeInTheDocument();
    expect(within(screen.getByRole('table')).queryByText('ZP0008')).not.toBeInTheDocument();
  });

  it('navigates to building scopes when building is clicked', async () => {
    const user = userEvent.setup();
    renderGlobalScopesPage();

    await waitFor(() => {
      expect(within(screen.getByRole('table')).getByText('ZP0001')).toBeInTheDocument();
    });

    await user.click(screen.getByRole('button', { name: /Pusta 64 · PUSTA64/i }));

    expect(await screen.findByText('Building scopes')).toBeInTheDocument();
  });

  it('handles API 403 with forbidden message', async () => {
    vi.stubGlobal('fetch', createGlobalScopesFetchMock({ listStatus: 403 }));

    renderGlobalScopesPage();

    expect(await screen.findByText('Nie masz uprawnień do tej operacji.')).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Brak dostępu' })).toBeInTheDocument();
  });
});
