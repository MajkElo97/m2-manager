import { render, type RenderOptions } from '@testing-library/react';
import { MemoryRouter, type MemoryRouterProps } from 'react-router-dom';
import { type ReactElement, type ReactNode } from 'react';
import { PermissionProvider } from '@/features/permissions/PermissionProvider';
import { ThemeProvider } from '@/hooks/ThemeProvider';
import type { PermissionsAdapter } from '@/features/permissions/permissionsAdapter';
import { TestAuthProvider } from '@/test/testAuthProvider';

interface TestProvidersProps {
  children: ReactNode;
  routerProps?: MemoryRouterProps;
  permissionsAdapter?: PermissionsAdapter;
}

export function TestProviders({
  children,
  routerProps,
  permissionsAdapter,
}: TestProvidersProps) {
  return (
    <MemoryRouter {...routerProps}>
      <ThemeProvider>
        <PermissionProvider adapter={permissionsAdapter}>
          <TestAuthProvider>{children}</TestAuthProvider>
        </PermissionProvider>
      </ThemeProvider>
    </MemoryRouter>
  );
}

export function renderWithProviders(
  ui: ReactElement,
  options?: RenderOptions & {
    routerProps?: MemoryRouterProps;
    permissionsAdapter?: PermissionsAdapter;
  },
) {
  const { routerProps, permissionsAdapter, ...renderOptions } = options ?? {};

  return render(ui, {
    wrapper: ({ children }) => (
      <TestProviders routerProps={routerProps} permissionsAdapter={permissionsAdapter}>
        {children}
      </TestProviders>
    ),
    ...renderOptions,
  });
}

export function createMockJwt(payload: Record<string, unknown>): string {
  const encode = (value: object) =>
    btoa(JSON.stringify(value)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');

  return `${encode({ alg: 'HS256', typ: 'JWT' })}.${encode(payload)}.signature`;
}
