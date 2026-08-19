import { useState, type FormEvent } from 'react';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import type { Employee } from '@/features/employees/types/employee';
import { getFullName } from '@/features/employees/utils/employeeLabels';
import type {
  CreateVehiclePayload,
  UpdateVehiclePayload,
  Vehicle,
  VehicleStatus,
  VehicleType,
} from '@/features/fleet/types/vehicle';
import {
  getVehicleStatusLabel,
  getVehicleTypeLabel,
} from '@/features/fleet/utils/vehicleLabels';
import { parseIsoDate, toIsoDate } from '@/utils/dateFormat';
import './VehicleForm.css';

export interface VehicleFormValues {
  code: string;
  registrationNumber: string;
  make: string;
  model: string;
  productionYear: string;
  vin: string;
  vehicleType: VehicleType;
  employeeId: string;
  status: VehicleStatus;
  insuranceStartDate: string;
  insuranceEndDate: string;
  insurer: string;
  insurancePolicyNumber: string;
  lastInspectionDate: string;
  nextInspectionDate: string;
  lastInspectionMileage: string;
  purchaseDate: string;
  currentMileage: string;
  notes: string;
}

interface VehicleFormProps {
  mode: 'create' | 'edit';
  initialVehicle?: Vehicle;
  employees: Employee[];
  submitLabel: string;
  loading?: boolean;
  serverError?: string | null;
  onSubmit: (payload: CreateVehiclePayload | UpdateVehiclePayload) => Promise<void>;
  onCancel: () => void;
}

function emptyFormValues(): VehicleFormValues {
  return {
    code: '',
    registrationNumber: '',
    make: '',
    model: '',
    productionYear: '',
    vin: '',
    vehicleType: 'PASSENGER',
    employeeId: '',
    status: 'ACTIVE',
    insuranceStartDate: '',
    insuranceEndDate: '',
    insurer: '',
    insurancePolicyNumber: '',
    lastInspectionDate: '',
    nextInspectionDate: '',
    lastInspectionMileage: '',
    purchaseDate: '',
    currentMileage: '',
    notes: '',
  };
}

function toFormValues(vehicle: Vehicle): VehicleFormValues {
  return {
    code: vehicle.code,
    registrationNumber: vehicle.registrationNumber,
    make: vehicle.make,
    model: vehicle.model,
    productionYear: vehicle.productionYear != null ? String(vehicle.productionYear) : '',
    vin: vehicle.vin ?? '',
    vehicleType: vehicle.vehicleType,
    employeeId: vehicle.employeeId ?? '',
    status: vehicle.status,
    insuranceStartDate: parseIsoDate(vehicle.insuranceStartDate),
    insuranceEndDate: parseIsoDate(vehicle.insuranceEndDate),
    insurer: vehicle.insurer ?? '',
    insurancePolicyNumber: vehicle.insurancePolicyNumber ?? '',
    lastInspectionDate: parseIsoDate(vehicle.lastInspectionDate),
    nextInspectionDate: parseIsoDate(vehicle.nextInspectionDate),
    lastInspectionMileage:
      vehicle.lastInspectionMileage != null ? String(vehicle.lastInspectionMileage) : '',
    purchaseDate: parseIsoDate(vehicle.purchaseDate),
    currentMileage: vehicle.currentMileage != null ? String(vehicle.currentMileage) : '',
    notes: vehicle.notes ?? '',
  };
}

