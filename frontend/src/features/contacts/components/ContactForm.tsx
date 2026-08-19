import { useState, type FormEvent } from 'react';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import type { Building } from '@/features/buildings/types/building';
import type {
  Contact,
  CreateContactPayload,
  UpdateContactPayload,
} from '@/features/contacts/types/contact';
import './ContactForm.css';

export interface ContactFormValues {
  buildingId: string;
  firstName: string;
  lastName: string;
  functionTitle: string;
  phone: string;
  email: string;
  notes: string;
  active: boolean;
}

interface ContactFormProps {
  mode: 'create' | 'edit';
  initialContact?: Contact;
  buildings: Building[];
  fixedBuildingId?: string;
  submitLabel: string;
  loading?: boolean;
  serverError?: string | null;
  onSubmit: (payload: CreateContactPayload | UpdateContactPayload) => Promise<void>;
  onCancel: () => void;
}

function emptyFormValues(buildings: Building[], fixedBuildingId?: string): ContactFormValues {
  return {
    buildingId: fixedBuildingId ?? buildings[0]?.id ?? '',
    firstName: '',
    lastName: '',
    functionTitle: '',
    phone: '',
    email: '',
    notes: '',
    active: true,
  };
}

function toFormValues(contact: Contact): ContactFormValues {
  return {
    buildingId: contact.buildingId,
    firstName: contact.firstName ?? '',
    lastName: contact.lastName ?? '',
    functionTitle: contact.functionTitle ?? '',
    phone: contact.phone ?? '',
    email: contact.email ?? '',
    notes: contact.notes ?? '',
    active: contact.active,
  };
}

function validateForm(values: ContactFormValues): Partial<Record<keyof ContactFormValues, string>> {
  const errors: Partial<Record<keyof ContactFormValues, string>> = {};

  if (!values.buildingId) {
    errors.buildingId = 'Wybierz budynek.';
  }

  return errors;
}

function toPayload(values: ContactFormValues): CreateContactPayload {
  return {
    buildingId: values.buildingId,
    firstName: values.firstName.trim() || undefined,
    lastName: values.lastName.trim() || undefined,
    functionTitle: values.functionTitle.trim() || undefined,
    phone: values.phone.trim() || undefined,
    email: values.email.trim() || undefined,
    notes: values.notes.trim() || undefined,
  };
}

export function ContactForm({
  mode,
  initialContact,
  buildings,
  fixedBuildingId,
  submitLabel,
  loading = false,
  serverError,
  onSubmit,
  onCancel,
}: ContactFormProps) {
  const [values, setValues] = useState<ContactFormValues>(
    initialContact
      ? toFormValues(initialContact)
      : emptyFormValues(buildings, fixedBuildingId),
  );
  const [fieldErrors, setFieldErrors] = useState<
    Partial<Record<keyof ContactFormValues, string>>
  >({});

  const updateField = <K extends keyof ContactFormValues>(field: K, value: ContactFormValues[K]) => {
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
    <form className="contact-form" onSubmit={handleSubmit} noValidate>
      <div className="contact-form__grid">
        {!fixedBuildingId ? (
          <label className="contact-form__select-label contact-form__field--full">
            <span>Budynek</span>
            <select
              className="contact-form__select"
              name="buildingId"
              value={values.buildingId}
              onChange={(event) => updateField('buildingId', event.target.value)}
            >
              <option value="">Wybierz budynek…</option>
              {buildings.map((building) => (
                <option key={building.id} value={building.id}>
                  {building.name} ({building.code})
                </option>
              ))}
            </select>
            {fieldErrors.buildingId ? (
              <span className="contact-form__error">{fieldErrors.buildingId}</span>
            ) : null}
          </label>
        ) : null}
        <Input
          label="Imię"
          name="firstName"
          value={values.firstName}
          onChange={(event) => updateField('firstName', event.target.value)}
        />
        <Input
          label="Nazwisko"
          name="lastName"
          value={values.lastName}
          onChange={(event) => updateField('lastName', event.target.value)}
        />
        <Input
          label="Funkcja"
          name="functionTitle"
          value={values.functionTitle}
          onChange={(event) => updateField('functionTitle', event.target.value)}
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
          className="contact-form__field--full"
          onChange={(event) => updateField('notes', event.target.value)}
        />
        {mode === 'edit' ? (
          <label className="contact-form__select-label">
            <span>Status</span>
            <select
              className="contact-form__select"
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

      {serverError ? <p className="contact-form__error">{serverError}</p> : null}

      <div className="contact-form__actions">
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
