import { Navigate, Route, Routes } from 'react-router-dom';
import { AppLayout } from '@/components/layout/AppLayout';
import { ProtectedRoute, PublicOnlyRoute } from '@/components/routing/ProtectedRoute';
import { appRoutes } from '@/config/navigation';
import { BuildingsPage } from '@/features/buildings/pages/BuildingsPage';
import { BuildingStaircasesPage } from '@/features/staircases/pages/BuildingStaircasesPage';
import { StaircasesPage } from '@/features/staircases/pages/StaircasesPage';
import { DashboardPage } from '@/pages/dashboard/DashboardPage';
import { LoginPage } from '@/pages/LoginPage';
import { PlaceholderPage } from '@/pages/PlaceholderPage';

const IMPLEMENTED_ROUTES = new Set(['/buildings', '/dashboard', '/staircases']);

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
          <Route path="/staircases" element={<StaircasesPage />} />
          <Route path="/buildings/:buildingId/staircases" element={<BuildingStaircasesPage />} />
          <Route path="/buildings" element={<BuildingsPage />} />
          {appRoutes
            .filter((route) => route.placeholder && !IMPLEMENTED_ROUTES.has(route.path))
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
