import { NavLink } from 'react-router-dom';
import { adminNavigation, mainNavigation, type NavigationItem } from '@/config/navigation';
import { useAuth } from '@/features/auth/AuthProvider';
import { usePermissions } from '@/features/permissions/PermissionProvider';
import './Sidebar.css';

interface SidebarProps {
  mobileOpen: boolean;
  onNavigate?: () => void;
}

function isNavigationItemVisible(
  item: NavigationItem,
  hasPermission: (code: string) => boolean,
  isSuperAdmin: boolean,
  hasActiveOrganization: boolean,
) {
  if (isSuperAdmin && !hasActiveOrganization) {
    if (item.superAdminOnly) {
      return true;
    }
    if (item.path === '/settings') {
      return hasPermission('SETTINGS_VIEW');
    }
    return false;
  }

  if (item.superAdminOnly) {
    return isSuperAdmin;
  }
  if (!item.requiredPermission) {
    return false;
  }
  return hasPermission(item.requiredPermission);
}

function filterNavigation(
  items: NavigationItem[],
  hasPermission: (code: string) => boolean,
  isSuperAdmin: boolean,
  hasActiveOrganization: boolean,
) {
  return items.filter((item) =>
    isNavigationItemVisible(item, hasPermission, isSuperAdmin, hasActiveOrganization),
  );
}

export function Sidebar({ mobileOpen, onNavigate }: SidebarProps) {
  const { hasPermission } = usePermissions();
  const { context } = useAuth();
  const isSuperAdmin = context?.superAdmin ?? false;
  const hasActiveOrganization = Boolean(context?.activeOrganization);
  const visibleMain = filterNavigation(mainNavigation, hasPermission, isSuperAdmin, hasActiveOrganization);
  const visibleAdmin = filterNavigation(adminNavigation, hasPermission, isSuperAdmin, hasActiveOrganization);

  return (
    <aside className={`sidebar ${mobileOpen ? 'sidebar--open' : ''}`} aria-label="Nawigacja główna">
      <nav className="sidebar__nav">
        {visibleMain.length > 0 ? (
          <ul className="sidebar__list">
            {visibleMain.map((item) => (
              <li key={item.path}>
                <NavLink
                  to={item.path}
                  className={({ isActive }) =>
                    `sidebar__link ${isActive ? 'sidebar__link--active' : ''}`.trim()
                  }
                  onClick={onNavigate}
                >
                  <item.icon size={18} aria-hidden="true" />
                  <span>{item.label}</span>
                </NavLink>
              </li>
            ))}
          </ul>
        ) : null}

        {visibleAdmin.length > 0 ? (
          <>
            <p className="sidebar__section-title">Administracja</p>
            <ul className="sidebar__list">
              {visibleAdmin.map((item) => (
                <li key={item.path}>
                  <NavLink
                    to={item.path}
                    className={({ isActive }) =>
                      `sidebar__link ${isActive ? 'sidebar__link--active' : ''}`.trim()
                    }
                    onClick={onNavigate}
                  >
                    <item.icon size={18} aria-hidden="true" />
                    <span>{item.label}</span>
                  </NavLink>
                </li>
              ))}
            </ul>
          </>
        ) : null}
      </nav>
    </aside>
  );
}
