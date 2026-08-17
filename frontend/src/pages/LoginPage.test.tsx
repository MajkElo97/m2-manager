import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { LoginPage } from '@/pages/LoginPage';
import { AuthProvider } from '@/features/auth/AuthProvider';
import { MemoryRouter } from 'react-router-dom';
import { PermissionProvider } from '@/features/permissions/PermissionProvider';
import { ThemeProvider } from '@/hooks/ThemeProvider';
import { createMockJwt } from '@/test/testUtils';

function renderLoginPage() {
  return render(
    <MemoryRouter initialEntries={['/login']}>
      <ThemeProvider>
        <PermissionProvider
          adapter={{
            loadPermissions: async () => ['DASHBOARD_VIEW'],
          }}
        >
          <AuthProvider>
            <LoginPage />
          </AuthProvider>
        </PermissionProvider>
      </ThemeProvider>
    </MemoryRouter>,
  );
}

describe('LoginPage', () => {
  it('renders login form', () => {
    renderLoginPage();

    expect(screen.getByRole('heading', { name: 'M2 Manager' })).toBeInTheDocument();
    expect(screen.getByLabelText('Organizacja')).toBeInTheDocument();
    expect(screen.getByLabelText('E-mail')).toBeInTheDocument();
    expect(screen.getByLabelText('Hasło')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Zaloguj się' })).toBeInTheDocument();
  });

  it('shows validation errors for empty form', async () => {
    const user = userEvent.setup();
    renderLoginPage();

    await user.clear(screen.getByLabelText('Organizacja'));
    await user.click(screen.getByRole('button', { name: 'Zaloguj się' }));

    expect(await screen.findByText('Podaj identyfikator organizacji.')).toBeInTheDocument();
    expect(screen.getByText('Podaj adres e-mail.')).toBeInTheDocument();
    expect(screen.getByText('Podaj hasło.')).toBeInTheDocument();
  });

  it('stores access token in memory after successful login', async () => {
    const user = userEvent.setup();
    const fetchMock = vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
      const url = String(input);

      if (url.includes('/actuator/health')) {
        return new Response(null, { status: 200 });
      }

      if (url.includes('/api/auth/refresh')) {
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

      return new Response(null, { status: 404 });
    });

    vi.stubGlobal('fetch', fetchMock);
    renderLoginPage();

    await user.type(screen.getByLabelText('E-mail'), 'user@example.com');
    await user.type(screen.getByLabelText('Hasło'), 'password');
    await user.click(screen.getByRole('button', { name: 'Zaloguj się' }));

    await waitFor(() => {
      expect(fetchMock).toHaveBeenCalledWith(
        expect.stringContaining('/api/auth/login'),
        expect.objectContaining({ method: 'POST' }),
      );
    });

    vi.unstubAllGlobals();
  });

  it('shows friendly error for failed login', async () => {
    const user = userEvent.setup();

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

        if (url.includes('/api/auth/login')) {
          return Response.json(
            { status: 401, message: 'Invalid credentials' },
            { status: 401 },
          );
        }

        return new Response(null, { status: 404 });
      }),
    );

    renderLoginPage();

    await user.type(screen.getByLabelText('E-mail'), 'user@example.com');
    await user.type(screen.getByLabelText('Hasło'), 'wrong');
    await user.click(screen.getByRole('button', { name: 'Zaloguj się' }));

    expect(await screen.findByText('Nieprawidłowy login lub hasło.')).toBeInTheDocument();
    vi.unstubAllGlobals();
  });
});
