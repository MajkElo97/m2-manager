import { useMemo, useState, type FormEvent } from 'react';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { useEmployees } from '@/features/employees/hooks/useEmployees';
import { getFullName } from '@/features/employees/utils/employeeLabels';
import { useRoles } from '@/features/roles/hooks/useRoles';
import type { CreateUserPayload, UpdateUserPayload, User } from '@/features/users/types/user';
import './UserForm.css';

export interface UserFormValues {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  roleIds: string[];
  employeeId: string;
  active: boolean;
}

interface UserFormProps {
  mode: 'create' | 'edit';
  initialUser?: User;
  submitLabel: string;
  loading?: boolean;
  serverError?: string | null;
  onSubmit: (payload: CreateUserPayload | UpdateUserPayload) => Promise<void>;
  onCancel: () => void;
}

function emptyFormValues(): UserFormValues {
  return {
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    roleIds: [],
    employeeId: '',
    active: true,
  };
}

function toFormValues(user: User): UserFormValues {
  return {
    firstName: user.firstName,
    lastName: user.lastName,
    email: user.email,
    password: '',
    roleIds: user.roles.map((role) => role.id),
    employeeId: user.employeeId ?? '',
    active: user.active,
  };
}

function validateForm(
  mode: 'create' | 'edit',
  values: UserFormValues,
): Partial<Record<keyof UserFormValues, string>> {
  const errors: Partial<Record<keyof UserFormValues, string>> = {};

  if (!values.firstName.trim()) {
    errors.firstName = 'Podaj imię.';
  }

  if (!values.lastName.trim()) {
    errors.lastName = 'Podaj nazwisko.';
  }

  if (!values.email.trim()) {
    errors.email = 'Podaj adres e-mail.';
  }

  if (mode === 'create' && !values.password.trim()) {
    errors.password = 'Podaj hasło.';
  } else if (values.password.trim() && values.password.trim().length < 8) {
    errors.password = 'Hasło musi mieć co najmniej 8 znaków.';
  }

  if (values.roleIds.length === 0) {
    errors.roleIds = 'Wybierz co najmniej jedną rolę.';
  }

  return errors;
}

function toCreatePayload(values: UserFormValues): CreateUserPayload {
  return {
    firstName: values.firstName.trim(),
    lastName: values.lastName.trim(),
    email: values.email.trim(),
    password: values.password.trim(),
    roleIds: values.roleIds,
    employeeId: values.employeeId || null,
  };
}

function toUpdatePayload(values: UserFormValues): UpdateUserPayload {
  const payload: UpdateUserPayload = {
    firstName: values.firstName.trim(),
    lastName: values.lastName.trim(),
    email: values.email.trim(),
    roleIds: values.roleIds,
    employeeId: values.employeeId || null,
    active: values.active,
  };

  if (values.password.trim()) {
    payload.password = values.password.trim();
  }

  return payload;
}

export function UserForm({
  mode,
  initialUser,
  submitLabel,
  loading = false,
  serverError,
  onSubmit,
  onCancel,
}: UserFormProps) {
  const [values, setValues] = useState<UserFormValues>(
    initialUser ? toFormValues(initialUser) : emptyFormValues(),
  );
  const [fieldErrors, setFieldErrors] = useState<
    Partial<Record<keyof UserFormValues, string>>
  >({});

  const { roles } = useRoles();
  const { employees } = useEmployees({ active: true });

  const activeRoles = useMemo(
    () => roles.filter((role) => role.active),
    [roles],
  );

  const employeeOptions = useMemo(
    () =>
      employees.map((employee) => ({
        id: employee.id,
        label: `${getFullName(employee.firstName, employee.lastName)} (${employee.code})`,
      })),
    [employees],
  );

  const updateField = <K extends keyof UserFormValues>(field: K, value: UserFormValues[K]) => {
    setValues((current) => ({ ...current, [field]: value }));
    setFieldErrors((current) => ({ ...current, [field]: undefined }));
  };

  const toggleRole = (roleId: string, checked: boolean) => {
    setValues((current) => ({
      ...current,
      roleIds: checked
        ? [...current.roleIds, roleId]
        : current.roleIds.filter((id) => id !== roleId),
    }));
    setFieldErrors((current) => ({ ...current, roleIds: undefined }));
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const errors = validateForm(mode, values);
    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      return;
    }

    await onSubmit(mode === 'create' ? toCreatePayload(values) : toUpdatePayload(values));
  };

  return (
    <form className="user-form" onSubmit={handleSubmit} noValidate>
      <div className="user-form__grid">
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
          error={fieldErrors.lastName}
          onChange={(event) => updateField('lastName', event.target.value)}
          required
        />
        <Input
          label="E-mail"
          name="email"
          type="email"
          value={values.email}
          error={fieldErrors.email}
          onChange={(event) => updateField('email', event.target.value)}
          required
        />
        <div className="user-form__password-field">
          <Input
            label="Hasło"
            name="password"
            type="password"
            value={values.password}
            error={fieldErrors.password}
            onChange={(event) => updateField('password', event.target.value)}
            required={mode === 'create'}
          />
          {mode === 'edit' ? (
            <p className="user-form__hint">Pozostaw puste, aby nie zmieniać hasła.</p>
          ) : null}
        </div>
      </div>

      <fieldset className="user-form__roles">
        <legend>Role</legend>
        <div className="user-form__checkboxes">
          {activeRoles.map((role) => (
            <label key={role.id} className="user-form__checkbox">
              <input
                type="checkbox"
                checked={values.roleIds.includes(role.id)}
                onChange={(event) => toggleRole(role.id, event.target.checked)}
              />
              {role.name}
            </label>
          ))}
        </div>
        {fieldErrors.roleIds ? (
          <p className="user-form__field-error">{fieldErrors.roleIds}</p>
        ) : null}
      </fieldset>

      <label className="user-form__select-label">
        <span>Powiązany pracownik</span>
        <select
          className="user-form__select"
          value={values.employeeId}
          onChange={(event) => updateField('employeeId', event.target.value)}
          aria-label="Powiązany pracownik"
        >
          <option value="">— Brak —</option>
          {employeeOptions.map((employee) => (
            <option key={employee.id} value={employee.id}>
              {employee.label}
            </option>
          ))}
        </select>
      </label>

      {mode === 'edit' ? (
        <label className="user-form__checkbox user-form__checkbox--standalone">
          <input
            type="checkbox"
            checked={values.active}
            onChange={(event) => updateField('active', event.target.checked)}
          />
          Aktywny
        </label>
      ) : null}

      {serverError ? <p className="user-form__error">{serverError}</p> : null}

      <div className="user-form__actions">
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
