import { type FormEvent, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { authService } from '@/features/auth/authService';
import { useAuth } from '@/features/auth/AuthProvider';
import { AppLayoutContainer } from '@/components/layout/AppLayout';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { PageHeader } from '@/components/ui/PageHeader';
import { ApiError } from '@/services/apiError';
import './ChangePasswordPage.css';

export function ChangePasswordPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { context } = useAuth();
  const fromSettings = location.pathname === '/settings/change-password';
  const forced = !fromSettings && (context?.mustChangePassword ?? false);
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);

    if (newPassword !== confirmPassword) {
      setError('Nowe hasło i potwierdzenie muszą być identyczne.');
      return;
    }

    setLoading(true);
    try {
      await authService.changePassword({
        currentPassword,
        newPassword,
        confirmPassword,
      });

      if (forced) {
        window.location.assign('/dashboard');
        return;
      }

      await authService.logout();
      navigate('/login', { replace: true });
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Nie udało się zmienić hasła.');
    } finally {
      setLoading(false);
    }
  };

  const content = (
    <>
      <PageHeader
        title={forced ? 'Ustaw nowe hasło' : 'Zmień hasło'}
        description={
          forced
            ? 'To pierwsze logowanie. Ustaw własne hasło, aby kontynuować korzystanie z aplikacji.'
            : 'Zmień hasło do swojego konta.'
        }
      />

      <form className="change-password-form" onSubmit={(event) => void handleSubmit(event)}>
        <Input
          label="Aktualne hasło *"
          type="password"
          value={currentPassword}
          onChange={(event) => setCurrentPassword(event.target.value)}
          disabled={loading}
          autoComplete="current-password"
        />
        <Input
          label="Nowe hasło *"
          type="password"
          value={newPassword}
          onChange={(event) => setNewPassword(event.target.value)}
          disabled={loading}
          autoComplete="new-password"
        />
        <Input
          label="Powtórz nowe hasło *"
          type="password"
          value={confirmPassword}
          onChange={(event) => setConfirmPassword(event.target.value)}
          disabled={loading}
          autoComplete="new-password"
        />

        {error ? <p role="alert">{error}</p> : null}

        <div className="change-password-form__actions">
          <Button type="submit" disabled={loading}>
            Ustaw nowe hasło
          </Button>
        </div>
      </form>
    </>
  );

  if (fromSettings) {
    return <AppLayoutContainer>{content}</AppLayoutContainer>;
  }

  return (
    <div className="change-password-page">
      <div className="change-password-page__card">{content}</div>
    </div>
  );
}
