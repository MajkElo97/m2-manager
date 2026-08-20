import { useMemo, type ReactNode } from 'react';
import { AuthContext, type AuthContextValue } from '@/features/auth/AuthProvider';
import type { AuthState } from '@/features/auth/authTypes';

const defaultTestAuthState: AuthState = {
  status: 'authenticated',
  user: {
    userId: '00000000-0000-4000-8000-000000000001',
    organizationId: '00000000-0000-4000-8000-000000000002',
    email: 'test@example.com',
  },
  accessToken: 'test-access-token',
  context: {
    user: {
      id: '00000000-0000-4000-8000-000000000001',
      name: 'Test User',
      email: 'test@example.com',
    },
    activeOrganization: {
      id: '00000000-0000-4000-8000-000000000002',
      name: 'Test Organization',
      slug: 'test-org',
    },
    availableOrganizations: [
      {
        id: '00000000-0000-4000-8000-000000000002',
        name: 'Test Organization',
        slug: 'test-org',
      },
    ],
    canSwitchOrganizations: false,
  },
  organizationContextKey: '00000000-0000-4000-8000-000000000002',
};

export function TestAuthProvider({
  children,
  value,
}: {
  children: ReactNode;
  value?: Partial<AuthState> & {
    switchOrganization?: AuthContextValue['switchOrganization'];
  };
}) {
  const contextValue = useMemo<AuthContextValue>(
    () => ({
      ...defaultTestAuthState,
      ...value,
      login: async () => {},
      logout: async () => {},
      switchOrganization: value?.switchOrganization ?? (async () => {}),
    }),
    [value],
  );

  return <AuthContext.Provider value={contextValue}>{children}</AuthContext.Provider>;
}
