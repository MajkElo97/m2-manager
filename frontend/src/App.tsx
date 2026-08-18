import { Navigate, Route, Routes } from 'react-router-dom';
import { AppLayout } from '@/components/layout/AppLayout';
import { ProtectedRoute, PublicOnlyRoute } from '@/components/routing/ProtectedRoute';
import { appRoutes } from '@/config/navigation';
import { BuildingsPage } from '@/features/buildings/pages/BuildingsPage';
import { DashboardPage } from '@/pages/dashboard/DashboardPage';
import { LoginPage } from '@/pages/LoginPage';
import { PlaceholderPage } from '@/pages/PlaceholderPage';

export function AppRoutes() {
  return (
    <Routes>
      <Route element={<PublicOnlyRoute />}>
        <Route path="/login" element={<LoginPage />} />
      </Route>

      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/buildings" element={<BuildingsPage />} />
          {appRoutes
            .filter((route) => route.placeholder && route.path !== '/buildings')
            .map((route) => (
              <Route
                key={route.path}
                path={route.path}
                element={<PlaceholderPage moduleName={route.moduleName} />}
              />
            ))}
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}
