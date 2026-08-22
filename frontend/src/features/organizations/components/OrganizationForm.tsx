import { type FormEvent, useEffect, useState } from 'react';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { suggestSlugFromName, isValidSlug } from '@/features/organizations/utils/slugUtils';
import type { CreateOrganizationPayload, UpdateOrganizationPayload } from '@/features/organizations/types/organization';

interface OrganizationFormProps {
  mode: 'create' | 'edit';
  initialValues?: {
    name: string;
    slug: string;
    adminEmail?: string;
  };
  loading?: boolean;
  onSubmit: (payload: CreateOrganizationPayload | UpdateOrganizationPayload) => Promise<void>;
  onCancel: () => void;
}

export function OrganizationForm({
  mode,
  initialValues,
  loading = false,
  onSubmit,
  onCancel,
}: OrganizationFormProps) {
  const [name, setName] = useState(initialValues?.name ?? '');
  const [slug, setSlug] = useState(initialValues?.slug ?? '');
  const [adminEmail, setAdminEmail] = useState(initialValues?.adminEmail ?? '');
  const [slugTouched, setSlugTouched] = useState(mode === 'edit');
  const [errors, setErrors] = useState<Record<string, string>>({});

  useEffect(() => {
    if (mode === 'create' && !slugTouched) {
      setSlug(suggestSlugFromName(name));
    }
  }, [name, mode, slugTouched]);

  const validate = (): boolean => {
    const nextErrors: Record<string, string> = {};

    if (!name.trim()) {
      nextErrors.name = 'Nazwa organizacji jest wymagana.';
    }

    if (!slug.trim()) {
      nextErrors.slug = 'Slug jest wymagany.';
    } else if (!isValidSlug(slug.trim())) {
      nextErrors.slug = 'Slug może zawierać małe litery, cyfry i myślniki.';
    }

    if (mode === 'create' && !adminEmail.trim()) {
      nextErrors.adminEmail = 'Login administratora jest wymagany.';
    }

    setErrors(nextErrors);
    return Object.keys(nextErrors).length === 0;
  };

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    if (!validate()) {
      return;
    }

    if (mode === 'create') {
      await onSubmit({
        name: name.trim(),
        slug: slug.trim().toLowerCase(),
        adminEmail: adminEmail.trim().toLowerCase(),
      });
      return;
    }

    await onSubmit({
      name: name.trim(),
      slug: slug.trim().toLowerCase(),
    });
  };

  return (
    <form className="organization-form" onSubmit={(event) => void handleSubmit(event)}>
      <section className="organization-form__section">
        <h3 className="organization-form__section-title">Organizacja</h3>
        <Input
          label="Nazwa organizacji *"
          value={name}
          onChange={(event) => setName(event.target.value)}
          error={errors.name}
          disabled={loading}
        />
        <Input
          label="Slug *"
          value={slug}
          onChange={(event) => {
            setSlugTouched(true);
            setSlug(event.target.value.toLowerCase());
          }}
          error={errors.slug}
          disabled={loading}
        />
        <p className="organization-form__hint">Małe litery, cyfry i myślniki, np. m2-group</p>
      </section>

      {mode === 'create' ? (
        <section className="organization-form__section">
          <h3 className="organization-form__section-title">Konto administratora</h3>
          <Input
            label="Login / e-mail *"
            type="email"
            value={adminEmail}
            onChange={(event) => setAdminEmail(event.target.value)}
            error={errors.adminEmail}
            disabled={loading}
          />
          <p className="organization-form__hint">
            Hasło zostanie wygenerowane automatycznie i pokazane tylko raz po utworzeniu organizacji.
          </p>
        </section>
      ) : null}

      <div className="organization-form__actions">
        <Button type="button" variant="secondary" onClick={onCancel} disabled={loading}>
          Anuluj
        </Button>
        <Button type="submit" disabled={loading}>
          {mode === 'create' ? 'Utwórz organizację' : 'Zapisz zmiany'}
        </Button>
      </div>
    </form>
  );
}
