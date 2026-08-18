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

const buildingId = '11111111-1111-1111-1111-111111111111';

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

const sampleStaircases: Staircase[] = [
  {
    id: 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
    buildingId,
    code: 'KL0003',
    designation: '1',
    intercomCode: '0610#',
    keyRequired: false,
    elevator: false,
    floors: 5,
    notes: null,
    createdAt: '2025-01-01T00:00:00Z',
    updatedAt: '2025-01-01T00:00:00Z',
  },
  {
    id: 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
    buildingId,
    code: 'KL0004',
    designation: '2',
    intercomCode: '2606#',
    keyRequired: false,
    elevator: false,
    floors: 5,
    notes: null,
    createdAt: '2025-01-01T00:00:00Z',
    updatedAt: '2025-01-01T00:00:00Z',
  },
];

interface MockFetchOptions {
  building?: Building | null;
  staircases?: Staircase[];
  buildingStatus?: number;
  listStatus?: number;
  createStatus?: number;
  updateStatus?: number;
  deleteStatus?: number;
}

function createStaircasesFetchMock(options: MockFetchOptions = {}) {
  let staircases = [...(options.staircases ?? sampleStaircases)];
  const building = options.building ?? sampleBuilding;

  return vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
    const url = String(input);

    if (url.match(/\/api\/buildings\/[^/?]+$/) && (!init?.method || init.method === 'GET')) {
      if (options.buildingStatus === 404) {
        return Response.json({ status: 404, message: 'Not found' }, { status: 404 });
      }
      if (building === null) {
        return Response.json({ status: 404, message: 'Not found' }, { status: 404 });
      }
      return Response.json(building);
    }

    if (url.includes('/api/staircases') && (!init?.method || init.method === 'GET')) {
      if (options.listStatus === 403) {
        return Response.json({ status: 403, message: 'Forbidden' }, { status: 403 });
      }
      if (options.listStatus === 404) {
        return Response.json({ status: 404, message: 'Not found' }, { status: 404 });
      }
      return Response.json(staircases);
    }

    if (url.includes('/api/staircases') && init?.method === 'POST') {
      if (options.createStatus && options.createStatus !== 201) {
        return Response.json({ status: options.createStatus, message: 'Error' }, { status: options.createStatus });
      }

      const body = JSON.parse(String(init.body)) as Partial<Staircase>;
      const created: Staircase = {
        id: 'cccccccc-cccc-cccc-cccc-cccccccccccc',
        buildingId: body.buildingId ?? buildingId,
        code: body.code ?? 'NEW',
        designation: body.designation ?? '1',
        intercomCode: body.intercomCode ?? null,
        keyRequired: body.keyRequired ?? false,
        elevator: body.elevator ?? false,
        floors: body.floors ?? 4,
        notes: body.notes ?? null,
        createdAt: '2025-01-01T00:00:00Z',
        updatedAt: '2025-01-01T00:00:00Z',
      };
      staircases = [...staircases, created];
      return Response.json(created, { status: 201 });
    }

    if (url.match(/\/api\/staircases\/[^/?]+$/) && init?.method === 'PUT') {
      const id = url.split('/').pop()!;
      const body = JSON.parse(String(init.body)) as Partial<Staircase>;
      staircases = staircases.map((staircase) =>
        staircase.id === id ? { ...staircase, ...body, id: staircase.id } : staircase,
      );
      const updated = staircases.find((staircase) => staircase.id === id);
      return Response.json(updated);
    }

    if (url.match(/\/api\/staircases\/[^/?]+$/) && init?.method === 'DELETE') {
      const id = url.split('/').pop()!;
      staircases = staircases.filter((staircase) => staircase.id !== id);
      return new Response(null, { status: 204 });
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
      <Route path="/buildings/:buildingId/staircases" element={<StaircasesPage />} />
    </Routes>
  );
}

function renderStaircasesPage(permissions: string[] = ['BUILDINGS_VIEW', 'BUILDINGS_EDIT']) {
  return renderWithProviders(<StaircasesPageHarness />, {
    routerProps: { initialEntries: [`/buildings/${buildingId}/staircases`] },
    permissionsAdapter: {
      loadPermissions: async () => permissions,
    },
  });
}

async function waitForStaircaseInTable(code: string) {
  await waitFor(() => {
    expect(within(screen.getByRole('table')).getByText(code)).toBeInTheDocument();
  });
}

