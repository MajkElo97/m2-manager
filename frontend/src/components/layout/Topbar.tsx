import { LogOut, Menu, Moon, Sun, UserCircle2 } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { useAuth } from '@/features/auth/AuthProvider';
import { useTheme } from '@/hooks/ThemeProvider';
import './Topbar.css';

interface TopbarProps {
  onMenuClick: () => void;
  showMenuButton: boolean;
}

export function Topbar({ onMenuClick, showMenuButton }: TopbarProps) {
  const { user, logout } = useAuth();
  const { preference, toggleTheme } = useTheme();

  const themeLabel =
    preference === 'dark' ? 'Przełącz na jasny motyw' : 'Przełącz na ciemny motyw';

  return (
    <header className="topbar">
      <div className="topbar__left">
        {showMenuButton ? (
          <Button variant="ghost" size="sm" aria-label="Otwórz menu" onClick={onMenuClick}>
            <Menu size={18} />
          </Button>
        ) : null}
        <div className="topbar__brand">
          <span className="topbar__brand-mark">M2</span>
          <span className="topbar__brand-name">M2 Manager</span>
        </div>
      </div>

      <div className="topbar__right">
        <div className="topbar__user" aria-label="Obszar użytkownika">
          <UserCircle2 size={18} aria-hidden="true" />
          <span className="topbar__user-email">{user?.email ?? 'Użytkownik'}</span>
        </div>

        <Button variant="ghost" size="sm" aria-label={themeLabel} onClick={toggleTheme}>
          {preference === 'dark' ? <Sun size={18} /> : <Moon size={18} />}
        </Button>

        <Button variant="secondary" size="sm" onClick={() => void logout()}>
          <LogOut size={16} aria-hidden="true" />
          Wyloguj
        </Button>
      </div>
    </header>
  );
}
