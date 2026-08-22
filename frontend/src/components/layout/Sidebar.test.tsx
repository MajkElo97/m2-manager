import { useEffect } from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it } from 'vitest';
import { Sidebar } from '@/components/layout/Sidebar';
import { TestAuthProvider } from '@/test/testAuthProvider';
import { PermissionProvider, usePermissions } from '@/features/permissions/PermissionProvider';
import { ThemeProvider } from '@/hooks/ThemeProvider';

const allTestPermissions = [
  'SETTINGS_VIEW',
  'BUILDINGS_VIEW',
  'DASHBOARD_VIEW',
  'USERS_VIEW',
  'ROLES_VIEW',
];

function SidebarWithPermissions({
  authOverrides,
}: {
  authOverrides?: Parameters<typeof TestAuthProvider>[0]['value'];
}) {
  const { loadPermissions } = usePermissions();

  useEffect(() => {
    void loadPermissions();
  }, [loadPermissions]);

  return (
    <TestAuthProvider value={authOverrides}>
      <Sidebar mobileOpen={false} />
    </TestAuthProvider>
  );
}

function renderSidebar(authOverrides?: Parameters<typeof TestAuthProvider>[0]['value']) {
  return render(
    <MemoryRouter>
      <ThemeProvider>
        <PermissionProvider adapter={{ loadPermissions: async () => allTestPermissions }}>
          <SidebarWithPermissions authOverrides={authOverrides} />
        </PermissionProvider>
      </ThemeProvider>
    </MemoryRouter>,
  );
}

describe('Sidebar super admin without organization', () => {
  it('shows only system modules when super admin has no active organization', async () => {
    renderSidebar({
      context: {
        user: { id: 'super-1', name: 'Super Admin', email: 'admin@m2manager.local' },
        activeOrganization: null,
        availableOrganizations: [{ id: 'org-dev', name: 'M2 Manager Dev', slug: 'm2-manager-dev' }],
        canSwitchOrganizations: true,
        mustChangePassword: false,
        superAdmin: true,
      },
    });

    expect(await screen.findByRole('link', { name: 'Organizacje' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Ustawienia' })).toBeInTheDocument();
    expect(screen.queryByRole('link', { name: 'Dashboard' })).not.toBeInTheDocument();
    expect(screen.queryByRole('link', { name: 'Budynki' })).not.toBeInTheDocument();
  });

  it('shows tenant modules when super admin has active organization', async () => {
    renderSidebar({
      context: {
        user: { id: 'super-1', name: 'Super Admin', email: 'admin@m2manager.local' },
        activeOrganization: { id: 'org-dev', name: 'M2 Manager Dev', slug: 'm2-manager-dev' },
        availableOrganizations: [{ id: 'org-dev', name: 'M2 Manager Dev', slug: 'm2-manager-dev' }],
        canSwitchOrganizations: true,
        mustChangePassword: false,
        superAdmin: true,
      },
    });

    await waitFor(() => {
      expect(screen.getByRole('link', { name: 'Dashboard' })).toBeInTheDocument();
    });
    expect(screen.getByRole('link', { name: 'Organizacje' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Budynki' })).toBeInTheDocument();
  });
});
