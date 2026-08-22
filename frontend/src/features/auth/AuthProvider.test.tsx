import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import { AuthProvider, useAuth } from '@/features/auth/AuthProvider';
import { PermissionProvider } from '@/features/permissions/PermissionProvider';
import { ThemeProvider } from '@/hooks/ThemeProvider';
import { PublicOnlyRoute, ProtectedRoute } from '@/components/routing/ProtectedRoute';
import { LoginPage } from '@/pages/LoginPage';
import { createMockJwt } from '@/test/testUtils';

function renderLoginFlow() {
  return render(
    <MemoryRouter initialEntries={['/login']}>
      <ThemeProvider>
        <PermissionProvider adapter={{ loadPermissions: async () => [] }}>
          <AuthProvider>
            <Routes>
              <Route element={<PublicOnlyRoute />}>
                <Route path="/login" element={<LoginPage />} />
              </Route>
              <Route element={<ProtectedRoute />}>
                <Route path="/dashboard" element={<div>Dashboard content</div>} />
              </Route>
            </Routes>
          </AuthProvider>
        </PermissionProvider>
      </ThemeProvider>
    </MemoryRouter>,
  );
}

describe('AuthProvider login flow', () => {
  it('redirects to dashboard after login without route-change reinitialization loop', async () => {
    const user = userEvent.setup();
    let refreshCalls = 0;

    vi.stubGlobal(
      'fetch',
      vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
        const url = String(input);

        if (url.includes('/actuator/health')) {
          return new Response(null, { status: 200 });
        }

        if (url.includes('/api/auth/refresh')) {
          refreshCalls += 1;
          return new Response(null, { status: 401 });
        }

        if (url.includes('/api/auth/login') && init?.method === 'POST') {
          const token = createMockJwt({
            sub: 'user-1',
            organization_id: 'org-1',
            email: 'user@example.com',
            exp: Math.floor(Date.now() / 1000) + 3600,
          });

          return Response.json({
            accessToken: token,
            tokenType: 'Bearer',
            expiresIn: 900,
            mustChangePassword: false,
          });
        }

        if (url.includes('/api/auth/permissions')) {
          return new Response(null, { status: 404 });
        }

        if (url.includes('/api/auth/context')) {
          return Response.json({
            user: {
              id: 'user-1',
              name: 'user@example.com',
              email: 'user@example.com',
            },
            activeOrganization: {
              id: 'org-1',
              name: 'Test Org',
              slug: 'test-org',
            },
            availableOrganizations: [
              {
                id: 'org-1',
                name: 'Test Org',
                slug: 'test-org',
              },
            ],
            canSwitchOrganizations: false,
            mustChangePassword: false,
            superAdmin: false,
          });
        }

        return new Response(null, { status: 404 });
      }),
    );

    renderLoginFlow();

    await screen.findByRole('button', { name: 'Zaloguj się' });

    await user.type(screen.getByLabelText('E-mail'), 'user@example.com');
    await user.type(screen.getByLabelText('Hasło'), 'password');
    await user.click(screen.getByRole('button', { name: 'Zaloguj się' }));

    expect(await screen.findByText('Dashboard content')).toBeInTheDocument();
    await waitFor(() => {
      expect(refreshCalls).toBe(1);
    });

    vi.unstubAllGlobals();
  });

  it('switchOrganization updates active organization context', async () => {
    const orgAToken = createMockJwt({
      sub: 'user-1',
      organization_id: 'org-a',
      email: 'user@example.com',
      exp: Math.floor(Date.now() / 1000) + 3600,
    });
    const orgBToken = createMockJwt({
      sub: 'user-1',
      organization_id: 'org-b',
      email: 'user@example.com',
      exp: Math.floor(Date.now() / 1000) + 3600,
    });

    vi.stubGlobal(
      'fetch',
      vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
        const url = String(input);

        if (url.includes('/actuator/health')) {
          return new Response(null, { status: 200 });
        }

        if (url.includes('/api/auth/refresh')) {
          return Response.json({
            accessToken: orgAToken,
            tokenType: 'Bearer',
            expiresIn: 900,
            mustChangePassword: false,
          });
        }

        if (url.includes('/api/auth/context/organization') && init?.method === 'POST') {
          return Response.json({
            accessToken: orgBToken,
            tokenType: 'Bearer',
            expiresIn: 900,
            mustChangePassword: false,
          });
        }

        if (url.includes('/api/auth/context')) {
          const authHeader = init?.headers instanceof Headers
            ? init.headers.get('Authorization')
            : (init?.headers as Record<string, string> | undefined)?.Authorization;

          const activeOrganizationId = authHeader?.includes(orgBToken) ? 'org-b' : 'org-a';

          return Response.json({
            user: {
              id: 'user-1',
              name: 'User Example',
              email: 'user@example.com',
            },
            activeOrganization: {
              id: activeOrganizationId,
              name: activeOrganizationId === 'org-b' ? 'Org B' : 'Org A',
              slug: activeOrganizationId === 'org-b' ? 'org-b' : 'org-a',
            },
            availableOrganizations: [
              { id: 'org-a', name: 'Org A', slug: 'org-a' },
              { id: 'org-b', name: 'Org B', slug: 'org-b' },
            ],
            canSwitchOrganizations: true,
            mustChangePassword: false,
            superAdmin: false,
          });
        }

        if (url.includes('/api/auth/permissions')) {
          return Response.json({ permissions: ['BUILDINGS_VIEW'] });
        }

        return new Response(null, { status: 404 });
      }),
    );

    function SwitchProbe() {
      const { context, switchOrganization } = useAuth();
      return (
        <div>
          <span data-testid="active-org">{context?.activeOrganization.name ?? 'none'}</span>
          <button type="button" onClick={() => void switchOrganization('org-b')}>
            Switch org
          </button>
        </div>
      );
    }

    const user = userEvent.setup();

    render(
      <MemoryRouter initialEntries={['/dashboard']}>
        <ThemeProvider>
          <PermissionProvider>
            <AuthProvider>
              <SwitchProbe />
            </AuthProvider>
          </PermissionProvider>
        </ThemeProvider>
      </MemoryRouter>,
    );

    expect(await screen.findByTestId('active-org')).toHaveTextContent('Org A');
    await user.click(screen.getByRole('button', { name: 'Switch org' }));
    await waitFor(() => {
      expect(screen.getByTestId('active-org')).toHaveTextContent('Org B');
    });

    vi.unstubAllGlobals();
  });
});
