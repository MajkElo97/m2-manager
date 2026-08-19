import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { useEffect } from 'react';
import { Route, Routes } from 'react-router-dom';
import { describe, expect, it, vi, beforeEach, afterEach } from 'vitest';
import type { Activity } from '@/features/activities/types/activity';
import type { Building } from '@/features/buildings/types/building';
import { BuildingScopesPage } from '@/features/scopes/pages/BuildingScopesPage';
import type { Scope } from '@/features/scopes/types/scope';
import { usePermissions } from '@/features/permissions/PermissionProvider';
import { renderWithProviders } from '@/test/testUtils';

const buildingId = '11111111-1111-1111-1111-111111111111';
const activityId = 'f0000000-0000-4000-8000-000000000001';

const sampleBuilding: Building = {
  id: buildingId,
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
};

const sampleActivities: Activity[] = [
  {
    id: activityId,
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

const sampleScopes: Scope[] = [
  {
    id: 'g0000000-0000-4000-8000-000000000001',
    code: 'ZP0001',
    buildingId,
    activityId,
    planningType: 'WEEKLY',
    frequency: 1,
    weekdays: 'Wtorek',
    notes: null,
    status: 'ACTIVE',
    createdAt: '2025-01-01T00:00:00Z',
    updatedAt: '2025-01-01T00:00:00Z',
  },
];

interface MockFetchOptions {
  building?: Building | null;
  scopes?: Scope[];
  buildingStatus?: number;
  listStatus?: number;
}

function createScopesFetchMock(options: MockFetchOptions = {}) {
  let scopes = [...(options.scopes ?? sampleScopes)];
  const building = options.building ?? sampleBuilding;

  return vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
    const url = String(input);

    if (url.match(/\/api\/buildings\/[^/?]+$/) && (!init?.method || init.method === 'GET')) {
      if (options.buildingStatus === 404 || building === null) {
        return Response.json({ status: 404, message: 'Not found' }, { status: 404 });
      }
      return Response.json(building);
    }

    if (url.includes('/api/activities') && (!init?.method || init.method === 'GET')) {
      return Response.json(sampleActivities);
    }

    if (url.includes('/api/scopes') && (!init?.method || init.method === 'GET')) {
      if (options.listStatus === 403) {
        return Response.json({ status: 403, message: 'Forbidden' }, { status: 403 });
      }
      return Response.json(scopes);
    }

    if (url.includes('/api/scopes') && init?.method === 'POST') {
      const body = JSON.parse(String(init.body)) as Partial<Scope>;
      const created: Scope = {
        id: 'g0000000-0000-4000-8000-000000000099',
        code: body.code ?? 'NEW',
        buildingId: body.buildingId ?? buildingId,
        activityId: body.activityId ?? activityId,
        planningType: body.planningType ?? 'WEEKLY',
        frequency: body.frequency ?? null,
        weekdays: body.weekdays ?? null,
        notes: body.notes ?? null,
        status: 'ACTIVE',
        createdAt: '2025-01-01T00:00:00Z',
        updatedAt: '2025-01-01T00:00:00Z',
      };
      scopes = [...scopes, created];
      return Response.json(created, { status: 201 });
    }

    if (url.match(/\/api\/scopes\/[^/?]+$/) && init?.method === 'PUT') {
      const id = url.split('/').pop()!;
      const body = JSON.parse(String(init.body)) as Partial<Scope>;
      scopes = scopes.map((scope) =>
        scope.id === id ? { ...scope, ...body, id: scope.id } : scope,
      );
      const updated = scopes.find((scope) => scope.id === id);
      return Response.json(updated);
    }

    if (url.match(/\/api\/scopes\/[^/?]+$/) && init?.method === 'DELETE') {
      const id = url.split('/').pop()!;
      scopes = scopes.map((scope) =>
        scope.id === id ? { ...scope, status: 'INACTIVE' } : scope,
      );
      return new Response(null, { status: 204 });
    }

    return new Response(null, { status: 404 });
  });
}

function BuildingScopesPageHarness() {
  const { loadPermissions } = usePermissions();

  useEffect(() => {
    void loadPermissions();
  }, [loadPermissions]);

  return (
    <Routes>
      <Route path="/buildings/:buildingId/scopes" element={<BuildingScopesPage />} />
    </Routes>
  );
}

function renderBuildingScopesPage(
  permissions: string[] = ['SCOPES_VIEW', 'SCOPES_CREATE', 'SCOPES_EDIT', 'SCOPES_DELETE', 'BUILDINGS_VIEW'],
) {
  return renderWithProviders(<BuildingScopesPageHarness />, {
    routerProps: { initialEntries: [`/buildings/${buildingId}/scopes`] },
    permissionsAdapter: {
      loadPermissions: async () => permissions,
    },
  });
}

async function waitForScopeInTable(code: string) {
  await waitFor(() => {
    expect(within(screen.getByRole('table')).getByText(code)).toBeInTheDocument();
  });
}

describe('BuildingScopesPage', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', createScopesFetchMock());
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('loads building context and scopes list', async () => {
    renderBuildingScopesPage();

    expect(await screen.findByRole('heading', { name: 'Zakresy' })).toBeInTheDocument();
    expect(await screen.findByText(/Kasprzaka 6/)).toBeInTheDocument();
    await waitForScopeInTable('ZP0001');
  });

  it('shows add button when user has SCOPES_CREATE permission', async () => {
    renderBuildingScopesPage(['SCOPES_VIEW', 'SCOPES_CREATE']);

    expect(await screen.findByRole('button', { name: /Dodaj zakres/i })).toBeInTheDocument();
  });

  it('validates create form required fields', async () => {
    const user = userEvent.setup();
    renderBuildingScopesPage();

    await waitForScopeInTable('ZP0001');
    await user.click(screen.getByRole('button', { name: /Dodaj zakres/i }));

    const dialog = await screen.findByRole('dialog');
    await user.click(within(dialog).getByRole('button', { name: 'Dodaj zakres' }));

    expect(await screen.findByText('Podaj kod zakresu.')).toBeInTheDocument();
    expect(screen.getByText('Wybierz czynność.')).toBeInTheDocument();
  });

  it('creates scope successfully', async () => {
    const user = userEvent.setup();
    renderBuildingScopesPage();

    await waitForScopeInTable('ZP0001');
    await user.click(screen.getByRole('button', { name: /Dodaj zakres/i }));

    const dialog = await screen.findByRole('dialog');
    await user.type(within(dialog).getByLabelText('Kod'), 'ZP0099');
    await user.selectOptions(within(dialog).getByLabelText('Czynność'), activityId);
    await user.click(within(dialog).getByRole('button', { name: 'Dodaj zakres' }));

    expect(await screen.findByText('Zakres został dodany.')).toBeInTheDocument();
  });

  it('renders empty state when no scopes exist', async () => {
    vi.stubGlobal('fetch', createScopesFetchMock({ scopes: [] }));

    renderBuildingScopesPage();

    expect(await screen.findByText('BRAK ZAKRESÓW')).toBeInTheDocument();
    expect(screen.getByText('Nie znaleziono zakresów w tym budynku.')).toBeInTheDocument();
  });

  it('navigates back to buildings list', async () => {
    renderBuildingScopesPage();

    await waitForScopeInTable('ZP0001');
    const backLink = screen.getByRole('link', { name: /Wróć do budynku/i });
    expect(backLink).toHaveAttribute('href', '/buildings');
  });
});
