import { NavLink } from 'react-router-dom';
import { adminNavigation, mainNavigation, type NavigationItem } from '@/config/navigation';
import { usePermissions } from '@/features/permissions/PermissionProvider';
import './Sidebar.css';

interface SidebarProps {
  mobileOpen: boolean;
  onNavigate?: () => void;
}

function filterNavigation(items: NavigationItem[], hasPermission: (code: string) => boolean) {
  return items.filter((item) => hasPermission(item.requiredPermission));
}

export function Sidebar({ mobileOpen, onNavigate }: SidebarProps) {
  const { hasPermission } = usePermissions();
  const visibleMain = filterNavigation(mainNavigation, hasPermission);
  const visibleAdmin = filterNavigation(adminNavigation, hasPermission);

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
