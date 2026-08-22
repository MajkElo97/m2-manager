import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { FullPageLoadingState } from '@/components/ui/LoadingState';
import { useAuth } from '@/features/auth/AuthProvider';

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
    return <Navigate to="/dashboard" replace />;
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
    return <Navigate to="/dashboard" replace />;
  }

  return <Outlet />;
}
