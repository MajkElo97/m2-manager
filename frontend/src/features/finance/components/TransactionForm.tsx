import { useMemo, useState, type FormEvent } from 'react';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import type { Building } from '@/features/buildings/types/building';
import { useBuildings } from '@/features/buildings/hooks/useBuildings';
import { useEmployees } from '@/features/employees/hooks/useEmployees';
import { getFullName } from '@/features/employees/utils/employeeLabels';
import { useFleet } from '@/features/fleet/hooks/useFleet';
import type { FinancialCategory } from '@/features/finance/types/category';
import type {
  CreateTransactionPayload,
  FinancialTransaction,
  PaymentStatus,
  TransactionType,
  UpdateTransactionPayload,
} from '@/features/finance/types/transaction';
import { calculateAmounts } from '@/features/finance/utils/financeCalculations';
import {
  getPaymentStatusLabel,
  getTransactionTypeLabel,
} from '@/features/finance/utils/financeLabels';
import { formatCurrency } from '@/features/finance/utils/formatCurrency';
import { useChemicals } from '@/features/inventory/hooks/useChemicals';
import { useEquipment } from '@/features/inventory/hooks/useEquipment';
import { parseIsoDate, toIsoDate } from '@/utils/dateFormat';
import './TransactionForm.css';

export interface TransactionFormValues {
  code: string;
  transactionDate: string;
  type: TransactionType;
  netAmount: string;
  vatRate: string;
  categoryId: string;
  contractorName: string;
  contractorNip: string;
  buildingId: string;
  employeeId: string;
  vehicleId: string;
  equipmentId: string;
  chemicalId: string;
  description: string;
  documentNumber: string;
  dueDate: string;
  paymentDate: string;
  paymentStatus: PaymentStatus;
  notes: string;
}

interface TransactionFormProps {
  mode: 'create' | 'edit';
  initialTransaction?: FinancialTransaction;
  categories: FinancialCategory[];
  buildings?: Building[];
  fixedBuildingId?: string;
  submitLabel: string;
  loading?: boolean;
  serverError?: string | null;
  onSubmit: (payload: CreateTransactionPayload | UpdateTransactionPayload) => Promise<void>;
  onCancel: () => void;
}

const PAYMENT_STATUSES: PaymentStatus[] = ['NOT_APPLICABLE', 'TO_PAY', 'PAID', 'OVERDUE'];
const TRANSACTION_TYPES: TransactionType[] = ['INCOME', 'EXPENSE'];

function emptyFormValues(): TransactionFormValues {
  return {
    code: '',
    transactionDate: '',
    type: 'INCOME',
    netAmount: '',
    vatRate: '23',
    categoryId: '',
    contractorName: '',
    contractorNip: '',
    buildingId: '',
    employeeId: '',
    vehicleId: '',
    equipmentId: '',
    chemicalId: '',
    description: '',
    documentNumber: '',
    dueDate: '',
    paymentDate: '',
    paymentStatus: 'NOT_APPLICABLE',
    notes: '',
  };
}

function toFormValues(transaction: FinancialTransaction): TransactionFormValues {
  return {
    code: transaction.code,
    transactionDate: parseIsoDate(transaction.transactionDate),
    type: transaction.type,
    netAmount: String(transaction.netAmount),
    vatRate: transaction.vatRate != null ? String(transaction.vatRate) : '',
    categoryId: transaction.categoryId,
    contractorName: transaction.contractorName ?? '',
    contractorNip: transaction.contractorNip ?? '',
    buildingId: transaction.buildingId ?? '',
    employeeId: transaction.employeeId ?? '',
    vehicleId: transaction.vehicleId ?? '',
    equipmentId: transaction.equipmentId ?? '',
    chemicalId: transaction.chemicalId ?? '',
    description: transaction.description ?? '',
    documentNumber: transaction.documentNumber ?? '',
    dueDate: parseIsoDate(transaction.dueDate),
    paymentDate: parseIsoDate(transaction.paymentDate),
    paymentStatus: transaction.paymentStatus,
    notes: transaction.notes ?? '',
  };
}

function validateForm(
  values: TransactionFormValues,
): Partial<Record<keyof TransactionFormValues, string>> {
  const errors: Partial<Record<keyof TransactionFormValues, string>> = {};

  if (!values.code.trim()) {
    errors.code = 'Podaj kod operacji.';
  }

  if (!values.transactionDate) {
    errors.transactionDate = 'Podaj datę operacji.';
  }

  if (!values.netAmount.trim()) {
    errors.netAmount = 'Podaj kwotę netto.';
  } else if (Number.isNaN(Number(values.netAmount)) || Number(values.netAmount) < 0) {
    errors.netAmount = 'Podaj poprawną kwotę netto.';
  }

  if (values.vatRate.trim() && (Number.isNaN(Number(values.vatRate)) || Number(values.vatRate) < 0)) {
    errors.vatRate = 'Podaj poprawną stawkę VAT.';
  }

  if (!values.categoryId) {
    errors.categoryId = 'Wybierz kategorię.';
  }

  return errors;
}

