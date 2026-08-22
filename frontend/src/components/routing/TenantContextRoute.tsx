import { Outlet } from 'react-router-dom';
import { useAuth } from '@/features/auth/AuthProvider';
import { NoOrganizationSelectedPage } from '@/pages/NoOrganizationSelectedPage';

export function TenantContextRoute() {
  const { organizationContextKey, context } = useAuth();

  if (!organizationContextKey) {
    if (context?.superAdmin) {
      return <NoOrganizationSelectedPage />;
    }
    return <NoOrganizationSelectedPage />;
  }

  return <Outlet />;
}
