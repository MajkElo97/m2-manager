import { useState, type FormEvent } from 'react';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import type {
  Building,
  BuildingStatus,
  CreateBuildingPayload,
  UpdateBuildingPayload,
} from '@/features/buildings/types/building';
import { parseIsoDate, toIsoDate } from '@/utils/dateFormat';
import './BuildingForm.css';

export interface BuildingFormValues {
  code: string;
  name: string;
  address: string;
  city: string;
  nip: string;
  phone: string;
  email: string;
  managerCode: string;
  supervisorCode: string;
  employeeCode: string;
  contractSignedAt: string;
  serviceStartDate: string;
  noticePeriodMonths: string;
  status: BuildingStatus;
  notes: string;
}

interface BuildingFormProps {
  mode: 'create' | 'edit';
  initialBuilding?: Building;
  submitLabel: string;
  loading?: boolean;
  serverError?: string | null;
  onSubmit: (payload: CreateBuildingPayload | UpdateBuildingPayload) => Promise<void>;
  onCancel: () => void;
}

function emptyFormValues(): BuildingFormValues {
  return {
    code: '',
    name: '',
    address: '',
    city: '',
    nip: '',
    phone: '',
    email: '',
    managerCode: '',
    supervisorCode: '',
    employeeCode: '',
    contractSignedAt: '',
    serviceStartDate: '',
    noticePeriodMonths: '3',
    status: 'ACTIVE',
    notes: '',
  };
}

function toFormValues(building: Building): BuildingFormValues {
  return {
    code: building.code,
    name: building.name,
    address: building.address,
    city: building.city,
    nip: building.nip ?? '',
    phone: building.phone ?? '',
    email: building.email ?? '',
    managerCode: building.managerCode ?? '',
    supervisorCode: building.supervisorCode ?? '',
    employeeCode: building.employeeCode ?? '',
    contractSignedAt: parseIsoDate(building.contractSignedAt),
    serviceStartDate: parseIsoDate(building.serviceStartDate),
    noticePeriodMonths: String(building.noticePeriodMonths),
    status: building.status,
    notes: building.notes ?? '',
  };
}

function validateForm(values: BuildingFormValues): Partial<Record<keyof BuildingFormValues, string>> {
  const errors: Partial<Record<keyof BuildingFormValues, string>> = {};

  if (!values.code.trim()) {
    errors.code = 'Podaj kod budynku.';
  } else if (values.code.length > 100) {
    errors.code = 'Kod może mieć maksymalnie 100 znaków.';
  }

  if (!values.name.trim()) {
    errors.name = 'Podaj nazwę budynku.';
  }

  if (!values.address.trim()) {
    errors.address = 'Podaj adres.';
  }

  if (!values.city.trim()) {
    errors.city = 'Podaj miasto.';
  }

  if (values.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(values.email)) {
    errors.email = 'Podaj prawidłowy adres e-mail.';
  }

  const noticePeriod = Number(values.noticePeriodMonths);
  if (values.noticePeriodMonths.trim() === '' || Number.isNaN(noticePeriod)) {
    errors.noticePeriodMonths = 'Podaj okres wypowiedzenia.';
  } else if (noticePeriod < 0 || noticePeriod > 120) {
    errors.noticePeriodMonths = 'Okres wypowiedzenia musi być od 0 do 120 miesięcy.';
  }

  return errors;
}

function toPayload(values: BuildingFormValues): CreateBuildingPayload {
  return {
    code: values.code.trim(),
    name: values.name.trim(),
    address: values.address.trim(),
    city: values.city.trim(),
    nip: values.nip.trim() || undefined,
    phone: values.phone.trim() || undefined,
    email: values.email.trim() || undefined,
    managerCode: values.managerCode.trim() || undefined,
    supervisorCode: values.supervisorCode.trim() || undefined,
    employeeCode: values.employeeCode.trim() || undefined,
    contractSignedAt: toIsoDate(values.contractSignedAt),
    serviceStartDate: toIsoDate(values.serviceStartDate),
    noticePeriodMonths: Number(values.noticePeriodMonths),
    notes: values.notes.trim() || undefined,
  };
}

export function BuildingForm({
  mode,
  initialBuilding,
  submitLabel,
  loading = false,
  serverError,
  onSubmit,
  onCancel,
}: BuildingFormProps) {
  const [values, setValues] = useState<BuildingFormValues>(
    initialBuilding ? toFormValues(initialBuilding) : emptyFormValues(),
  );
  const [fieldErrors, setFieldErrors] = useState<
    Partial<Record<keyof BuildingFormValues, string>>
  >({});

  const updateField = <K extends keyof BuildingFormValues>(field: K, value: BuildingFormValues[K]) => {
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

  return (
    <form className="building-form" onSubmit={handleSubmit} noValidate>
      <div className="building-form__grid">
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
          label="Adres"
          name="address"
          value={values.address}
          error={fieldErrors.address}
          onChange={(event) => updateField('address', event.target.value)}
          required
        />
        <Input
          label="Miasto"
          name="city"
          value={values.city}
          error={fieldErrors.city}
          onChange={(event) => updateField('city', event.target.value)}
          required
        />
        <Input
          label="NIP"
          name="nip"
          value={values.nip}
          onChange={(event) => updateField('nip', event.target.value)}
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
          error={fieldErrors.email}
          onChange={(event) => updateField('email', event.target.value)}
        />
        <Input
          label="Zarządca"
          name="managerCode"
          value={values.managerCode}
          onChange={(event) => updateField('managerCode', event.target.value)}
        />
        <Input
          label="Opiekun"
          name="supervisorCode"
          value={values.supervisorCode}
          onChange={(event) => updateField('supervisorCode', event.target.value)}
        />
        <Input
          label="Pracownik"
          name="employeeCode"
          value={values.employeeCode}
          onChange={(event) => updateField('employeeCode', event.target.value)}
        />
        <Input
          label="Data podpisania umowy"
          name="contractSignedAt"
          type="date"
          value={values.contractSignedAt}
          onChange={(event) => updateField('contractSignedAt', event.target.value)}
        />
        <Input
          label="Data rozpoczęcia obsługi"
          name="serviceStartDate"
          type="date"
          value={values.serviceStartDate}
          onChange={(event) => updateField('serviceStartDate', event.target.value)}
        />
        <Input
          label="Okres wypowiedzenia (miesiące)"
          name="noticePeriodMonths"
          type="number"
          min={0}
          max={120}
          value={values.noticePeriodMonths}
          error={fieldErrors.noticePeriodMonths}
          onChange={(event) => updateField('noticePeriodMonths', event.target.value)}
          required
        />
        {mode === 'edit' ? (
          <label className="building-form__select-label">
            <span>Status</span>
            <select
              className="building-form__select"
              name="status"
              value={values.status}
              onChange={(event) => updateField('status', event.target.value as BuildingStatus)}
            >
              <option value="ACTIVE">Aktywny</option>
              <option value="INACTIVE">Nieaktywny</option>
            </select>
          </label>
        ) : null}
        <label className="building-form__textarea-label building-form__field--full">
          <span>Uwagi</span>
          <textarea
            className="building-form__textarea"
            name="notes"
            value={values.notes}
            onChange={(event) => updateField('notes', event.target.value)}
          />
        </label>
      </div>

      {serverError ? <p className="building-form__error">{serverError}</p> : null}

      <div className="building-form__actions">
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