function validateForm(values: VehicleFormValues): Partial<Record<keyof VehicleFormValues, string>> {
  const errors: Partial<Record<keyof VehicleFormValues, string>> = {};

  if (!values.code.trim()) {
    errors.code = 'Podaj kod pojazdu.';
  }

  if (!values.registrationNumber.trim()) {
    errors.registrationNumber = 'Podaj numer rejestracyjny.';
  }

  if (!values.make.trim()) {
    errors.make = 'Podaj markę.';
  }

  if (!values.model.trim()) {
    errors.model = 'Podaj model.';
  }

  if (values.productionYear.trim()) {
    const year = Number(values.productionYear);
    if (Number.isNaN(year) || year < 1900 || year > 2100) {
      errors.productionYear = 'Podaj poprawny rok produkcji.';
    }
  }

  if (values.lastInspectionMileage.trim()) {
    const mileage = Number(values.lastInspectionMileage);
    if (Number.isNaN(mileage) || mileage < 0) {
      errors.lastInspectionMileage = 'Przebieg musi być liczbą nieujemną.';
    }
  }

  if (values.currentMileage.trim()) {
    const mileage = Number(values.currentMileage);
    if (Number.isNaN(mileage) || mileage < 0) {
      errors.currentMileage = 'Przebieg musi być liczbą nieujemną.';
    }
  }

  return errors;
}

function parseOptionalInt(value: string): number | null {
  if (!value.trim()) {
    return null;
  }

  return Number(value);
}

function toPayload(values: VehicleFormValues): CreateVehiclePayload {
  return {
    code: values.code.trim(),
    registrationNumber: values.registrationNumber.trim(),
    make: values.make.trim(),
    model: values.model.trim(),
    productionYear: parseOptionalInt(values.productionYear),
    vin: values.vin.trim() || undefined,
    vehicleType: values.vehicleType,
    employeeId: values.employeeId || null,
    status: values.status,
    insuranceStartDate: toIsoDate(values.insuranceStartDate),
    insuranceEndDate: toIsoDate(values.insuranceEndDate),
    insurer: values.insurer.trim() || undefined,
    insurancePolicyNumber: values.insurancePolicyNumber.trim() || undefined,
    lastInspectionDate: toIsoDate(values.lastInspectionDate),
    nextInspectionDate: toIsoDate(values.nextInspectionDate),
    lastInspectionMileage: parseOptionalInt(values.lastInspectionMileage),
    purchaseDate: toIsoDate(values.purchaseDate),
    currentMileage: parseOptionalInt(values.currentMileage),
    notes: values.notes.trim() || undefined,
  };
}

const VEHICLE_TYPES: VehicleType[] = ['PASSENGER', 'DELIVERY', 'VAN', 'OTHER'];
const VEHICLE_STATUSES: VehicleStatus[] = ['ACTIVE', 'IN_SERVICE', 'INACTIVE', 'SOLD'];

