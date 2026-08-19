import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { useEffect } from 'react';
import { Route, Routes } from 'react-router-dom';
import { describe, expect, it, vi, beforeEach, afterEach } from 'vitest';
import { ActivitiesPage } from '@/features/activities/pages/ActivitiesPage';
import type { Activity } from '@/features/activities/types/activity';
import { usePermissions } from '@/features/permissions/PermissionProvider';
import { renderWithProviders } from '@/test/testUtils';

const sampleActivities: Activity[] = [
  {
    id: 'f0000000-0000-4000-8000-000000000001',
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
  {
    id: 'f0000000-0000-4000-8000-000000000018',
    code: 'CZ0018',
    name: 'Odśnieżanie',
    category: 'Zimowe',
    planningType: 'ON_DEMAND',
    defaultPeriod: 'ZIMA',
    durationMinutes: 120,
    priority: 'HIGH',
    active: false,
    createdAt: '2025-01-01T00:00:00Z',
    updatedAt: '2025-01-01T00:00:00Z',
  },
];

async function waitForActivityInTable(code: string) {
  await waitFor(() => {
    expect(within(screen.getByRole('table')).getByText(code)).toBeInTheDocument();
  });
}

const allPermissions = [
  'ACTIVITIES_VIEW',
  'ACTIVITIES_CREATE',
  'ACTIVITIES_EDIT',
  'ACTIVITIES_DELETE',
];

interface MockFetchOptions {
  activities?: Activity[];
  onRequest?: (url: string, init?: RequestInit) => void;
  listStatus?: number;
}

function createActivitiesFetchMock(options: MockFetchOptions = {}) {
  let activities = [...(options.activities ?? sampleActivities)];

  return vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
    const url = String(input);
    options.onRequest?.(url, init);

    if (url.includes('/api/activities') && (!init?.method || init.method === 'GET')) {
      if (options.listStatus && options.listStatus !== 200) {
        return Response.json(
          { status: options.listStatus, message: 'Error' },
          { status: options.listStatus },
        );
      }

      return Response.json(activities);
    }

    if (url.includes('/api/activities') && init?.method === 'POST') {
      const body = JSON.parse(String(init.body)) as Partial<Activity>;
      const created: Activity = {
        id: 'f0000000-0000-4000-8000-000000000099',
        code: body.code ?? 'NEW',
        name: body.name ?? 'New Activity',
        category: body.category ?? 'Kategoria',
        planningType: body.planningType ?? 'CYCLIC',
        defaultPeriod: body.defaultPeriod ?? null,
        durationMinutes: body.durationMinutes ?? null,
        priority: body.priority ?? 'NORMAL',
        active: true,
        createdAt: '2025-01-01T00:00:00Z',
        updatedAt: '2025-01-01T00:00:00Z',
      };
      activities = [...activities, created];
      return Response.json(created, { status: 201 });
    }

    if (url.match(/\/api\/activities\/[^/]+$/) && init?.method === 'PUT') {
      const id = url.split('/').pop()!;
      const body = JSON.parse(String(init.body)) as Partial<Activity>;
      activities = activities.map((activity) =>
        activity.id === id ? { ...activity, ...body, id: activity.id } : activity,
      );
      const updated = activities.find((activity) => activity.id === id);
      return Response.json(updated);
    }

    if (url.match(/\/api\/activities\/[^/]+$/) && init?.method === 'DELETE') {
      const id = url.split('/').pop()!;
      activities = activities.map((activity) =>
        activity.id === id ? { ...activity, active: false } : activity,
      );
      return new Response(null, { status: 204 });
    }

    return new Response(null, { status: 404 });
  });
}

function ActivitiesPageHarness() {
  const { loadPermissions } = usePermissions();

  useEffect(() => {
    void loadPermissions();
  }, [loadPermissions]);

  return (
    <Routes>
      <Route path="/activities" element={<ActivitiesPage />} />
    </Routes>
  );
}

function renderActivitiesPage(permissions: string[] = allPermissions) {
  return renderWithProviders(<ActivitiesPageHarness />, {
    routerProps: { initialEntries: ['/activities'] },
    permissionsAdapter: {
      loadPermissions: async () => permissions,
    },
  });
}

describe('ActivitiesPage', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', createActivitiesFetchMock());
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('loads and displays activities list', async () => {
    renderActivitiesPage();

    expect(await screen.findByRole('heading', { name: 'Katalog czynności' })).toBeInTheDocument();
    await waitForActivityInTable('CZ0001');
    expect(within(screen.getByRole('table')).getByText('Tereny zewnętrzne')).toBeInTheDocument();
  });

  it('shows add button when user has CREATE permission', async () => {
    renderActivitiesPage(allPermissions);

    expect(await screen.findByRole('button', { name: /Dodaj czynność/i })).toBeInTheDocument();
  });

  it('hides add button when user lacks CREATE permission', async () => {
    renderActivitiesPage(['ACTIVITIES_VIEW', 'ACTIVITIES_EDIT', 'ACTIVITIES_DELETE']);

    await waitForActivityInTable('CZ0001');
    expect(screen.queryByRole('button', { name: /Dodaj czynność/i })).not.toBeInTheDocument();
  });

  it('validates create form required fields', async () => {
    const user = userEvent.setup();
    renderActivitiesPage();

    await waitForActivityInTable('CZ0001');
    await user.click(screen.getByRole('button', { name: /Dodaj czynność/i }));

    const dialog = await screen.findByRole('dialog');
    await user.click(within(dialog).getByRole('button', { name: 'Dodaj czynność' }));

    expect(await screen.findByText('Podaj kod czynności.')).toBeInTheDocument();
    expect(screen.getByText('Podaj nazwę czynności.')).toBeInTheDocument();
    expect(screen.getByText('Podaj kategorię.')).toBeInTheDocument();
  });

  it('handles API 403 with forbidden message', async () => {
    vi.stubGlobal('fetch', createActivitiesFetchMock({ listStatus: 403 }));

    renderActivitiesPage();

    expect(await screen.findByText('Nie masz uprawnień do tej operacji.')).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Brak dostępu' })).toBeInTheDocument();
  });

  it('renders empty state when no activities match criteria', async () => {
    vi.stubGlobal('fetch', createActivitiesFetchMock({ activities: [] }));

    renderActivitiesPage();

    expect(await screen.findByText('BRAK CZYNNOŚCI')).toBeInTheDocument();
  });
});
