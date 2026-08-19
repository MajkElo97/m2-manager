import { Navigate, Route, Routes } from 'react-router-dom';
import { AppLayout } from '@/components/layout/AppLayout';
import { ProtectedRoute, PublicOnlyRoute } from '@/components/routing/ProtectedRoute';
import { appRoutes } from '@/config/navigation';
import { ActivitiesPage } from '@/features/activities/pages/ActivitiesPage';
import { BuildingsPage } from '@/features/buildings/pages/BuildingsPage';
import { BuildingContactsPage } from '@/features/contacts/pages/BuildingContactsPage';
import { ContactsPage } from '@/features/contacts/pages/ContactsPage';
import { EmployeesPage } from '@/features/employees/pages/EmployeesPage';
import { FleetPage } from '@/features/fleet/pages/FleetPage';
import { InventoryPage } from '@/features/inventory/pages/InventoryPage';
import { ManagersPage } from '@/features/managers/pages/ManagersPage';
import { RolesPage } from '@/features/roles/pages/RolesPage';
import { UsersPage } from '@/features/users/pages/UsersPage';
import { BuildingScopesPage } from '@/features/scopes/pages/BuildingScopesPage';
import { ScopesPage } from '@/features/scopes/pages/ScopesPage';
import { BuildingStaircasesPage } from '@/features/staircases/pages/BuildingStaircasesPage';
import { StaircasesPage } from '@/features/staircases/pages/StaircasesPage';
import { SupervisorsPage } from '@/features/supervisors/pages/SupervisorsPage';
import { DashboardPage } from '@/pages/dashboard/DashboardPage';
import { LoginPage } from '@/pages/LoginPage';
import { PlaceholderPage } from '@/pages/PlaceholderPage';

const IMPLEMENTED_ROUTES = new Set([
  '/activities',
  '/buildings',
  '/contacts',
  '/dashboard',
  '/employees',
  '/fleet',
  '/inventory',
  '/managers',
  '/roles',
  '/users',
  '/scopes',
  '/staircases',
  '/supervisors',
]);

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
          <Route path="/activities" element={<ActivitiesPage />} />
          <Route path="/contacts" element={<ContactsPage />} />
          <Route path="/employees" element={<EmployeesPage />} />
          <Route path="/fleet" element={<FleetPage />} />
          <Route path="/inventory" element={<InventoryPage />} />
          <Route path="/managers" element={<ManagersPage />} />
          <Route path="/users" element={<UsersPage />} />
          <Route path="/roles" element={<RolesPage />} />
          <Route path="/scopes" element={<ScopesPage />} />
          <Route path="/staircases" element={<StaircasesPage />} />
          <Route path="/supervisors" element={<SupervisorsPage />} />
          <Route path="/buildings/:buildingId/contacts" element={<BuildingContactsPage />} />
          <Route path="/buildings/:buildingId/scopes" element={<BuildingScopesPage />} />
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
