import { Navigate, Route, Routes } from 'react-router-dom';
import { AppLayout } from '@/components/layout/AppLayout';
import { HomeRedirect } from '@/components/routing/HomeRedirect';
import { ProtectedRoute, PublicOnlyRoute } from '@/components/routing/ProtectedRoute';
import { TenantContextRoute } from '@/components/routing/TenantContextRoute';
import { appRoutes } from '@/config/navigation';
import { ActivitiesPage } from '@/features/activities/pages/ActivitiesPage';
import { BuildingsPage } from '@/features/buildings/pages/BuildingsPage';
import { BuildingContactsPage } from '@/features/contacts/pages/BuildingContactsPage';
import { ContactsPage } from '@/features/contacts/pages/ContactsPage';
import { EmployeesPage } from '@/features/employees/pages/EmployeesPage';
import { FinancePage } from '@/features/finance/pages/FinancePage';
import { BuildingFinancePage } from '@/features/finance/pages/BuildingFinancePage';
import { FleetPage } from '@/features/fleet/pages/FleetPage';
import { InventoryPage } from '@/features/inventory/pages/InventoryPage';
import { ManagersPage } from '@/features/managers/pages/ManagersPage';
import { OrganizationsPage } from '@/features/organizations/pages/OrganizationsPage';
import { RolesPage } from '@/features/roles/pages/RolesPage';
import { UsersPage } from '@/features/users/pages/UsersPage';
import { BuildingScopesPage } from '@/features/scopes/pages/BuildingScopesPage';
import { ScopesPage } from '@/features/scopes/pages/ScopesPage';
import { BuildingStaircasesPage } from '@/features/staircases/pages/BuildingStaircasesPage';
import { StaircasesPage } from '@/features/staircases/pages/StaircasesPage';
import { SupervisorsPage } from '@/features/supervisors/pages/SupervisorsPage';
import { ChangePasswordPage } from '@/pages/ChangePasswordPage';
import { DashboardPage } from '@/pages/dashboard/DashboardPage';
import { LoginPage } from '@/pages/LoginPage';
import { PlaceholderPage } from '@/pages/PlaceholderPage';
import { SettingsPage } from '@/pages/SettingsPage';

const IMPLEMENTED_ROUTES = new Set([
  '/activities',
  '/buildings',
  '/contacts',
  '/dashboard',
  '/employees',
  '/finance',
  '/fleet',
  '/inventory',
  '/managers',
  '/organizations',
  '/roles',
  '/settings',
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
        <Route path="/change-password" element={<ChangePasswordPage />} />
        <Route element={<AppLayout />}>
          <Route path="/" element={<HomeRedirect />} />
          <Route path="/organizations" element={<OrganizationsPage />} />
          <Route path="/settings" element={<SettingsPage />} />
          <Route path="/settings/change-password" element={<ChangePasswordPage />} />
          <Route element={<TenantContextRoute />}>
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/activities" element={<ActivitiesPage />} />
            <Route path="/contacts" element={<ContactsPage />} />
            <Route path="/employees" element={<EmployeesPage />} />
            <Route path="/finance" element={<FinancePage />} />
            <Route path="/fleet" element={<FleetPage />} />
            <Route path="/inventory" element={<InventoryPage />} />
            <Route path="/managers" element={<ManagersPage />} />
            <Route path="/users" element={<UsersPage />} />
            <Route path="/roles" element={<RolesPage />} />
            <Route path="/scopes" element={<ScopesPage />} />
            <Route path="/staircases" element={<StaircasesPage />} />
            <Route path="/supervisors" element={<SupervisorsPage />} />
            <Route path="/buildings/:buildingId/contacts" element={<BuildingContactsPage />} />
            <Route path="/buildings/:buildingId/finance" element={<BuildingFinancePage />} />
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
      </Route>

      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}
