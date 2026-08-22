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
) {
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
) {
  return items.filter((item) => isNavigationItemVisible(item, hasPermission, isSuperAdmin));
}

export function Sidebar({ mobileOpen, onNavigate }: SidebarProps) {
  const { hasPermission } = usePermissions();
  const { context } = useAuth();
  const isSuperAdmin = context?.superAdmin ?? false;
  const visibleMain = filterNavigation(mainNavigation, hasPermission, isSuperAdmin);
  const visibleAdmin = filterNavigation(adminNavigation, hasPermission, isSuperAdmin);

  return (
    <aside className={`sidebar ${mobileOpen ? 'sidebar--open' : ''}`} aria-label="Nawigacja główna">
      <nav className="sidebar__nav">
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