describe('StaircasesPage', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', createStaircasesFetchMock());
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('loads building context', async () => {
    renderStaircasesPage();

    expect(await screen.findByRole('heading', { name: 'Klatki schodowe' })).toBeInTheDocument();
    expect(await screen.findByText(/Kasprzaka 6/)).toBeInTheDocument();
    expect(screen.getByText(/KASPRZAKA6/)).toBeInTheDocument();
    expect(screen.getByText(/ul\. Kasprzaka 6, Dąbrowa Górnicza/)).toBeInTheDocument();
  });

  it('loads staircases list', async () => {
    renderStaircasesPage();

    await waitForStaircaseInTable('KL0003');
    expect(within(screen.getByRole('table')).getByText('KL0004')).toBeInTheDocument();
  });

  it('shows add button when user has BUILDINGS_EDIT permission', async () => {
    renderStaircasesPage(['BUILDINGS_VIEW', 'BUILDINGS_EDIT']);

    expect(await screen.findByRole('button', { name: /Dodaj klatkę/i })).toBeInTheDocument();
  });

  it('hides add button when user lacks BUILDINGS_EDIT permission', async () => {
    renderStaircasesPage(['BUILDINGS_VIEW']);

    await waitForStaircaseInTable('KL0003');
    expect(screen.queryByRole('button', { name: /Dodaj klatkę/i })).not.toBeInTheDocument();
  });

  it('validates create form required fields', async () => {
    const user = userEvent.setup();
    renderStaircasesPage();

    await waitForStaircaseInTable('KL0003');
    await user.click(screen.getByRole('button', { name: /Dodaj klatkę/i }));

    const dialog = await screen.findByRole('dialog');
    await user.click(within(dialog).getByRole('button', { name: 'Dodaj klatkę' }));

    expect(await screen.findByText('Kod klatki jest wymagany.')).toBeInTheDocument();
    expect(screen.getByText('Oznaczenie jest wymagane.')).toBeInTheDocument();
  });

  it('creates staircase successfully', async () => {
    const user = userEvent.setup();
    renderStaircasesPage();

    await waitForStaircaseInTable('KL0003');
    await user.click(screen.getByRole('button', { name: /Dodaj klatkę/i }));

    const dialog = await screen.findByRole('dialog');
    await user.type(within(dialog).getByLabelText('Kod klatki'), 'KL0099');
    await user.type(within(dialog).getByLabelText('Oznaczenie'), '3');
    await user.click(within(dialog).getByRole('button', { name: 'Dodaj klatkę' }));

    expect(await screen.findByText('Klatka została dodana.')).toBeInTheDocument();
  });

  it('updates staircase successfully', async () => {
    const user = userEvent.setup();
    renderStaircasesPage();

    await waitForStaircaseInTable('KL0003');
    await user.click(screen.getAllByRole('button', { name: 'Edytuj' })[0]);

    const dialog = await screen.findByRole('dialog');
    const intercomInput = within(dialog).getByLabelText('Kod domofonu');
    await user.clear(intercomInput);
    await user.type(intercomInput, '9999#');
    await user.click(within(dialog).getByRole('button', { name: 'Zapisz zmiany' }));

    expect(await screen.findByText('Klatka została zaktualizowana.')).toBeInTheDocument();
  });

  it('shows delete confirmation dialog', async () => {
    const user = userEvent.setup();
    renderStaircasesPage();

    await waitForStaircaseInTable('KL0003');
    await user.click(screen.getAllByRole('button', { name: 'Usuń' })[0]);

    expect(await screen.findByText('Czy na pewno chcesz usunąć tę klatkę?')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Anuluj' })).toBeInTheDocument();
  });

  it('deletes staircase successfully', async () => {
    const user = userEvent.setup();
    renderStaircasesPage();

    await waitForStaircaseInTable('KL0003');
    await user.click(screen.getAllByRole('button', { name: 'Usuń' })[0]);

    const dialog = await screen.findByRole('dialog', { name: 'Usuwanie klatki' });
    await user.click(within(dialog).getByRole('button', { name: 'Usuń' }));

    expect(await screen.findByText('Klatka została usunięta.')).toBeInTheDocument();
  });

  it('renders empty state when no staircases exist', async () => {
    vi.stubGlobal('fetch', createStaircasesFetchMock({ staircases: [] }));

    renderStaircasesPage();

    expect(await screen.findByText('BRAK KLATEK')).toBeInTheDocument();
    expect(screen.getByText('Nie znaleziono klatek w tym budynku.')).toBeInTheDocument();
  });

  it('handles API 404 for building', async () => {
    vi.stubGlobal('fetch', createStaircasesFetchMock({ building: null, buildingStatus: 404 }));

    renderStaircasesPage();

    expect(await screen.findByText('Budynek nie znaleziony')).toBeInTheDocument();
  });

  it('handles API 403 with forbidden message', async () => {
    vi.stubGlobal('fetch', createStaircasesFetchMock({ listStatus: 403 }));

    renderStaircasesPage();

    expect(await screen.findByText('Nie masz uprawnień do tej operacji.')).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Brak dostępu' })).toBeInTheDocument();
  });

  it('navigates back to buildings list', async () => {
    renderStaircasesPage();

    await waitForStaircaseInTable('KL0003');
    const backLink = screen.getByRole('link', { name: /Wróć do budynku/i });
    expect(backLink).toHaveAttribute('href', '/buildings');
  });
});
