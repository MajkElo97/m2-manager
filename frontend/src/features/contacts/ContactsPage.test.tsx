import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { useEffect } from 'react';
import { Route, Routes } from 'react-router-dom';
import { describe, expect, it, vi, beforeEach, afterEach } from 'vitest';
import type { Building } from '@/features/buildings/types/building';
import { ContactsPage } from '@/features/contacts/pages/ContactsPage';
import type { Contact } from '@/features/contacts/types/contact';
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

const contacts: Contact[] = [
  {
    id: 'c0000000-0000-4000-8000-000000000001',
    buildingId: buildingAId,
    buildingCode: 'PUSTA64',
    buildingName: 'Pusta 64',
    firstName: 'Marek',
    lastName: 'Wiśniewski',
    functionTitle: 'Administrator',
    phone: '600700800',
    email: 'marek@example.com',
    notes: null,
    active: true,
    createdAt: '2025-01-01T00:00:00Z',
    updatedAt: '2025-01-01T00:00:00Z',
  },
  {
    id: 'c0000000-0000-4000-8000-000000000002',
    buildingId: buildingBId,
    buildingCode: 'KASPRZAKA6',
    buildingName: 'Kasprzaka 6',
    firstName: 'Ewa',
    lastName: 'Lewandowska',
    functionTitle: 'Właściciel',
    phone: null,
    email: 'ewa@example.com',
    notes: 'Kontakt główny',
    active: true,
    createdAt: '2025-01-01T00:00:00Z',
    updatedAt: '2025-01-01T00:00:00Z',
  },
];

function createContactsFetchMock(options: { listStatus?: number } = {}) {
  return vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
    const url = String(input);

    if (url.includes('/api/buildings') && (!init?.method || init.method === 'GET')) {
      return Response.json(buildings);
    }

    if (url.includes('/api/contacts') && (!init?.method || init.method === 'GET')) {
      if (options.listStatus === 403) {
        return Response.json({ status: 403, message: 'Forbidden' }, { status: 403 });
      }
      return Response.json(contacts);
    }

    return new Response(null, { status: 404 });
  });
}

function ContactsPageHarness() {
  const { loadPermissions } = usePermissions();

  useEffect(() => {
    void loadPermissions();
  }, [loadPermissions]);

  return (
    <Routes>
      <Route path="/contacts" element={<ContactsPage />} />
      <Route path="/buildings/:buildingId/contacts" element={<div>Building contacts</div>} />
    </Routes>
  );
}

function renderContactsPage(permissions: string[] = ['CONTACTS_VIEW', 'BUILDINGS_VIEW']) {
  return renderWithProviders(<ContactsPageHarness />, {
    routerProps: { initialEntries: ['/contacts'] },
    permissionsAdapter: {
      loadPermissions: async () => permissions,
    },
  });
}

describe('ContactsPage', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', createContactsFetchMock());
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('renders page header and contacts with building column', async () => {
    renderContactsPage();

    expect(await screen.findByRole('heading', { name: 'Kontakty' })).toBeInTheDocument();
    expect(within(screen.getByRole('table')).getByText('Marek Wiśniewski')).toBeInTheDocument();
    expect(within(screen.getByRole('table')).getByText(/Pusta 64 · PUSTA64/)).toBeInTheDocument();
  });

  it('filters by building', async () => {
    const user = userEvent.setup();
    renderContactsPage();

    await waitFor(() => {
      expect(within(screen.getByRole('table')).getByText('Marek Wiśniewski')).toBeInTheDocument();
    });

    await user.selectOptions(screen.getByLabelText('Filtr budynku'), buildingAId);

    expect(within(screen.getByRole('table')).getByText('Marek Wiśniewski')).toBeInTheDocument();
    expect(within(screen.getByRole('table')).queryByText('Ewa Lewandowska')).not.toBeInTheDocument();
  });

  it('filters by function', async () => {
    const user = userEvent.setup();
    renderContactsPage();

    await waitFor(() => {
      expect(within(screen.getByRole('table')).getByText('Marek Wiśniewski')).toBeInTheDocument();
    });

    await user.type(screen.getByLabelText('Funkcja'), 'Właściciel');

    expect(within(screen.getByRole('table')).getByText('Ewa Lewandowska')).toBeInTheDocument();
    expect(within(screen.getByRole('table')).queryByText('Marek Wiśniewski')).not.toBeInTheDocument();
  });

  it('navigates to building contacts when building is clicked', async () => {
    const user = userEvent.setup();
    renderContactsPage();

    await waitFor(() => {
      expect(within(screen.getByRole('table')).getByText('Marek Wiśniewski')).toBeInTheDocument();
    });

    await user.click(screen.getByRole('button', { name: /Pusta 64 · PUSTA64/i }));

    expect(await screen.findByText('Building contacts')).toBeInTheDocument();
  });

  it('handles API 403 with forbidden message', async () => {
    vi.stubGlobal('fetch', createContactsFetchMock({ listStatus: 403 }));

    renderContactsPage();

    expect(await screen.findByText('Nie masz uprawnień do tej operacji.')).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Brak dostępu' })).toBeInTheDocument();
  });
});
