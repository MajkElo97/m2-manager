import { useState, type FormEvent } from 'react';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import type {
  CreateManagerPayload,
  Manager,
  UpdateManagerPayload,
} from '@/features/managers/types/manager';
import './ManagerForm.css';

export interface ManagerFormValues {
  code: string;
  name: string;
  phone: string;
  email: string;
  address: string;
  notes: string;
}

interface ManagerFormProps {
  mode: 'create' | 'edit';
  initialManager?: Manager;
  submitLabel: string;
  loading?: boolean;
  serverError?: string | null;
  onSubmit: (payload: CreateManagerPayload | UpdateManagerPayload) => Promise<void>;
  onCancel: () => void;
}

function emptyFormValues(): ManagerFormValues {
  return {
    code: '',
    name: '',
    phone: '',
    email: '',
    address: '',
    notes: '',
  };
}

function toFormValues(manager: Manager): ManagerFormValues {
  return {
    code: manager.code,
    name: manager.name,
    phone: manager.phone ?? '',
    email: manager.email ?? '',
    address: manager.address ?? '',
    notes: manager.notes ?? '',
  };
}

function validateForm(values: ManagerFormValues): Partial<Record<keyof ManagerFormValues, string>> {
  const errors: Partial<Record<keyof ManagerFormValues, string>> = {};

  if (!values.code.trim()) {
    errors.code = 'Podaj kod zarządcy.';
  }

  if (!values.name.trim()) {
    errors.name = 'Podaj nazwę firmy.';
  }

  return errors;
}

function toPayload(values: ManagerFormValues): CreateManagerPayload {
  return {
    code: values.code.trim(),
    name: values.name.trim(),
    phone: values.phone.trim() || undefined,
    email: values.email.trim() || undefined,
    address: values.address.trim() || undefined,
    notes: values.notes.trim() || undefined,
  };
}

export function ManagerForm({
  mode: _mode,
  initialManager,
  submitLabel,
  loading = false,
  serverError,
  onSubmit,
  onCancel,
}: ManagerFormProps) {
  const [values, setValues] = useState<ManagerFormValues>(
    initialManager ? toFormValues(initialManager) : emptyFormValues(),
  );
  const [fieldErrors, setFieldErrors] = useState<
    Partial<Record<keyof ManagerFormValues, string>>
  >({});

  const updateField = <K extends keyof ManagerFormValues>(field: K, value: ManagerFormValues[K]) => {
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

    await onSubmit(toPayload(values));
  };

  return (
    <form className="manager-form" onSubmit={handleSubmit} noValidate>
      <div className="manager-form__grid">
        <Input
          label="Kod"
          name="code"
          value={values.code}
          error={fieldErrors.code}
          onChange={(event) => updateField('code', event.target.value)}
          required
        />
        <Input
          label="Nazwa firmy"
          name="name"
          value={values.name}
          error={fieldErrors.name}
          onChange={(event) => updateField('name', event.target.value)}
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
          label="Adres"
          name="address"
          value={values.address}
          className="manager-form__field--full"
          onChange={(event) => updateField('address', event.target.value)}
        />
        <Input
          label="Uwagi"
          name="notes"
          value={values.notes}
          className="manager-form__field--full"
          onChange={(event) => updateField('notes', event.target.value)}
        />
      </div>

      {serverError ? <p className="manager-form__error">{serverError}</p> : null}

      <div className="manager-form__actions">
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
