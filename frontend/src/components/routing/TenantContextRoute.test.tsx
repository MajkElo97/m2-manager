import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { describe, expect, it } from 'vitest';
import { TenantContextRoute } from '@/components/routing/TenantContextRoute';
import { TestAuthProvider } from '@/test/testAuthProvider';
import { ThemeProvider } from '@/hooks/ThemeProvider';

function renderTenantRoute(authOverrides?: Parameters<typeof TestAuthProvider>[0]['value']) {
  return render(
    <MemoryRouter initialEntries={['/buildings']}>
      <ThemeProvider>
        <TestAuthProvider value={authOverrides}>
          <Routes>
            <Route element={<TenantContextRoute />}>
              <Route path="/buildings" element={<div>Tenant buildings page</div>} />
            </Route>
          </Routes>
        </TestAuthProvider>
      </ThemeProvider>
    </MemoryRouter>,
  );
}

describe('TenantContextRoute', () => {
  it('blocks tenant route for super admin without active organization', () => {
    renderTenantRoute({
      organizationContextKey: null,
      context: {
        user: { id: 'super-1', name: 'Super Admin', email: 'admin@m2manager.local' },
        activeOrganization: null,
        availableOrganizations: [{ id: 'org-dev', name: 'M2 Manager Dev', slug: 'm2-manager-dev' }],
        canSwitchOrganizations: true,
        mustChangePassword: false,
        superAdmin: true,
      },
    });

    expect(screen.getByText('Brak wybranej organizacji')).toBeInTheDocument();
    expect(screen.queryByText('Tenant buildings page')).not.toBeInTheDocument();
  });

  it('allows tenant route when organization context is active', () => {
    renderTenantRoute({
      organizationContextKey: 'org-dev',
      context: {
        user: { id: 'super-1', name: 'Super Admin', email: 'admin@m2manager.local' },
        activeOrganization: { id: 'org-dev', name: 'M2 Manager Dev', slug: 'm2-manager-dev' },
        availableOrganizations: [{ id: 'org-dev', name: 'M2 Manager Dev', slug: 'm2-manager-dev' }],
        canSwitchOrganizations: true,
        mustChangePassword: false,
        superAdmin: true,
      },
    });

    expect(screen.getByText('Tenant buildings page')).toBeInTheDocument();
  });
});
