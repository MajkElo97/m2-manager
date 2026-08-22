import { Navigate } from 'react-router-dom';
import { useAuth } from '@/features/auth/AuthProvider';

export function HomeRedirect() {
  const { context } = useAuth();

  if (context?.superAdmin && !context.activeOrganization) {
    return <Navigate to="/organizations" replace />;
  }

  return <Navigate to="/dashboard" replace />;
}
