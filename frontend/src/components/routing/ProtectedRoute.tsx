import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { FullPageLoadingState } from '@/components/ui/LoadingState';
import { useAuth } from '@/features/auth/AuthProvider';

function resolveAuthenticatedHome(context: NonNullable<ReturnType<typeof useAuth>['context']>): string {
  if (context.superAdmin && !context.activeOrganization) {
    return '/organizations';
  }
  return '/dashboard';
}

export function ProtectedRoute() {
  const { status, context } = useAuth();
  const location = useLocation();

  if (status === 'initializing') {
    return <FullPageLoadingState label="Inicjalizacja sesji…" />;
  }

  if (status !== 'authenticated') {
    return <Navigate to="/login" replace />;
  }

  if (context?.mustChangePassword && location.pathname !== '/change-password') {
    return <Navigate to="/change-password" replace />;
  }

  if (!context?.mustChangePassword && location.pathname === '/change-password') {
    return <Navigate to={context ? resolveAuthenticatedHome(context) : '/dashboard'} replace />;
  }

  return <Outlet />;
}

export function PublicOnlyRoute() {
  const { status, context } = useAuth();

  if (status === 'initializing') {
    return <FullPageLoadingState />;
  }

  if (status === 'authenticated') {
    if (context?.mustChangePassword) {
      return <Navigate to="/change-password" replace />;
    }
    return <Navigate to={resolveAuthenticatedHome(context!)} replace />;
  }

  return <Outlet />;
}
