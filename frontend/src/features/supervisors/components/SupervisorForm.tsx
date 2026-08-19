import { useState, type FormEvent } from 'react';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import type { Manager } from '@/features/managers/types/manager';
import type {
  CreateSupervisorPayload,
  Supervisor,
  UpdateSupervisorPayload,
} from '@/features/supervisors/types/supervisor';
import './SupervisorForm.css';

export interface SupervisorFormValues {
  managerId: string;
  code: string;
  firstName: string;
  lastName: string;
  phone: string;
  email: string;
  notes: string;
  active: boolean;
}

interface SupervisorFormProps {
  mode: 'create' | 'edit';
  initialSupervisor?: Supervisor;
  managers: Manager[];
  submitLabel: string;
  loading?: boolean;
  serverError?: string | null;
  onSubmit: (payload: CreateSupervisorPayload | UpdateSupervisorPayload) => Promise<void>;
  onCancel: () => void;
}

function emptyFormValues(managers: Manager[]): SupervisorFormValues {
  return {
    managerId: managers[0]?.id ?? '',
    code: '',
    firstName: '',
    lastName: '',
    phone: '',
    email: '',
    notes: '',
    active: true,
  };
}

function toFormValues(supervisor: Supervisor): SupervisorFormValues {
  return {
    managerId: supervisor.managerId,
    code: supervisor.code,
    firstName: supervisor.firstName,
    lastName: supervisor.lastName,
    phone: supervisor.phone ?? '',
    email: supervisor.email ?? '',
    notes: supervisor.notes ?? '',
    active: supervisor.active,
  };
}

function validateForm(values: SupervisorFormValues): Partial<Record<keyof SupervisorFormValues, string>> {
  const errors: Partial<Record<keyof SupervisorFormValues, string>> = {};

  if (!values.managerId) {
    errors.managerId = 'Wybierz zarządcę.';
  }

  if (!values.code.trim()) {
    errors.code = 'Podaj kod opiekuna.';
  }

  if (!values.firstName.trim()) {
    errors.firstName = 'Podaj imię.';
  }

  if (!values.lastName.trim()) {
    errors.lastName = 'Podaj nazwisko.';
  }

  return errors;
}

function toPayload(values: SupervisorFormValues): CreateSupervisorPayload {
  return {
    managerId: values.managerId,
    code: values.code.trim(),
    firstName: values.firstName.trim(),
    lastName: values.lastName.trim(),
    phone: values.phone.trim() || undefined,
    email: values.email.trim() || undefined,
    notes: values.notes.trim() || undefined,
  };
}

export function SupervisorForm({
  mode,
  initialSupervisor,
  managers,
  submitLabel,
  loading = false,
  serverError,
  onSubmit,
  onCancel,
}: SupervisorFormProps) {
  const [values, setValues] = useState<SupervisorFormValues>(
    initialSupervisor ? toFormValues(initialSupervisor) : emptyFormValues(managers),
  );
  const [fieldErrors, setFieldErrors] = useState<
    Partial<Record<keyof SupervisorFormValues, string>>
  >({});

  const updateField = <K extends keyof SupervisorFormValues>(
    field: K,
    value: SupervisorFormValues[K],
  ) => {
    setValues((current) => ({ ...current, [field]: value }));
    setFieldErrors((current) => ({ ...current, [field]: undefined }));
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const errors = validateForm(values);
    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      return;
    }

    const payload = toPayload(values);
    if (mode === 'edit') {
      await onSubmit({ ...payload, active: values.active });
    } else {
      await onSubmit(payload);
    }
  };

  return (
    <form className="supervisor-form" onSubmit={handleSubmit} noValidate>
      <div className="supervisor-form__grid">
        <label className="supervisor-form__select-label supervisor-form__field--full">
          <span>Zarządca</span>
          <select
            className="supervisor-form__select"
            name="managerId"
            value={values.managerId}
            onChange={(event) => updateField('managerId', event.target.value)}
          >
            <option value="">Wybierz zarządcę…</option>
            {managers.map((manager) => (
              <option key={manager.id} value={manager.id}>
                {manager.name} ({manager.code})
              </option>
            ))}
          </select>
          {fieldErrors.managerId ? (
            <span className="supervisor-form__error">{fieldErrors.managerId}</span>
          ) : null}
        </label>
        <Input
          label="Kod"
          name="code"
          value={values.code}
          error={fieldErrors.code}
          onChange={(event) => updateField('code', event.target.value)}
          required
        />
        <Input
          label="Imię"
          name="firstName"
          value={values.firstName}
          error={fieldErrors.firstName}
          onChange={(event) => updateField('firstName', event.target.value)}
          required
        />
        <Input
          label="Nazwisko"
          name="lastName"
          value={values.lastName}
          error={fieldErrors.lastName}
          onChange={(event) => updateField('lastName', event.target.value)}
          required
        />
        <Input
          label="Telefon"
          name="phone"
          value={values.phone}
          onChange={(event) => updateField('phone', event.target.value)}
        />
        <Input
          label="E-mail"
          name="email"
          type="email"
          value={values.email}
          onChange={(event) => updateField('email', event.target.value)}
        />
        <Input
          label="Uwagi"
          name="notes"
          value={values.notes}
          className="supervisor-form__field--full"
          onChange={(event) => updateField('notes', event.target.value)}
        />
        {mode === 'edit' ? (
          <label className="supervisor-form__select-label">
            <span>Status</span>
            <select
              className="supervisor-form__select"
              name="active"
              value={values.active ? 'true' : 'false'}
              onChange={(event) => updateField('active', event.target.value === 'true')}
            >
              <option value="true">Aktywny</option>
              <option value="false">Nieaktywny</option>
            </select>
          </label>
        ) : null}
      </div>

      {serverError ? <p className="supervisor-form__error">{serverError}</p> : null}

      <div className="supervisor-form__actions">
        <Button type="button" variant="secondary" onClick={onCancel}>
          Anuluj
        </Button>
        <Button type="submit" loading={loading}>
          {submitLabel}
        </Button>
      </div>
    </form>
  );
}
