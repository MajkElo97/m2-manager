import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi, beforeEach } from 'vitest';
import { useEffect } from 'react';
import { ProtectedRoute } from '@/components/routing/ProtectedRoute';
import { AuthProvider } from '@/features/auth/AuthProvider';
import { PermissionProvider, usePermissions } from '@/features/permissions/PermissionProvider';
import { ThemeProvider, useTheme } from '@/hooks/ThemeProvider';
import { Sidebar } from '@/components/layout/Sidebar';
import { Route, Routes } from 'react-router-dom';
import { TestAuthProvider } from '@/test/testAuthProvider';
import { createMockJwt } from '@/test/testUtils';

function SidebarHarness() {
  const { loadPermissions } = usePermissions();

  useEffect(() => {
    void loadPermissions();
  }, [loadPermissions]);

  return <Sidebar mobileOpen={false} />;
}

function renderProtectedRoute(initialEntry = '/dashboard') {
  return render(
    <MemoryRouter initialEntries={[initialEntry]}>
      <ThemeProvider>
        <PermissionProvider adapter={{ loadPermissions: async () => [] }}>
          <AuthProvider>
            <Routes>
              <Route element={<ProtectedRoute />}>
                <Route path="/dashboard" element={<div>Dashboard content</div>} />
              </Route>
              <Route path="/login" element={<div>Login page</div>} />
            </Routes>
          </AuthProvider>
        </PermissionProvider>
      </ThemeProvider>
    </MemoryRouter>,
  );
}

describe('ProtectedRoute', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('redirects unauthenticated user to login', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(async (input: RequestInfo | URL) => {
        const url = String(input);
        if (url.includes('/actuator/health')) {
          return new Response(null, { status: 200 });
        }
        if (url.includes('/api/auth/refresh')) {
          return new Response(null, { status: 401 });
        }
        return new Response(null, { status: 404 });
      }),
    );

    renderProtectedRoute();

    expect(await screen.findByText('Login page')).toBeInTheDocument();
    vi.unstubAllGlobals();
  });

  it('allows authenticated user to access dashboard', async () => {
    const token = createMockJwt({
      sub: 'user-1',
      organization_id: 'org-1',
      email: 'user@example.com',
      exp: Math.floor(Date.now() / 1000) + 3600,
    });

    vi.stubGlobal(
      'fetch',
      vi.fn(async (input: RequestInfo | URL) => {
        const url = String(input);
        if (url.includes('/actuator/health')) {
          return new Response(null, { status: 200 });
        }
        if (url.includes('/api/auth/refresh')) {
          return Response.json({ accessToken: token, tokenType: 'Bearer', expiresIn: 900, mustChangePassword: false });
        }
        if (url.includes('/api/auth/permissions')) {
          return new Response(null, { status: 404 });
        }
        if (url.includes('/api/auth/context')) {
          return Response.json({
            user: { id: 'user-1', name: 'user@example.com', email: 'user@example.com' },
            activeOrganization: { id: 'org-1', name: 'Test Org', slug: 'test-org' },
            availableOrganizations: [{ id: 'org-1', name: 'Test Org', slug: 'test-org' }],
            canSwitchOrganizations: false,
            mustChangePassword: false,
            superAdmin: false,
          });
        }
        return new Response(null, { status: 404 });
      }),
    );

    renderProtectedRoute();

    expect(await screen.findByText('Dashboard content')).toBeInTheDocument();
    vi.unstubAllGlobals();
  });
});

describe('Auth logout', () => {
  it('clears auth state after logout', async () => {
    const { tokenStore } = await import('@/features/auth/tokenStore');
    tokenStore.set('temporary-token');
    expect(tokenStore.get()).toBe('temporary-token');

    const { authService } = await import('@/features/auth/authService');

    vi.stubGlobal(
      'fetch',
      vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
        const url = String(input);
        if (url.includes('/actuator/health')) {
          return new Response(null, { status: 200 });
        }
        if (url.includes('/api/auth/logout')) {
          expect(init?.method).toBe('POST');
          return new Response(null, { status: 204 });
        }
        return new Response(null, { status: 404 });
      }),
    );

    await authService.logout();
    expect(tokenStore.get()).toBeNull();
    vi.unstubAllGlobals();
  });
});

describe('ThemeProvider', () => {
  it('switches theme preference', async () => {
    const user = userEvent.setup();

    function ThemeProbe() {
      const { preference, toggleTheme } = useTheme();
      return (
        <div>
          <span data-testid="theme">{preference}</span>
          <button type="button" onClick={toggleTheme}>
            toggle
          </button>
        </div>
      );
    }

    localStorage.setItem('m2-manager-theme', 'light');

    render(
      <ThemeProvider>
        <ThemeProbe />
      </ThemeProvider>,
    );

    expect(screen.getByTestId('theme')).toHaveTextContent('light');
    await user.click(screen.getByRole('button', { name: 'toggle' }));

    await waitFor(() => {
      expect(screen.getByTestId('theme')).toHaveTextContent('dark');
      expect(document.documentElement.dataset.theme).toBe('dark');
    });
  });
});

describe('Sidebar permission-aware navigation', () => {
  it('hides unauthorized module links', async () => {
    render(
      <MemoryRouter>
        <ThemeProvider>
          <TestAuthProvider>
            <PermissionProvider adapter={{ loadPermissions: async () => ['DASHBOARD_VIEW'] }}>
              <SidebarHarness />
            </PermissionProvider>
          </TestAuthProvider>
        </ThemeProvider>
      </MemoryRouter>,
    );

    expect(await screen.findByRole('link', { name: /Dashboard/i })).toBeInTheDocument();
    expect(screen.queryByRole('link', { name: /Budynki/i })).not.toBeInTheDocument();
  });
});
