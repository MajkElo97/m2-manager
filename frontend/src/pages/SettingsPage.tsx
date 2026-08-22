import { useNavigate } from 'react-router-dom';
import { AppLayoutContainer } from '@/components/layout/AppLayout';
import { Button } from '@/components/ui/Button';
import { PageHeader } from '@/components/ui/PageHeader';
import { usePermissions } from '@/features/permissions/PermissionProvider';
import { Navigate } from 'react-router-dom';
import './ChangePasswordPage.css';

export function SettingsPage() {
  const navigate = useNavigate();
  const { hasPermission } = usePermissions();

  if (!hasPermission('SETTINGS_VIEW')) {
    return <Navigate to="/dashboard" replace />;
  }

  return (
    <AppLayoutContainer>
      <PageHeader
        title="Ustawienia"
        description="Konfiguracja konta i bezpieczeństwa."
      />

      <section className="settings-page__section">
        <h2 className="settings-page__section-title">Bezpieczeństwo</h2>
        <p className="settings-page__section-description">
          Zmień hasło używane do logowania w bieżącej organizacji.
        </p>
        <Button type="button" variant="secondary" onClick={() => navigate('/settings/change-password')}>
          Zmień hasło
        </Button>
      </section>
    </AppLayoutContainer>
  );
}
