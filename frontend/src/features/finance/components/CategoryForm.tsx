import { useState, type FormEvent } from 'react';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import type {
  CreateCategoryPayload,
  FinancialCategory,
  UpdateCategoryPayload,
} from '@/features/finance/types/category';
import type { TransactionType } from '@/features/finance/types/transaction';
import { getTransactionTypeLabel } from '@/features/finance/utils/financeLabels';
import './CategoryForm.css';

export interface CategoryFormValues {
  code: string;
  name: string;
  type: TransactionType;
  active: boolean;
}

interface CategoryFormProps {
  mode: 'create' | 'edit';
  initialCategory?: FinancialCategory;
  submitLabel: string;
  loading?: boolean;
  serverError?: string | null;
  onSubmit: (payload: CreateCategoryPayload | UpdateCategoryPayload) => Promise<void>;
  onCancel: () => void;
}

const TRANSACTION_TYPES: TransactionType[] = ['INCOME', 'EXPENSE'];

function emptyFormValues(): CategoryFormValues {
  return {
    code: '',
    name: '',
    type: 'INCOME',
    active: true,
  };
}

function toFormValues(category: FinancialCategory): CategoryFormValues {
  return {
    code: category.code,
    name: category.name,
    type: category.type,
    active: category.active,
  };
}

function validateForm(
  values: CategoryFormValues,
): Partial<Record<keyof CategoryFormValues, string>> {
  const errors: Partial<Record<keyof CategoryFormValues, string>> = {};

  if (!values.code.trim()) {
    errors.code = 'Podaj kod kategorii.';
  }

  if (!values.name.trim()) {
    errors.name = 'Podaj nazwę kategorii.';
  }

  return errors;
}

function toPayload(values: CategoryFormValues): CreateCategoryPayload {
  return {
    code: values.code.trim(),
    name: values.name.trim(),
    type: values.type,
    active: values.active,
  };
}

export function CategoryForm({
  mode,
  initialCategory,
  submitLabel,
  loading = false,
  serverError,
  onSubmit,
  onCancel,
}: CategoryFormProps) {
  const [values, setValues] = useState<CategoryFormValues>(() =>
    initialCategory ? toFormValues(initialCategory) : emptyFormValues(),
  );
  const [fieldErrors, setFieldErrors] = useState<
    Partial<Record<keyof CategoryFormValues, string>>
  >({});

  const handleChange = (field: keyof CategoryFormValues, value: string | boolean) => {
    setValues((current) => ({ ...current, [field]: value }));

    if (fieldErrors[field as keyof CategoryFormValues]) {
      setFieldErrors((current) => ({ ...current, [field]: undefined }));
    }
  };

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();

    const errors = validateForm(values);
    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      return;
    }

    await onSubmit(toPayload(values));
  };

  return (
    <form className="category-form" onSubmit={(event) => void handleSubmit(event)} noValidate>
      <div className="category-form__grid">
        <Input
          label="Kod"
          name="code"
          value={values.code}
          onChange={(event) => handleChange('code', event.target.value)}
          error={fieldErrors.code}
          required
        />

        <Input
          label="Nazwa"
          name="name"
          value={values.name}
          onChange={(event) => handleChange('name', event.target.value)}
          error={fieldErrors.name}
          required
        />

        <label className="category-form__select-label">
          <span>Typ</span>
          <select
            className="category-form__select"
            value={values.type}
            onChange={(event) => handleChange('type', event.target.value)}
            aria-label="Typ kategorii"
          >
            {TRANSACTION_TYPES.map((type) => (
              <option key={type} value={type}>
                {getTransactionTypeLabel(type)}
              </option>
            ))}
          </select>
        </label>

        {mode === 'edit' ? (
          <label className="category-form__checkbox-label">
            <input
              type="checkbox"
              checked={values.active}
              onChange={(event) => handleChange('active', event.target.checked)}
            />
            <span>Aktywna</span>
          </label>
        ) : null}
      </div>

      {serverError ? <p className="category-form__error">{serverError}</p> : null}

      <div className="category-form__actions">
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
