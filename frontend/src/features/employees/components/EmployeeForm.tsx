import { useState, type FormEvent } from 'react';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import type {
  CreateEmployeePayload,
  Employee,
  EmployeeRole,
  EmploymentType,
  RemunerationUnit,
  UpdateEmployeePayload,
} from '@/features/employees/types/employee';
import { parseIsoDate, toIsoDate } from '@/utils/dateFormat';
import './EmployeeForm.css';

export interface EmployeeFormValues {
  code: string;
  firstName: string;
  lastName: string;
  phone: string;
  email: string;
  googleEmail: string;
  position: string;
  role: EmployeeRole;
  employmentType: EmploymentType | '';
  employmentStartDate: string;
  remunerationAmount: string;
  remunerationUnit: RemunerationUnit | '';
  remunerationNet: 'true' | 'false' | '';
  calendarColor: string;
  notes: string;
  active: boolean;
}

interface EmployeeFormProps {
  mode: 'create' | 'edit';
  initialEmployee?: Employee;
  submitLabel: string;
  loading?: boolean;
  serverError?: string | null;
  onSubmit: (payload: CreateEmployeePayload | UpdateEmployeePayload) => Promise<void>;
  onCancel: () => void;
}

function emptyFormValues(): EmployeeFormValues {
  return {
    code: '',
    firstName: '',
    lastName: '',
    phone: '',
    email: '',
    googleEmail: '',
    position: '',
    role: 'PRACOWNIK',
    employmentType: '',
    employmentStartDate: '',
    remunerationAmount: '',
    remunerationUnit: '',
    remunerationNet: '',
    calendarColor: '',
    notes: '',
    active: true,
  };
}

function toFormValues(employee: Employee): EmployeeFormValues {
  return {
    code: employee.code,
    firstName: employee.firstName,
    lastName: employee.lastName ?? '',
    phone: employee.phone ?? '',
    email: employee.email ?? '',
    googleEmail: employee.googleEmail ?? '',
    position: employee.position ?? '',
    role: employee.role,
    employmentType: employee.employmentType ?? '',
    employmentStartDate: parseIsoDate(employee.employmentStartDate),
    remunerationAmount:
      employee.remunerationAmount != null ? String(employee.remunerationAmount) : '',
    remunerationUnit: employee.remunerationUnit ?? '',
    remunerationNet:
      employee.remunerationNet === true
        ? 'true'
        : employee.remunerationNet === false
          ? 'false'
          : '',
    calendarColor: employee.calendarColor ?? '',
    notes: employee.notes ?? '',
    active: employee.active,
  };
}

function validateForm(values: EmployeeFormValues): Partial<Record<keyof EmployeeFormValues, string>> {
  const errors: Partial<Record<keyof EmployeeFormValues, string>> = {};

  if (!values.code.trim()) {
    errors.code = 'Podaj kod pracownika.';
  }

  if (!values.firstName.trim()) {
    errors.firstName = 'Podaj imię.';
  }

  if (values.calendarColor.trim() && !/^#[0-9A-Fa-f]{6}$/.test(values.calendarColor.trim())) {
    errors.calendarColor = 'Kolor musi być w formacie #RRGGBB.';
  }

  if (values.remunerationAmount.trim()) {
    const amount = Number(values.remunerationAmount);
    if (Number.isNaN(amount) || amount < 0) {
      errors.remunerationAmount = 'Wynagrodzenie musi być liczbą nieujemną.';
    }
  }

  return errors;
}

function toPayload(values: EmployeeFormValues): CreateEmployeePayload {
  return {
    code: values.code.trim(),
    firstName: values.firstName.trim(),
    lastName: values.lastName.trim() || undefined,
    phone: values.phone.trim() || undefined,
    email: values.email.trim() || undefined,
    googleEmail: values.googleEmail.trim() || undefined,
    position: values.position.trim() || undefined,
    role: values.role,
    employmentType: values.employmentType || null,
    employmentStartDate: toIsoDate(values.employmentStartDate),
    remunerationAmount: values.remunerationAmount.trim()
      ? Number(values.remunerationAmount)
      : null,
    remunerationUnit: values.remunerationUnit || null,
    remunerationNet:
      values.remunerationNet === 'true'
        ? true
        : values.remunerationNet === 'false'
          ? false
          : null,
    calendarColor: values.calendarColor.trim() || undefined,
    notes: values.notes.trim() || undefined,
  };
}

