import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import { AuthProvider } from '@/features/auth/AuthProvider';
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
          });
        }

        if (url.includes('/api/auth/permissions')) {
          return new Response(null, { status: 404 });
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
});
