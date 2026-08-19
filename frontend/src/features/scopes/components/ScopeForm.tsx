import { useState, type FormEvent } from 'react';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import type { Activity } from '@/features/activities/types/activity';
import type { Building } from '@/features/buildings/types/building';
import type {
  CreateScopePayload,
  Scope,
  ScopePlanningType,
  ScopeStatus,
  UpdateScopePayload,
} from '@/features/scopes/types/scope';
import './ScopeForm.css';

export interface ScopeFormValues {
  code: string;
  buildingId: string;
  activityId: string;
  planningType: ScopePlanningType;
  frequency: string;
  weekdays: string;
  notes: string;
  status: ScopeStatus;
}

interface ScopeFormProps {
  mode: 'create' | 'edit';
  initialScope?: Scope;
  buildings: Building[];
  activities: Activity[];
  fixedBuildingId?: string;
  submitLabel: string;
  loading?: boolean;
  serverError?: string | null;
  onSubmit: (payload: CreateScopePayload | UpdateScopePayload) => Promise<void>;
  onCancel: () => void;
}

function emptyFormValues(fixedBuildingId?: string): ScopeFormValues {
  return {
    code: '',
    buildingId: fixedBuildingId ?? '',
    activityId: '',
    planningType: 'WEEKLY',
    frequency: '',
    weekdays: '',
    notes: '',
    status: 'ACTIVE',
  };
}

function toFormValues(scope: Scope): ScopeFormValues {
  return {
    code: scope.code,
    buildingId: scope.buildingId,
    activityId: scope.activityId,
    planningType: scope.planningType,
    frequency: scope.frequency != null ? String(scope.frequency) : '',
    weekdays: scope.weekdays ?? '',
    notes: scope.notes ?? '',
    status: scope.status,
  };
}

function validateForm(values: ScopeFormValues): Partial<Record<keyof ScopeFormValues, string>> {
  const errors: Partial<Record<keyof ScopeFormValues, string>> = {};

  if (!values.code.trim()) {
    errors.code = 'Podaj kod zakresu.';
  } else if (values.code.length > 100) {
    errors.code = 'Kod może mieć maksymalnie 100 znaków.';
  }

  if (!values.buildingId) {
    errors.buildingId = 'Wybierz budynek.';
  }

  if (!values.activityId) {
    errors.activityId = 'Wybierz czynność.';
  }

  if (values.frequency.trim()) {
    const frequency = Number(values.frequency);
    if (Number.isNaN(frequency) || frequency < 0) {
      errors.frequency = 'Częstotliwość musi być liczbą nieujemną.';
    }
  }

  return errors;
}

function toPayload(values: ScopeFormValues): CreateScopePayload {
  return {
    code: values.code.trim(),
    buildingId: values.buildingId,
    activityId: values.activityId,
    planningType: values.planningType,
    frequency: values.frequency.trim() ? Number(values.frequency) : undefined,
    weekdays: values.weekdays.trim() || undefined,
    notes: values.notes.trim() || undefined,
  };
}

export function ScopeForm({
  mode,
  initialScope,
  buildings,
  activities,
  fixedBuildingId,
  submitLabel,
  loading = false,
  serverError,
  onSubmit,
  onCancel,
}: ScopeFormProps) {
  const [values, setValues] = useState<ScopeFormValues>(
    initialScope ? toFormValues(initialScope) : emptyFormValues(fixedBuildingId),
  );
  const [fieldErrors, setFieldErrors] = useState<
    Partial<Record<keyof ScopeFormValues, string>>
  >({});

  const updateField = <K extends keyof ScopeFormValues>(field: K, value: ScopeFormValues[K]) => {
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
      await onSubmit({ ...payload, status: values.status });
    } else {
      await onSubmit(payload);
    }
  };

  const showBuildingSelect = !fixedBuildingId;

  return (
    <form className="scope-form" onSubmit={handleSubmit} noValidate>
      <div className="scope-form__grid">
        <Input
          label="Kod"
          name="code"
          value={values.code}
          error={fieldErrors.code}
          onChange={(event) => updateField('code', event.target.value)}
          required
        />
        {showBuildingSelect ? (
          <label className="scope-form__select-label">
            <span>Budynek</span>
            <select
              className="scope-form__select"
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
              <span className="scope-form__error">{fieldErrors.buildingId}</span>
            ) : null}
          </label>
        ) : null}
        <label className="scope-form__select-label">
          <span>Czynność</span>
          <select
            className="scope-form__select"
            name="activityId"
            value={values.activityId}
            onChange={(event) => updateField('activityId', event.target.value)}
          >
            <option value="">Wybierz czynność…</option>
            {activities.map((activity) => (
              <option key={activity.id} value={activity.id}>
                {activity.name} ({activity.code})
              </option>
            ))}
          </select>
          {fieldErrors.activityId ? (
            <span className="scope-form__error">{fieldErrors.activityId}</span>
          ) : null}
        </label>
        <label className="scope-form__select-label">
          <span>Typ planowania</span>
          <select
            className="scope-form__select"
            name="planningType"
            value={values.planningType}
            onChange={(event) =>
              updateField('planningType', event.target.value as ScopePlanningType)
            }
          >
            <option value="WEEKLY">Tygodniowy</option>
            <option value="MONTHLY">Miesięczny</option>
            <option value="YEARLY">Roczny</option>
            <option value="EVENT">Zdarzeniowy</option>
          </select>
        </label>
        <Input
          label="Częstotliwość"
          name="frequency"
          type="number"
          min={0}
          value={values.frequency}
          error={fieldErrors.frequency}
          onChange={(event) => updateField('frequency', event.target.value)}
        />
        <Input
          label="Dzień/dni"
          name="weekdays"
          value={values.weekdays}
          onChange={(event) => updateField('weekdays', event.target.value)}
        />
        {mode === 'edit' ? (
          <label className="scope-form__select-label">
            <span>Status</span>
            <select
              className="scope-form__select"
              name="status"
              value={values.status}
              onChange={(event) => updateField('status', event.target.value as ScopeStatus)}
            >
              <option value="ACTIVE">Aktywny</option>
              <option value="INACTIVE">Nieaktywny</option>
            </select>
          </label>
        ) : null}
        <label className="scope-form__textarea-label scope-form__field--full">
          <span>Uwagi</span>
          <textarea
            className="scope-form__textarea"
            name="notes"
            value={values.notes}
            onChange={(event) => updateField('notes', event.target.value)}
          />
        </label>
      </div>

      {serverError ? <p className="scope-form__error">{serverError}</p> : null}

      <div className="scope-form__actions">
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
