import type { FormEvent } from 'react';
import { useState } from 'react';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { useAuth } from '@/features/auth/AuthProvider';
import { getFriendlyAuthError } from '@/services/apiError';
import './LoginPage.css';

interface LoginFormState {
  organizationSlug: string;
  email: string;
  password: string;
}

interface LoginFormErrors {
  organizationSlug?: string;
  email?: string;
  password?: string;
  form?: string;
}

export function LoginPage() {
  const { login } = useAuth();
  const [form, setForm] = useState<LoginFormState>({
    organizationSlug: 'm2-manager-dev',
    email: '',
    password: '',
  });
  const [errors, setErrors] = useState<LoginFormErrors>({});
  const [loading, setLoading] = useState(false);

  const validate = (): LoginFormErrors => {
    const nextErrors: LoginFormErrors = {};

    if (!form.organizationSlug.trim()) {
      nextErrors.organizationSlug = 'Podaj identyfikator organizacji.';
    }

    if (!form.email.trim()) {
      nextErrors.email = 'Podaj adres e-mail.';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
      nextErrors.email = 'Podaj poprawny adres e-mail.';
    }

    if (!form.password) {
      nextErrors.password = 'Podaj hasło.';
    }

    return nextErrors;
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const validationErrors = validate();
    setErrors(validationErrors);

    if (Object.keys(validationErrors).length > 0) {
      return;
    }

    setLoading(true);
    setErrors({});

    try {
      await login({
        organizationSlug: form.organizationSlug.trim(),
        email: form.email.trim(),
        password: form.password,
      });
    } catch (error) {
      setErrors({ form: getFriendlyAuthError(error) });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-page__panel">
        <div className="login-page__brand">
          <span className="login-page__brand-mark">M2</span>
          <div>
            <h1 className="login-page__title">M2 Manager</h1>
            <p className="login-page__subtitle">Zaloguj się do panelu zarządzania</p>
          </div>
        </div>

        <form className="login-page__form" onSubmit={(event) => void handleSubmit(event)} noValidate>
          <Input
            label="Organizacja"
            name="organizationSlug"
            autoComplete="organization"
            value={form.organizationSlug}
            onChange={(event) => setForm((current) => ({ ...current, organizationSlug: event.target.value }))}
            error={errors.organizationSlug}
          />

          <Input
            label="E-mail"
            name="email"
            type="email"
            autoComplete="username"
            value={form.email}
            onChange={(event) => setForm((current) => ({ ...current, email: event.target.value }))}
            error={errors.email}
          />

          <Input
            label="Hasło"
            name="password"
            type="password"
            autoComplete="current-password"
            value={form.password}
            onChange={(event) => setForm((current) => ({ ...current, password: event.target.value }))}
            error={errors.password}
          />

          {errors.form ? <p className="login-page__form-error">{errors.form}</p> : null}

          <Button type="submit" size="lg" loading={loading} className="login-page__submit">
            Zaloguj się
          </Button>
        </form>
      </div>
    </div>
  );
}
