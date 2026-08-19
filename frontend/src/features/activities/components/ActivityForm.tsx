import { useState, type FormEvent } from 'react';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import type {
  Activity,
  ActivityPlanningType,
  ActivityPriority,
  CreateActivityPayload,
  UpdateActivityPayload,
} from '@/features/activities/types/activity';
import './ActivityForm.css';

export interface ActivityFormValues {
  code: string;
  name: string;
  category: string;
  planningType: ActivityPlanningType;
  defaultPeriod: string;
  durationMinutes: string;
  priority: ActivityPriority;
  active: boolean;
}

interface ActivityFormProps {
  mode: 'create' | 'edit';
  initialActivity?: Activity;
  submitLabel: string;
  loading?: boolean;
  serverError?: string | null;
  onSubmit: (payload: CreateActivityPayload | UpdateActivityPayload) => Promise<void>;
  onCancel: () => void;
}

function emptyFormValues(): ActivityFormValues {
  return {
    code: '',
    name: '',
    category: '',
    planningType: 'CYCLIC',
    defaultPeriod: '',
    durationMinutes: '',
    priority: 'NORMAL',
    active: true,
  };
}

function toFormValues(activity: Activity): ActivityFormValues {
  return {
    code: activity.code,
    name: activity.name,
    category: activity.category,
    planningType: activity.planningType,
    defaultPeriod: activity.defaultPeriod ?? '',
    durationMinutes: activity.durationMinutes != null ? String(activity.durationMinutes) : '',
    priority: activity.priority,
    active: activity.active,
  };
}

function validateForm(values: ActivityFormValues): Partial<Record<keyof ActivityFormValues, string>> {
  const errors: Partial<Record<keyof ActivityFormValues, string>> = {};

  if (!values.code.trim()) {
    errors.code = 'Podaj kod czynności.';
  } else if (values.code.length > 100) {
    errors.code = 'Kod może mieć maksymalnie 100 znaków.';
  }

  if (!values.name.trim()) {
    errors.name = 'Podaj nazwę czynności.';
  }

  if (!values.category.trim()) {
    errors.category = 'Podaj kategorię.';
  }

  if (values.durationMinutes.trim()) {
    const duration = Number(values.durationMinutes);
    if (Number.isNaN(duration) || duration < 0) {
      errors.durationMinutes = 'Czas trwania musi być liczbą nieujemną.';
    }
  }

  return errors;
}

function toPayload(values: ActivityFormValues): CreateActivityPayload {
  return {
    code: values.code.trim(),
    name: values.name.trim(),
    category: values.category.trim(),
    planningType: values.planningType,
    defaultPeriod: values.defaultPeriod.trim() || undefined,
    durationMinutes: values.durationMinutes.trim()
      ? Number(values.durationMinutes)
      : undefined,
    priority: values.priority,
  };
}

export function ActivityForm({
  mode,
  initialActivity,
  submitLabel,
  loading = false,
  serverError,
  onSubmit,
  onCancel,
}: ActivityFormProps) {
  const [values, setValues] = useState<ActivityFormValues>(
    initialActivity ? toFormValues(initialActivity) : emptyFormValues(),
  );
  const [fieldErrors, setFieldErrors] = useState<
    Partial<Record<keyof ActivityFormValues, string>>
  >({});

  const updateField = <K extends keyof ActivityFormValues>(field: K, value: ActivityFormValues[K]) => {
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
    <form className="activity-form" onSubmit={handleSubmit} noValidate>
      <div className="activity-form__grid">
        <Input
          label="Kod"
          name="code"
          value={values.code}
          error={fieldErrors.code}
          onChange={(event) => updateField('code', event.target.value)}
          required
        />
        <Input
          label="Nazwa"
          name="name"
          value={values.name}
          error={fieldErrors.name}
          onChange={(event) => updateField('name', event.target.value)}
          required
        />
        <Input
          label="Kategoria"
          name="category"
          value={values.category}
          error={fieldErrors.category}
          onChange={(event) => updateField('category', event.target.value)}
          required
        />
        <label className="activity-form__select-label">
          <span>Typ planowania</span>
          <select
            className="activity-form__select"
            name="planningType"
            value={values.planningType}
            onChange={(event) =>
              updateField('planningType', event.target.value as ActivityPlanningType)
            }
          >
            <option value="CYCLIC">Cykliczna</option>
            <option value="PERIODIC">Okresowa</option>
            <option value="ON_DEMAND">Na żądanie</option>
          </select>
        </label>
        <Input
          label="Domyślny okres"
          name="defaultPeriod"
          value={values.defaultPeriod}
          onChange={(event) => updateField('defaultPeriod', event.target.value)}
        />
        <Input
          label="Czas trwania (minuty)"
          name="durationMinutes"
          type="number"
          min={0}
          value={values.durationMinutes}
          error={fieldErrors.durationMinutes}
          onChange={(event) => updateField('durationMinutes', event.target.value)}
        />
        <label className="activity-form__select-label">
          <span>Priorytet</span>
          <select
            className="activity-form__select"
            name="priority"
            value={values.priority}
            onChange={(event) => updateField('priority', event.target.value as ActivityPriority)}
          >
            <option value="LOW">Niski</option>
            <option value="NORMAL">Normalny</option>
            <option value="HIGH">Wysoki</option>
          </select>
        </label>
        {mode === 'edit' ? (
          <label className="activity-form__select-label">
            <span>Status</span>
            <select
              className="activity-form__select"
              name="active"
              value={values.active ? 'true' : 'false'}
              onChange={(event) => updateField('active', event.target.value === 'true')}
            >
              <option value="true">Aktywna</option>
              <option value="false">Nieaktywna</option>
            </select>
          </label>
        ) : null}
      </div>

      {serverError ? <p className="activity-form__error">{serverError}</p> : null}

      <div className="activity-form__actions">
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
