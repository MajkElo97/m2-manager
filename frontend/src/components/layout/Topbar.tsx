import { LogOut, Menu, Moon, Sun, UserCircle2 } from 'lucide-react';
import { useState } from 'react';
import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { useAuth } from '@/features/auth/AuthProvider';
import { useTheme } from '@/hooks/ThemeProvider';
import './Topbar.css';

interface TopbarProps {
  onMenuClick: () => void;
  showMenuButton: boolean;
}

export function Topbar({ onMenuClick, showMenuButton }: TopbarProps) {
  const { context, logout, switchOrganization } = useAuth();
  const { preference, toggleTheme } = useTheme();
  const [isSwitching, setIsSwitching] = useState(false);

  const themeLabel =
    preference === 'dark' ? 'Przełącz na jasny motyw' : 'Przełącz na ciemny motyw';

  const displayName = context?.user.name ?? context?.user.email ?? 'Użytkownik';
  const displayEmail = context?.user.email ?? '';
  const activeOrganization = context?.activeOrganization ?? null;
  const availableOrganizations = context?.availableOrganizations ?? [];
  const isSuperAdmin = context?.superAdmin ?? false;
  const showOrganizationSelector = isSuperAdmin
    ? availableOrganizations.length > 0
    : Boolean(activeOrganization);
  const showOrganizationDropdown = isSuperAdmin
    ? availableOrganizations.length > 0
    : (context?.canSwitchOrganizations ?? false) && availableOrganizations.length > 1;

  async function handleOrganizationChange(nextOrganizationId: string) {
    if (!nextOrganizationId || isSwitching) {
      return;
    }

    if (activeOrganization?.id === nextOrganizationId) {
      return;
    }

    setIsSwitching(true);
    try {
      await switchOrganization(nextOrganizationId);
    } catch (error) {
      console.error('Organization switch failed', error);
    } finally {
      setIsSwitching(false);
    }
  }

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

      <div className="topbar__spacer" aria-hidden="true" />

      <div className="topbar__right">
        {showOrganizationSelector ? (
          <div className="topbar__organization">
            <span className="topbar__organization-label">Organizacja:</span>
            {showOrganizationDropdown ? (
              <select
                className="topbar__organization-select"
                aria-label="Wybierz organizację"
                value={activeOrganization?.id ?? ''}
                disabled={isSwitching}
                onChange={(event) => void handleOrganizationChange(event.target.value)}
              >
                {!activeOrganization ? (
                  <option value="" disabled>
                    Brak organizacji
                  </option>
                ) : null}
                {availableOrganizations.map((organization) => (
                  <option key={organization.id} value={organization.id}>
                    {organization.name}
                  </option>
                ))}
              </select>
            ) : (
              <span className="topbar__organization-value">
                {activeOrganization?.name ?? 'Brak organizacji'}
              </span>
            )}
          </div>
        ) : null}

        {isSuperAdmin ? (
          <Badge variant="warning">SUPER ADMIN</Badge>
        ) : null}

        <Button variant="ghost" size="sm" aria-label={themeLabel} onClick={toggleTheme}>
          {preference === 'dark' ? <Sun size={18} /> : <Moon size={18} />}
        </Button>

        <div className="topbar__profile" aria-label="Obszar użytkownika">
          <span className="topbar__avatar" aria-hidden="true">
            <UserCircle2 size={20} />
          </span>
          <div className="topbar__profile-text">
            <span className="topbar__user-name">{displayName}</span>
            {displayEmail ? <span className="topbar__user-email">{displayEmail}</span> : null}
          </div>
        </div>

        <Button variant="secondary" size="sm" onClick={() => void logout()}>
          <LogOut size={16} aria-hidden="true" />
          Wyloguj
        </Button>
      </div>
    </header>
  );
}
