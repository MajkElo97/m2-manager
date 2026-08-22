import { render, screen, within } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import { OrganizationsPage } from '@/features/organizations/pages/OrganizationsPage';
import { TestAuthProvider } from '@/test/testAuthProvider';
import { ThemeProvider } from '@/hooks/ThemeProvider';

function renderOrganizationsPage(authOverrides?: Parameters<typeof TestAuthProvider>[0]['value']) {
  return render(
    <ThemeProvider>
      <TestAuthProvider value={authOverrides}>
        <MemoryRouter>
          <OrganizationsPage />
        </MemoryRouter>
      </TestAuthProvider>
    </ThemeProvider>,
  );
}

describe('OrganizationsPage', () => {
  it('redirects non super admin users to dashboard', () => {
    renderOrganizationsPage({
      context: {
        user: { id: 'user-1', name: 'Admin', email: 'admin@test.local' },
        activeOrganization: { id: 'org-1', name: 'Org', slug: 'org' },
        availableOrganizations: [{ id: 'org-1', name: 'Org', slug: 'org' }],
        canSwitchOrganizations: false,
        mustChangePassword: false,
        superAdmin: false,
      },
    });

    expect(screen.queryByText('Organizacje')).not.toBeInTheDocument();
  });

  it('shows organizations list for super admin', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(async (input: RequestInfo | URL) => {
        const url = String(input);
        if (url.includes('/api/organizations')) {
          return Response.json([
            {
              id: 'a0000000-0000-4000-8000-000000000001',
              name: 'M2 Manager Dev',
              slug: 'm2-manager-dev',
              adminName: 'Michał Ociepka',
              adminEmail: 'multiadmin@m2manager.local',
              active: true,
              createdAt: '2026-01-01T10:00:00Z',
            },
          ]);
        }
        return new Response(null, { status: 404 });
      }),
    );

    renderOrganizationsPage({
      context: {
        user: { id: 'user-1', name: 'Super Admin', email: 'admin@m2manager.local' },
        activeOrganization: null,
        availableOrganizations: [{ id: 'org-dev', name: 'M2 Manager Dev', slug: 'm2-manager-dev' }],
        canSwitchOrganizations: true,
        mustChangePassword: false,
        superAdmin: true,
      },
    });

    expect(await screen.findByText('Organizacje')).toBeInTheDocument();
    const table = screen.getByRole('table');
    expect(table).toHaveTextContent('M2 Manager Dev');
    expect(within(table).getByRole('button', { name: 'Edytuj' })).toBeInTheDocument();

    vi.unstubAllGlobals();
  });

  it('hides edit actions for non super admin via redirect', () => {
    renderOrganizationsPage({
      context: {
        user: { id: 'user-1', name: 'Admin', email: 'admin@test.local' },
        activeOrganization: { id: 'org-1', name: 'Org', slug: 'org' },
        availableOrganizations: [{ id: 'org-1', name: 'Org', slug: 'org' }],
        canSwitchOrganizations: false,
        mustChangePassword: false,
        superAdmin: false,
      },
    });

    expect(screen.queryByRole('button', { name: 'Dodaj organizację' })).not.toBeInTheDocument();
  });

  it('shows active business context bar when super admin switched to business organization', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(async (input: RequestInfo | URL) => {
        const url = String(input);
        if (url.includes('/api/organizations')) {
          return Response.json([]);
        }
        return new Response(null, { status: 404 });
      }),
    );

    renderOrganizationsPage({
      context: {
        user: { id: 'user-1', name: 'Super Admin', email: 'admin@m2manager.local' },
        activeOrganization: { id: 'org-dev', name: 'M2 Manager Dev', slug: 'm2-manager-dev' },
        availableOrganizations: [{ id: 'org-dev', name: 'M2 Manager Dev', slug: 'm2-manager-dev' }],
        canSwitchOrganizations: true,
        mustChangePassword: false,
        superAdmin: true,
      },
    });

    expect(await screen.findByText(/Aktywny kontekst:/)).toBeInTheDocument();
    expect(screen.getByText('M2 Manager Dev')).toBeInTheDocument();

    vi.unstubAllGlobals();
  });

  it('shows no active context message when super admin has no organization selected', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(async (input: RequestInfo | URL) => {
        const url = String(input);
        if (url.includes('/api/organizations')) {
          return Response.json([]);
        }
        return new Response(null, { status: 404 });
      }),
    );

    renderOrganizationsPage({
      context: {
        user: { id: 'user-1', name: 'Super Admin', email: 'admin@m2manager.local' },
        activeOrganization: null,
        availableOrganizations: [{ id: 'org-dev', name: 'M2 Manager Dev', slug: 'm2-manager-dev' }],
        canSwitchOrganizations: true,
        mustChangePassword: false,
        superAdmin: true,
      },
    });

    expect(await screen.findByText('Brak aktywnego kontekstu organizacji')).toBeInTheDocument();

    vi.unstubAllGlobals();
  });

  it('renders desktop table with readable text layout classes', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(async (input: RequestInfo | URL) => {
        const url = String(input);
        if (url.includes('/api/organizations')) {
          return Response.json([
            {
              id: 'a0000000-0000-4000-8000-000000000001',
              name: 'M2 Manager Dev',
              slug: 'm2-manager-dev',
              adminName: 'Michał Ociepka',
              adminEmail: 'multiadmin@m2manager.local',
              active: true,
              createdAt: '2026-01-01T10:00:00Z',
            },
          ]);
        }
        return new Response(null, { status: 404 });
      }),
    );

    renderOrganizationsPage({
      context: {
        user: { id: 'user-1', name: 'Super Admin', email: 'admin@m2manager.local' },
        activeOrganization: null,
        availableOrganizations: [],
        canSwitchOrganizations: true,
        mustChangePassword: false,
        superAdmin: true,
      },
    });

    const table = await screen.findByRole('table');
    const name = within(table).getByText('M2 Manager Dev');
    const admin = within(table).getByText('Michał Ociepka');

    expect(name.className).toContain('organizations-table__name');
    expect(name.className).not.toContain('organizations-table__slug');
    expect(admin.className).toContain('organizations-table__admin-name');
    expect(getComputedStyle(name).wordBreak).not.toBe('break-all');
    expect(within(table).getByRole('button', { name: 'Edytuj' })).toBeInTheDocument();
    expect(within(table).getByRole('button', { name: 'Resetuj hasło' })).toBeInTheDocument();

    vi.unstubAllGlobals();
  });
});
