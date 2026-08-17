import { Navigate, Outlet } from 'react-router-dom';
import { FullPageLoadingState } from '@/components/ui/LoadingState';
import { useAuth } from '@/features/auth/AuthProvider';

export function ProtectedRoute() {
  const { status } = useAuth();

  if (status === 'initializing') {
    return <FullPageLoadingState label="Inicjalizacja sesji…" />;
  }

  if (status !== 'authenticated') {
    return <Navigate to="/login" replace />;
  }

  return <Outlet />;
}

export function PublicOnlyRoute() {
  const { status } = useAuth();

  if (status === 'initializing') {
    return <FullPageLoadingState />;
  }

  if (status === 'authenticated') {
    return <Navigate to="/dashboard" replace />;
  }

  return <Outlet />;
}
