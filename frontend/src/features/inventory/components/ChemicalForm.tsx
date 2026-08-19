import { useState, type FormEvent } from 'react';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import type {
  Chemical,
  ChemicalUnit,
  CreateChemicalPayload,
  UpdateChemicalPayload,
} from '@/features/inventory/types/chemical';
import './ChemicalForm.css';

export interface ChemicalFormValues {
  code: string;
  name: string;
  category: string;
  quantity: string;
  unit: ChemicalUnit;
  minimumStock: string;
  location: string;
  notes: string;
  active: boolean;
}

interface ChemicalFormProps {
  mode: 'create' | 'edit';
  initialChemical?: Chemical;
  submitLabel: string;
  loading?: boolean;
  serverError?: string | null;
  onSubmit: (payload: CreateChemicalPayload | UpdateChemicalPayload) => Promise<void>;
  onCancel: () => void;
}

function emptyFormValues(): ChemicalFormValues {
  return {
    code: '',
    name: '',
    category: '',
    quantity: '0',
    unit: 'LITER',
    minimumStock: '',
    location: '',
    notes: '',
    active: true,
  };
}

function toFormValues(chemical: Chemical): ChemicalFormValues {
  return {
    code: chemical.code,
    name: chemical.name,
    category: chemical.category,
    quantity: String(chemical.quantity),
    unit: chemical.unit,
    minimumStock: chemical.minimumStock != null ? String(chemical.minimumStock) : '',
    location: chemical.location ?? '',
    notes: chemical.notes ?? '',
    active: chemical.active,
  };
}

function validateForm(values: ChemicalFormValues): Partial<Record<keyof ChemicalFormValues, string>> {
  const errors: Partial<Record<keyof ChemicalFormValues, string>> = {};

  if (!values.code.trim()) {
    errors.code = 'Podaj kod.';
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
    if (Number.isNaN(quantity) || quantity < 0) {
      errors.quantity = 'Ilość musi być liczbą nieujemną.';
    }
  }

  if (values.minimumStock.trim()) {
    const minimumStock = Number(values.minimumStock);
    if (Number.isNaN(minimumStock) || minimumStock < 0) {
      errors.minimumStock = 'Stan minimalny musi być liczbą nieujemną.';
    }
  }

  return errors;
}

function toPayload(values: ChemicalFormValues): CreateChemicalPayload {
  return {
    code: values.code.trim(),
    name: values.name.trim(),
    category: values.category.trim(),
    quantity: Number(values.quantity),
    unit: values.unit,
    minimumStock: values.minimumStock.trim() ? Number(values.minimumStock) : null,
    location: values.location.trim() || undefined,
    notes: values.notes.trim() || undefined,
  };
}

export function ChemicalForm({
  mode,
  initialChemical,
  submitLabel,
  loading = false,
  serverError,
  onSubmit,
  onCancel,
}: ChemicalFormProps) {
  const [values, setValues] = useState<ChemicalFormValues>(
    initialChemical ? toFormValues(initialChemical) : emptyFormValues(),
  );
  const [fieldErrors, setFieldErrors] = useState<
    Partial<Record<keyof ChemicalFormValues, string>>
  >({});

  const updateField = <K extends keyof ChemicalFormValues>(
    field: K,
    value: ChemicalFormValues[K],
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
          label="Ilość"
          name="quantity"
          type="number"
          min={0}
          step="0.01"
          value={values.quantity}
          error={fieldErrors.quantity}
          onChange={(event) => updateField('quantity', event.target.value)}
          required
        />
        <label className="inventory-form__select-label">
          <span>Jednostka</span>
          <select
            className="inventory-form__select"
            name="unit"
            value={values.unit}
            onChange={(event) => updateField('unit', event.target.value as ChemicalUnit)}
          >
            <option value="LITER">l</option>
            <option value="KILOGRAM">kg</option>
            <option value="PIECE">szt.</option>
            <option value="PACK">opak.</option>
            <option value="OTHER">inne</option>
          </select>
        </label>
        <Input
          label="Stan minimalny"
          name="minimumStock"
          type="number"
          min={0}
          step="0.01"
          value={values.minimumStock}
          error={fieldErrors.minimumStock}
          onChange={(event) => updateField('minimumStock', event.target.value)}
        />
        <Input
          label="Lokalizacja"
          name="location"
          value={values.location}
          onChange={(event) => updateField('location', event.target.value)}
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
