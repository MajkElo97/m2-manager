import { useMemo, useState, type FormEvent } from 'react';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { useEmployees } from '@/features/employees/hooks/useEmployees';
import { getFullName } from '@/features/employees/utils/employeeLabels';
import type {
  CreateEquipmentPayload,
  Equipment,
  EquipmentCondition,
  UpdateEquipmentPayload,
} from '@/features/inventory/types/equipment';
import { parseIsoDate, toIsoDate } from '@/utils/dateFormat';
import './EquipmentForm.css';

export interface EquipmentFormValues {
  code: string;
  name: string;
  category: string;
  manufacturer: string;
  model: string;
  serialNumber: string;
  quantity: string;
  conditionStatus: EquipmentCondition;
  location: string;
  employeeId: string;
  purchaseDate: string;
  purchaseValue: string;
  notes: string;
  active: boolean;
}

interface EquipmentFormProps {
  mode: 'create' | 'edit';
  initialEquipment?: Equipment;
  submitLabel: string;
  loading?: boolean;
  serverError?: string | null;
  onSubmit: (payload: CreateEquipmentPayload | UpdateEquipmentPayload) => Promise<void>;
  onCancel: () => void;
}

function emptyFormValues(): EquipmentFormValues {
  return {
    code: '',
    name: '',
    category: '',
    manufacturer: '',
    model: '',
    serialNumber: '',
    quantity: '1',
    conditionStatus: 'GOOD',
    location: '',
    employeeId: '',
    purchaseDate: '',
    purchaseValue: '',
    notes: '',
    active: true,
  };
}

function toFormValues(equipment: Equipment): EquipmentFormValues {
  return {
    code: equipment.code,
    name: equipment.name,
    category: equipment.category,
    manufacturer: equipment.manufacturer ?? '',
    model: equipment.model ?? '',
    serialNumber: equipment.serialNumber ?? '',
    quantity: String(equipment.quantity),
    conditionStatus: equipment.conditionStatus,
    location: equipment.location ?? '',
    employeeId: equipment.employeeId ?? '',
    purchaseDate: parseIsoDate(equipment.purchaseDate),
    purchaseValue: equipment.purchaseValue != null ? String(equipment.purchaseValue) : '',
    notes: equipment.notes ?? '',
    active: equipment.active,
  };
}

function validateForm(
  values: EquipmentFormValues,
): Partial<Record<keyof EquipmentFormValues, string>> {
  const errors: Partial<Record<keyof EquipmentFormValues, string>> = {};

  if (!values.code.trim()) {
    errors.code = 'Podaj kod sprzętu.';
  }

  if (!values.name.trim()) {
    errors.name = 'Podaj nazwę.';
  }

  if (!values.category.trim()) {
    errors.category = 'Podaj kategorię.';
  }

  if (!values.quantity.trim()) {
    errors.quantity = 'Podaj ilość.';
  } else {
    const quantity = Number(values.quantity);
    if (!Number.isInteger(quantity) || quantity < 0) {
      errors.quantity = 'Ilość musi być liczbą całkowitą nieujemną.';
    }
  }

  if (values.purchaseValue.trim()) {
    const purchaseValue = Number(values.purchaseValue);
    if (Number.isNaN(purchaseValue) || purchaseValue < 0) {
      errors.purchaseValue = 'Wartość zakupu musi być liczbą nieujemną.';
    }
  }

  return errors;
}

function toPayload(values: EquipmentFormValues): CreateEquipmentPayload {
  return {
    code: values.code.trim(),
    name: values.name.trim(),
    category: values.category.trim(),
    manufacturer: values.manufacturer.trim() || undefined,
    model: values.model.trim() || undefined,
    serialNumber: values.serialNumber.trim() || undefined,
    quantity: Number(values.quantity),
    conditionStatus: values.conditionStatus,
    location: values.location.trim() || undefined,
    employeeId: values.employeeId || null,
    purchaseDate: toIsoDate(values.purchaseDate),
    purchaseValue: values.purchaseValue.trim() ? Number(values.purchaseValue) : null,
    notes: values.notes.trim() || undefined,
  };
}