function toPayload(values: TransactionFormValues): CreateTransactionPayload {
  return {
    code: values.code.trim(),
    transactionDate: toIsoDate(values.transactionDate) ?? values.transactionDate,
    type: values.type,
    netAmount: Number(values.netAmount),
    vatRate: values.vatRate.trim() ? Number(values.vatRate) : null,
    categoryId: values.categoryId,
    contractorName: values.contractorName.trim() || null,
    contractorNip: values.contractorNip.trim() || null,
    buildingId: values.buildingId || null,
    employeeId: values.employeeId || null,
    vehicleId: values.vehicleId || null,
    equipmentId: values.equipmentId || null,
    chemicalId: values.chemicalId || null,
    description: values.description.trim() || null,
    documentNumber: values.documentNumber.trim() || null,
    dueDate: toIsoDate(values.dueDate),
    paymentDate: toIsoDate(values.paymentDate),
    paymentStatus: values.paymentStatus,
    notes: values.notes.trim() || null,
  };
}

export function TransactionForm({
  mode: _mode,
  initialTransaction,
  categories,
  buildings: buildingsProp,
  fixedBuildingId,
  submitLabel,
  loading = false,
  serverError,
  onSubmit,
  onCancel,
}: TransactionFormProps) {
  const [values, setValues] = useState<TransactionFormValues>(() =>
    initialTransaction
      ? toFormValues(initialTransaction)
      : {
          ...emptyFormValues(),
          buildingId: fixedBuildingId ?? '',
        },
  );
  const [fieldErrors, setFieldErrors] = useState<
    Partial<Record<keyof TransactionFormValues, string>>
  >({});

  const { buildings: fetchedBuildings } = useBuildings({ status: 'ACTIVE' });
  const { employees } = useEmployees({ active: true });
  const { vehicles } = useFleet({ status: 'ACTIVE' });
  const { equipment } = useEquipment({ active: true });
  const { chemicals } = useChemicals({ active: true });

  const buildings = buildingsProp ?? fetchedBuildings;

  const filteredCategories = useMemo(
    () => categories.filter((category) => category.active && category.type === values.type),
    [categories, values.type],
  );

  const previewAmounts = useMemo(() => {
    const net = Number(values.netAmount);
    if (Number.isNaN(net) || net < 0) {
      return null;
    }

    const rate = values.vatRate.trim() ? Number(values.vatRate) : null;
    if (values.vatRate.trim() && (Number.isNaN(rate) || rate! < 0)) {
      return null;
    }

    return calculateAmounts(net, rate);
  }, [values.netAmount, values.vatRate]);

  const handleChange = (field: keyof TransactionFormValues, value: string) => {
    setValues((current) => {
      const next = { ...current, [field]: value };

      if (field === 'type') {
        next.categoryId = '';
      }

      return next;
    });

    if (fieldErrors[field]) {
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

    const payload = toPayload({
      ...values,
      buildingId: fixedBuildingId ?? values.buildingId,
    });
    await onSubmit(payload);
  };

  return (
    <form className="transaction-form" onSubmit={(event) => void handleSubmit(event)} noValidate>
      <div className="transaction-form__grid">
        <Input
          label="Kod"
          name="code"
          value={values.code}
          onChange={(event) => handleChange('code', event.target.value)}
          error={fieldErrors.code}
          required
        />

        <Input
          label="Data operacji"
          name="transactionDate"
          type="date"
          value={values.transactionDate}
          onChange={(event) => handleChange('transactionDate', event.target.value)}
          error={fieldErrors.transactionDate}
          required
        />

        <label className="transaction-form__select-label">
          <span>Typ</span>
          <select
            className="transaction-form__select"
            value={values.type}
            onChange={(event) => handleChange('type', event.target.value)}
            aria-label="Typ operacji"
          >
            {TRANSACTION_TYPES.map((type) => (
              <option key={type} value={type}>
                {getTransactionTypeLabel(type)}
              </option>
            ))}
          </select>
        </label>

        <label className="transaction-form__select-label">
          <span>Kategoria</span>
          <select
            className="transaction-form__select"
            value={values.categoryId}
            onChange={(event) => handleChange('categoryId', event.target.value)}
            aria-label="Kategoria"
          >
            <option value="">Wybierz kategorię</option>
            {filteredCategories.map((category) => (
              <option key={category.id} value={category.id}>
                {category.name} ({category.code})
              </option>
            ))}
          </select>
          {fieldErrors.categoryId ? (
            <span className="transaction-form__field-error">{fieldErrors.categoryId}</span>
          ) : null}
        </label>

        <Input
          label="Kwota netto"
          name="netAmount"
          type="number"
          min="0"
          step="0.01"
          value={values.netAmount}
          onChange={(event) => handleChange('netAmount', event.target.value)}
          error={fieldErrors.netAmount}
          required
        />

        <Input
          label="Stawka VAT (%)"
          name="vatRate"
          type="number"
          min="0"
          step="0.01"
          value={values.vatRate}
          onChange={(event) => handleChange('vatRate', event.target.value)}
          error={fieldErrors.vatRate}
        />

        {previewAmounts ? (
          <div className="transaction-form__preview transaction-form__field--full">
            <span>VAT: {formatCurrency(previewAmounts.vatAmount)}</span>
            <span>Brutto: {formatCurrency(previewAmounts.grossAmount)}</span>
          </div>
        ) : null}

        <Input
          label="Kontrahent"
          name="contractorName"
          value={values.contractorName}
          onChange={(event) => handleChange('contractorName', event.target.value)}
        />

        <Input
          label="NIP kontrahenta"
          name="contractorNip"
          value={values.contractorNip}
          onChange={(event) => handleChange('contractorNip', event.target.value)}
        />

        {!fixedBuildingId ? (
          <label className="transaction-form__select-label">
            <span>Budynek</span>
            <select
              className="transaction-form__select"
              value={values.buildingId}
              onChange={(event) => handleChange('buildingId', event.target.value)}
              aria-label="Budynek"
            >
              <option value="">Brak przypisania</option>
              {buildings.map((building) => (
                <option key={building.id} value={building.id}>
                  {building.name} ({building.code})
                </option>
              ))}
            </select>
          </label>
        ) : null}

        <label className="transaction-form__select-label">
          <span>Pracownik</span>
          <select
            className="transaction-form__select"
            value={values.employeeId}
            onChange={(event) => handleChange('employeeId', event.target.value)}
            aria-label="Pracownik"
          >
            <option value="">Brak przypisania</option>
            {employees.map((employee) => (
              <option key={employee.id} value={employee.id}>
                {getFullName(employee.firstName, employee.lastName)} ({employee.code})
              </option>
            ))}
          </select>
        </label>

        <label className="transaction-form__select-label">
          <span>Pojazd</span>
          <select
            className="transaction-form__select"
            value={values.vehicleId}
            onChange={(event) => handleChange('vehicleId', event.target.value)}
            aria-label="Pojazd"
          >
            <option value="">Brak przypisania</option>
            {vehicles.map((vehicle) => (
              <option key={vehicle.id} value={vehicle.id}>
                {vehicle.registrationNumber} ({vehicle.code})
              </option>
            ))}
          </select>
        </label>

        <label className="transaction-form__select-label">
          <span>Sprzęt</span>
          <select
            className="transaction-form__select"
            value={values.equipmentId}
            onChange={(event) => handleChange('equipmentId', event.target.value)}
            aria-label="Sprzęt"
          >
            <option value="">Brak przypisania</option>
            {equipment.map((item) => (
              <option key={item.id} value={item.id}>
                {item.name} ({item.code})
              </option>
            ))}
          </select>
        </label>

        <label className="transaction-form__select-label">
          <span>Chemia</span>
          <select
            className="transaction-form__select"
            value={values.chemicalId}
            onChange={(event) => handleChange('chemicalId', event.target.value)}
            aria-label="Chemia"
          >
            <option value="">Brak przypisania</option>
            {chemicals.map((item) => (
              <option key={item.id} value={item.id}>
                {item.name} ({item.code})
              </option>
            ))}
          </select>
        </label>

        <Input
          label="Numer dokumentu"
          name="documentNumber"
          value={values.documentNumber}
          onChange={(event) => handleChange('documentNumber', event.target.value)}
        />

        <Input
          label="Termin płatności"
          name="dueDate"
          type="date"
          value={values.dueDate}
          onChange={(event) => handleChange('dueDate', event.target.value)}
        />

        <Input
          label="Data płatności"
          name="paymentDate"
          type="date"
          value={values.paymentDate}
          onChange={(event) => handleChange('paymentDate', event.target.value)}
        />

        <label className="transaction-form__select-label">
          <span>Status płatności</span>
          <select
            className="transaction-form__select"
            value={values.paymentStatus}
            onChange={(event) => handleChange('paymentStatus', event.target.value)}
            aria-label="Status płatności"
          >
            {PAYMENT_STATUSES.map((status) => (
              <option key={status} value={status}>
                {getPaymentStatusLabel(status)}
              </option>
            ))}
          </select>
        </label>

        <div className="transaction-form__field--full">
          <label className="transaction-form__textarea-label">
            <span>Opis</span>
            <textarea
              className="transaction-form__textarea"
              name="description"
              rows={2}
              value={values.description}
              onChange={(event) => handleChange('description', event.target.value)}
            />
          </label>
        </div>

        <div className="transaction-form__field--full">
          <label className="transaction-form__textarea-label">
            <span>Notatki</span>
            <textarea
              className="transaction-form__textarea"
              name="notes"
              rows={2}
              value={values.notes}
              onChange={(event) => handleChange('notes', event.target.value)}
            />
          </label>
        </div>
      </div>

      {serverError ? <p className="transaction-form__error">{serverError}</p> : null}

      <div className="transaction-form__actions">
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
