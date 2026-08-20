import { useAuth } from '@/features/auth/AuthProvider';

/**
 * Active organization scope key from AuthProvider.
 * Changes when the user switches organization; include in tenant-scoped hook
 * refetch dependencies so data reloads for the new organization context.
 */
export function useOrganizationContextKey(): string | null {
  return useAuth().organizationContextKey;
}
