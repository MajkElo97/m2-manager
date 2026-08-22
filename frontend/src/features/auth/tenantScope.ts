/**
 * Tenant-scoped API calls require an active business organization context.
 * SUPER_ADMIN without a selected organization has a null organizationContextKey.
 */
export function isTenantScopeActive(
  organizationContextKey: string | null,
): organizationContextKey is string {
  return organizationContextKey !== null;
}
