import { useState, type FormEvent } from 'react';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import type {
  CreateStaircasePayload,
  Staircase,
  UpdateStaircasePayload,
} from '@/features/staircases/types/staircase';
import './StaircaseForm.css';

export interface StaircaseFormValues {
  code: string;
  designation: string;
  intercomCode: string;
  keyRequired: boolean;
  elevator: boolean;
  floors: string;
  notes: string;
}

interface StaircaseFormProps {
  mode: 'create' | 'edit';
  initialStaircase?: Staircase;
  submitLabel: string;
  loading?: boolean;
  serverError?: string | null;
  onSubmit: (payload: CreateStaircasePayload | UpdateStaircasePayload) => Promise<void>;
  onCancel: () => void;
}

function emptyFormValues(): StaircaseFormValues {
  return {
    code: '',
    designation: '',
    intercomCode: '',
    keyRequired: false,
    elevator: false,
    floors: '4',
    notes: '',
  };
}

function toFormValues(staircase: Staircase): StaircaseFormValues {
  return {
    code: staircase.code,
    designation: staircase.designation,
    intercomCode: staircase.intercomCode ?? '',
    keyRequired: staircase.keyRequired,
    elevator: staircase.elevator,
    floors: String(staircase.floors),
    notes: staircase.notes ?? '',
  };
}

function validate(values: StaircaseFormValues): Partial<Record<keyof StaircaseFormValues, string>> {
  const errors: Partial<Record<keyof StaircaseFormValues, string>> = {};

  if (!values.code.trim()) {
    errors.code = 'Kod klatki jest wymagany.';
  } else if (values.code.length > 100) {
    errors.code = 'Kod klatki może mieć maksymalnie 100 znaków.';
  }

  if (!values.designation.trim()) {
    errors.designation = 'Oznaczenie jest wymagane.';
  } else if (values.designation.length > 50) {
    errors.designation = 'Oznaczenie może mieć maksymalnie 50 znaków.';
  }

  if (values.intercomCode.length > 255) {
    errors.intercomCode = 'Kod domofonu może mieć maksymalnie 255 znaków.';
  }

  const floors = Number(values.floors);
  if (!values.floors.trim() || Number.isNaN(floors)) {
    errors.floors = 'Kondygnacje są wymagane.';
  } else if (floors < 0 || floors > 200) {
    errors.floors = 'Kondygnacje muszą być w zakresie 0–200.';
  }

  return errors;
}

export function StaircaseForm({
  initialStaircase,
  submitLabel,
  loading = false,
  serverError,
  onSubmit,
  onCancel,
}: StaircaseFormProps) {
  const [values, setValues] = useState<StaircaseFormValues>(
    initialStaircase ? toFormValues(initialStaircase) : emptyFormValues(),
  );
  const [fieldErrors, setFieldErrors] = useState<Partial<Record<keyof StaircaseFormValues, string>>>({});

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const errors = validate(values);
    setFieldErrors(errors);
    if (Object.keys(errors).length > 0) {
      return;
    }

    const payload = {
      code: values.code.trim(),
      designation: values.designation.trim(),
      intercomCode: values.intercomCode.trim() || null,
      keyRequired: values.keyRequired,
      elevator: values.elevator,
      floors: Number(values.floors),
      notes: values.notes.trim() || null,
    };

    await onSubmit(payload);
  };

  return (
    <form className="staircase-form" onSubmit={(event) => void handleSubmit(event)} noValidate>
      <div className="staircase-form__grid">
        <Input
          label="Kod klatki"
          name="code"
          value={values.code}
          onChange={(event) => setValues((current) => ({ ...current, code: event.target.value }))}
          error={fieldErrors.code}
          required
        />
        <Input
          label="Oznaczenie"
          name="designation"
          value={values.designation}
          onChange={(event) => setValues((current) => ({ ...current, designation: event.target.value }))}
          error={fieldErrors.designation}
          required
        />
        <Input
          label="Kod domofonu"
          name="intercomCode"
          value={values.intercomCode}
          onChange={(event) => setValues((current) => ({ ...current, intercomCode: event.target.value }))}
          error={fieldErrors.intercomCode}
        />
        <Input
          label="Kondygnacje"
          name="floors"
          type="number"
          min={0}
          max={200}
          value={values.floors}
          onChange={(event) => setValues((current) => ({ ...current, floors: event.target.value }))}
          error={fieldErrors.floors}
          required
        />
      </div>

      <div className="staircase-form__checkboxes">
        <label className="staircase-form__checkbox">
          <input
            type="checkbox"
            checked={values.keyRequired}
            onChange={(event) => setValues((current) => ({ ...current, keyRequired: event.target.checked }))}
          />
          Klucz wymagany
        </label>
        <label className="staircase-form__checkbox">
          <input
            type="checkbox"
            checked={values.elevator}
            onChange={(event) => setValues((current) => ({ ...current, elevator: event.target.checked }))}
          />
          Winda
        </label>
      </div>

      <label className="staircase-form__textarea-label">
        <span>Uwagi</span>
        <textarea
          className="staircase-form__textarea"
          name="notes"
          rows={3}
          value={values.notes}
          onChange={(event) => setValues((current) => ({ ...current, notes: event.target.value }))}
        />
      </label>

      {serverError ? (
        <p className="staircase-form__error" role="alert">
          {serverError}
        </p>
      ) : null}

      <div className="staircase-form__actions">
        <Button type="button" variant="secondary" onClick={onCancel} disabled={loading}>
          Anuluj
        </Button>
        <Button type="submit" loading={loading}>
          {submitLabel}
        </Button>
      </div>
    </form>
  );
}