export function VehicleForm({
  mode: _mode,
  initialVehicle,
  employees,
  submitLabel,
  loading = false,
  serverError,
  onSubmit,
  onCancel,
}: VehicleFormProps) {
  const [values, setValues] = useState<VehicleFormValues>(
    initialVehicle ? toFormValues(initialVehicle) : emptyFormValues(),
  );
  const [fieldErrors, setFieldErrors] = useState<
    Partial<Record<keyof VehicleFormValues, string>>
  >({});

  const updateField = <K extends keyof VehicleFormValues>(field: K, value: VehicleFormValues[K]) => {
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

    await onSubmit(toPayload(values));
  };

  return (
    <form className="vehicle-form" onSubmit={handleSubmit} noValidate>
      <div className="vehicle-form__grid">
        <Input
          label="Kod"
          name="code"
          value={values.code}
          error={fieldErrors.code}
          onChange={(event) => updateField('code', event.target.value)}
          required
        />
        <Input
          label="Numer rejestracyjny"
          name="registrationNumber"
          value={values.registrationNumber}
          error={fieldErrors.registrationNumber}
          onChange={(event) => updateField('registrationNumber', event.target.value)}
          required
        />
        <Input
          label="Marka"
          name="make"
          value={values.make}
          error={fieldErrors.make}
          onChange={(event) => updateField('make', event.target.value)}
          required
        />
        <Input
          label="Model"
          name="model"
          value={values.model}
          error={fieldErrors.model}
          onChange={(event) => updateField('model', event.target.value)}
          required
        />
        <Input
          label="Rok produkcji"
          name="productionYear"
          type="number"
          min={1900}
          max={2100}
          value={values.productionYear}
          error={fieldErrors.productionYear}
          onChange={(event) => updateField('productionYear', event.target.value)}
        />
        <Input
          label="VIN"
          name="vin"
          value={values.vin}
          onChange={(event) => updateField('vin', event.target.value)}
        />
        <label className="vehicle-form__select-label">
          <span>Typ pojazdu</span>
          <select
            className="vehicle-form__select"
            name="vehicleType"
            value={values.vehicleType}
            onChange={(event) => updateField('vehicleType', event.target.value as VehicleType)}
          >
            {VEHICLE_TYPES.map((type) => (
              <option key={type} value={type}>
                {getVehicleTypeLabel(type)}
              </option>
            ))}
          </select>
        </label>
        <label className="vehicle-form__select-label">
          <span>Pracownik</span>
          <select
            className="vehicle-form__select"
            name="employeeId"
            value={values.employeeId}
            onChange={(event) => updateField('employeeId', event.target.value)}
          >
            <option value="">—</option>
            {employees.map((employee) => (
              <option key={employee.id} value={employee.id}>
                {getFullName(employee.firstName, employee.lastName)} ({employee.code})
              </option>
            ))}
          </select>
        </label>
        <label className="vehicle-form__select-label">
          <span>Status</span>
          <select
            className="vehicle-form__select"
            name="status"
            value={values.status}
            onChange={(event) => updateField('status', event.target.value as VehicleStatus)}
          >
            {VEHICLE_STATUSES.map((status) => (
              <option key={status} value={status}>
                {getVehicleStatusLabel(status)}
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
          label="Aktualny przebieg (km)"
          name="currentMileage"
          type="number"
          min={0}
          value={values.currentMileage}
          error={fieldErrors.currentMileage}
          onChange={(event) => updateField('currentMileage', event.target.value)}
        />
        <Input
          label="Ubezpieczyciel"
          name="insurer"
          value={values.insurer}
          onChange={(event) => updateField('insurer', event.target.value)}
        />
        <Input
          label="Numer polisy OC"
          name="insurancePolicyNumber"
          value={values.insurancePolicyNumber}
          onChange={(event) => updateField('insurancePolicyNumber', event.target.value)}
        />
        <Input
          label="OC od"
          name="insuranceStartDate"
          type="date"
          value={values.insuranceStartDate}
          onChange={(event) => updateField('insuranceStartDate', event.target.value)}
        />
        <Input
          label="OC do"
          name="insuranceEndDate"
          type="date"
          value={values.insuranceEndDate}
          onChange={(event) => updateField('insuranceEndDate', event.target.value)}
        />
        <Input
          label="Ostatni przegląd"
          name="lastInspectionDate"
          type="date"
          value={values.lastInspectionDate}
          onChange={(event) => updateField('lastInspectionDate', event.target.value)}
        />
        <Input
          label="Następny przegląd"
          name="nextInspectionDate"
          type="date"
          value={values.nextInspectionDate}
          onChange={(event) => updateField('nextInspectionDate', event.target.value)}
        />
        <Input
          label="Przebieg przy ostatnim przeglądzie (km)"
          name="lastInspectionMileage"
          type="number"
          min={0}
          value={values.lastInspectionMileage}
          error={fieldErrors.lastInspectionMileage}
          onChange={(event) => updateField('lastInspectionMileage', event.target.value)}
        />
        <Input
          label="Uwagi"
          name="notes"
          value={values.notes}
          className="vehicle-form__field--full"
          onChange={(event) => updateField('notes', event.target.value)}
        />
      </div>

      {serverError ? <p className="vehicle-form__error">{serverError}</p> : null}

      <div className="vehicle-form__actions">
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