export function EmployeeForm({
  mode,
  initialEmployee,
  submitLabel,
  loading = false,
  serverError,
  onSubmit,
  onCancel,
}: EmployeeFormProps) {
  const [values, setValues] = useState<EmployeeFormValues>(
    initialEmployee ? toFormValues(initialEmployee) : emptyFormValues(),
  );
  const [fieldErrors, setFieldErrors] = useState<
    Partial<Record<keyof EmployeeFormValues, string>>
  >({});

  const updateField = <K extends keyof EmployeeFormValues>(field: K, value: EmployeeFormValues[K]) => {
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
    <form className="employee-form" onSubmit={handleSubmit} noValidate>
      <div className="employee-form__grid">
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
          onChange={(event) => updateField('lastName', event.target.value)}
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
          label="E-mail Google"
          name="googleEmail"
          type="email"
          value={values.googleEmail}
          onChange={(event) => updateField('googleEmail', event.target.value)}
        />
        <Input
          label="Stanowisko"
          name="position"
          value={values.position}
          onChange={(event) => updateField('position', event.target.value)}
        />
        <label className="employee-form__select-label">
          <span>Rola</span>
          <select
            className="employee-form__select"
            name="role"
            value={values.role}
            onChange={(event) => updateField('role', event.target.value as EmployeeRole)}
          >
            <option value="PRACOWNIK">Pracownik</option>
            <option value="ADMIN">Admin</option>
          </select>
        </label>
        <label className="employee-form__select-label">
          <span>Forma zatrudnienia</span>
          <select
            className="employee-form__select"
            name="employmentType"
            value={values.employmentType}
            onChange={(event) =>
              updateField('employmentType', event.target.value as EmploymentType | '')
            }
          >
            <option value="">—</option>
            <option value="ZLECENIE">Zlecenie</option>
          </select>
        </label>
        <Input
          label="Data zatrudnienia"
          name="employmentStartDate"
          type="date"
          value={values.employmentStartDate}
          onChange={(event) => updateField('employmentStartDate', event.target.value)}
        />
        <Input
          label="Wynagrodzenie"
          name="remunerationAmount"
          type="number"
          min={0}
          step="0.01"
          value={values.remunerationAmount}
          error={fieldErrors.remunerationAmount}
          onChange={(event) => updateField('remunerationAmount', event.target.value)}
        />
        <label className="employee-form__select-label">
          <span>Jednostka wynagrodzenia</span>
          <select
            className="employee-form__select"
            name="remunerationUnit"
            value={values.remunerationUnit}
            onChange={(event) =>
              updateField('remunerationUnit', event.target.value as RemunerationUnit | '')
            }
          >
            <option value="">—</option>
            <option value="HOURLY">Godzinowa</option>
          </select>
        </label>
        <label className="employee-form__select-label">
          <span>Rodzaj wynagrodzenia</span>
          <select
            className="employee-form__select"
            name="remunerationNet"
            value={values.remunerationNet}
            onChange={(event) =>
              updateField('remunerationNet', event.target.value as EmployeeFormValues['remunerationNet'])
            }
          >
            <option value="">—</option>
            <option value="true">Netto</option>
            <option value="false">Brutto</option>
          </select>
        </label>
        <Input
          label="Kolor kalendarza"
          name="calendarColor"
          placeholder="#FF5733"
          value={values.calendarColor}
          error={fieldErrors.calendarColor}
          onChange={(event) => updateField('calendarColor', event.target.value)}
        />
        <Input
          label="Uwagi"
          name="notes"
          value={values.notes}
          className="employee-form__field--full"
          onChange={(event) => updateField('notes', event.target.value)}
        />
        {mode === 'edit' ? (
          <label className="employee-form__select-label">
            <span>Status</span>
            <select
              className="employee-form__select"
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

      {serverError ? <p className="employee-form__error">{serverError}</p> : null}

      <div className="employee-form__actions">
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