export function EquipmentForm({
  mode,
  initialEquipment,
  submitLabel,
  loading = false,
  serverError,
  onSubmit,
  onCancel,
}: EquipmentFormProps) {
  const [values, setValues] = useState<EquipmentFormValues>(
    initialEquipment ? toFormValues(initialEquipment) : emptyFormValues(),
  );
  const [fieldErrors, setFieldErrors] = useState<
    Partial<Record<keyof EquipmentFormValues, string>>
  >({});

  const { employees } = useEmployees({ active: true });

  const employeeOptions = useMemo(
    () =>
      employees.map((employee) => ({
        id: employee.id,
        label: `${getFullName(employee.firstName, employee.lastName)} (${employee.code})`,
      })),
    [employees],
  );

  const updateField = <K extends keyof EquipmentFormValues>(
    field: K,
    value: EquipmentFormValues[K],
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
    <form className="inventory-form" onSubmit={handleSubmit} noValidate>
      <div className="inventory-form__grid">
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
        <Input
          label="Producent"
          name="manufacturer"
          value={values.manufacturer}
          onChange={(event) => updateField('manufacturer', event.target.value)}
        />
        <Input
          label="Model"
          name="model"
          value={values.model}
          onChange={(event) => updateField('model', event.target.value)}
        />
        <Input
          label="Numer seryjny"
          name="serialNumber"
          value={values.serialNumber}
          onChange={(event) => updateField('serialNumber', event.target.value)}
        />
        <Input
          label="Ilość"
          name="quantity"
          type="number"
          min={0}
          step={1}
          value={values.quantity}
          error={fieldErrors.quantity}
          onChange={(event) => updateField('quantity', event.target.value)}
          required
        />
        <label className="inventory-form__select-label">
          <span>Stan</span>
          <select
            className="inventory-form__select"
            name="conditionStatus"
            value={values.conditionStatus}
            onChange={(event) =>
              updateField('conditionStatus', event.target.value as EquipmentCondition)
            }
          >
            <option value="GOOD">Dobry</option>
            <option value="USED">Używany</option>
            <option value="DAMAGED">Uszkodzony</option>
            <option value="OUT_OF_SERVICE">Wyłączony z użytku</option>
          </select>
        </label>
        <Input
          label="Lokalizacja"
          name="location"
          value={values.location}
          onChange={(event) => updateField('location', event.target.value)}
        />
        <label className="inventory-form__select-label">
          <span>Pracownik</span>
          <select
            className="inventory-form__select"
            name="employeeId"
            value={values.employeeId}
            onChange={(event) => updateField('employeeId', event.target.value)}
          >
            <option value="">—</option>
            {employeeOptions.map((option) => (
              <option key={option.id} value={option.id}>
                {option.label}
              </option>
            ))}
          </select>
        </label>
        <Input
          label="Data zakupu"
          name="purchaseDate"
          type="date"
          value={values.purchaseDate}
          onChange={(event) => updateField('purchaseDate', event.target.value)}
        />
        <Input
          label="Wartość zakupu"
          name="purchaseValue"
          type="number"
          min={0}
          step="0.01"
          value={values.purchaseValue}
          error={fieldErrors.purchaseValue}
          onChange={(event) => updateField('purchaseValue', event.target.value)}
        />
        <Input
          label="Uwagi"
          name="notes"
          value={values.notes}
          className="inventory-form__field--full"
          onChange={(event) => updateField('notes', event.target.value)}
        />
        {mode === 'edit' ? (
          <label className="inventory-form__select-label">
            <span>Status</span>
            <select
              className="inventory-form__select"
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

      {serverError ? <p className="inventory-form__error">{serverError}</p> : null}

      <div className="inventory-form__actions">
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
