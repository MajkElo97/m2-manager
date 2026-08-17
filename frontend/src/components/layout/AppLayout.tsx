import { useEffect, useState, type ReactNode } from 'react';
import { Outlet } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { Topbar } from './Topbar';
import './AppLayout.css';

export function AppLayout() {
  const [mobileOpen, setMobileOpen] = useState(false);
  const [isCompactLayout, setIsCompactLayout] = useState(window.innerWidth <= 1024);

  useEffect(() => {
    const mediaQuery = window.matchMedia('(max-width: 1024px)');

    const updateLayout = () => {
      setIsCompactLayout(mediaQuery.matches);
      if (!mediaQuery.matches) {
        setMobileOpen(false);
      }
    };

    updateLayout();
    mediaQuery.addEventListener('change', updateLayout);
    return () => mediaQuery.removeEventListener('change', updateLayout);
  }, []);

  return (
    <div className="app-layout">
      <Topbar
        showMenuButton={isCompactLayout}
        onMenuClick={() => setMobileOpen((open) => !open)}
      />

      <div className="app-layout__body">
        <Sidebar mobileOpen={mobileOpen} onNavigate={() => setMobileOpen(false)} />

        {mobileOpen ? (
          <button
            type="button"
            className="app-layout__backdrop"
            aria-label="Zamknij menu"
            onClick={() => setMobileOpen(false)}
          />
        ) : null}

        <main className="app-layout__content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

export function AppLayoutContainer({ children }: { children: ReactNode }) {
  return <div className="app-layout__content-inner">{children}</div>;
}
