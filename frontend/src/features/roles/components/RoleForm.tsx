import { useEffect, useMemo, useState, type FormEvent } from 'react';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { LoadingState } from '@/components/ui/LoadingState';
import { getPermissions } from '@/features/roles/api/permissionsApi';
import { getRolePermissions } from '@/features/roles/api/rolesApi';
import { SYSTEM_ROLE_READONLY_HINT } from '@/features/roles/rolesMessages';
import type { Permission, PermissionAction } from '@/features/roles/types/permission';
import { PERMISSION_MATRIX_ACTIONS } from '@/features/roles/types/permission';
import type { CreateRolePayload, Role, UpdateRolePayload } from '@/features/roles/types/role';
import {
  applyEditPreset,
  applyReadPreset,
  buildPermissionMatrix,
  clearModulePermissions,
  getPermissionActionLabel,
  isEditPresetSelected,
  isReadPresetSelected,
  togglePermissionCodes,
} from '@/features/roles/utils/permissionMatrix';
import './RoleForm.css';

export interface RoleFormValues {
  name: string;
  description: string;
}

export interface RoleFormSubmitPayload {
  role: CreateRolePayload | UpdateRolePayload;
  permissionCodes: string[];
}

interface RoleFormProps {
  mode: 'create' | 'edit';
  initialRole?: Role;
  submitLabel: string;
  loading?: boolean;
  serverError?: string | null;
  onSubmit: (payload: RoleFormSubmitPayload) => Promise<void>;
  onCancel: () => void;
}

function emptyFormValues(): RoleFormValues {
  return {
    name: '',
    description: '',
  };
}

function toFormValues(role: Role): RoleFormValues {
  return {
    name: role.name,
    description: role.description ?? '',
  };
}

function validateForm(values: RoleFormValues): Partial<Record<keyof RoleFormValues, string>> {
  const errors: Partial<Record<keyof RoleFormValues, string>> = {};

  if (!values.name.trim()) {
    errors.name = 'Podaj nazwę roli.';
  }

  return errors;
}

export function RoleForm({
  mode,
  initialRole,
  submitLabel,
  loading = false,
  serverError,
  onSubmit,
  onCancel,
}: RoleFormProps) {
  const [values, setValues] = useState<RoleFormValues>(
    initialRole ? toFormValues(initialRole) : emptyFormValues(),
  );
  const [fieldErrors, setFieldErrors] = useState<
    Partial<Record<keyof RoleFormValues, string>>
  >({});
  const [allPermissions, setAllPermissions] = useState<Permission[]>([]);
  const [selectedCodes, setSelectedCodes] = useState<Set<string>>(new Set());
  const [permissionsLoading, setPermissionsLoading] = useState(true);
  const [permissionsError, setPermissionsError] = useState<string | null>(null);

  const isReadOnly = mode === 'edit' && initialRole?.systemRole === true;

  const matrixRows = useMemo(
    () => buildPermissionMatrix(allPermissions),
    [allPermissions],
  );

  useEffect(() => {
    let cancelled = false;

    async function loadPermissions() {
      setPermissionsLoading(true);
      setPermissionsError(null);

      try {
        const permissions = await getPermissions();
        if (cancelled) {
          return;
        }

        setAllPermissions(permissions);

        if (mode === 'edit' && initialRole) {
          const rolePermissions = await getRolePermissions(initialRole.id);
          if (!cancelled) {
            setSelectedCodes(new Set(rolePermissions.map((permission) => permission.code)));
          }
        } else {
          setSelectedCodes(new Set());
        }
      } catch {
        if (!cancelled) {
          setPermissionsError('Nie udało się wczytać uprawnień.');
        }
      } finally {
        if (!cancelled) {
          setPermissionsLoading(false);
        }
      }
    }

    void loadPermissions();

    return () => {
      cancelled = true;
    };
  }, [mode, initialRole?.id]);

  const updateField = <K extends keyof RoleFormValues>(field: K, value: RoleFormValues[K]) => {
    setValues((current) => ({ ...current, [field]: value }));
    setFieldErrors((current) => ({ ...current, [field]: undefined }));
  };

  const togglePermission = (code: string, checked: boolean) => {
    setSelectedCodes((current) => togglePermissionCodes(current, [code], checked));
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (isReadOnly) {
      return;
    }

    const errors = validateForm(values);
    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      return;
    }

    const rolePayload =
      mode === 'create'
        ? {
            name: values.name.trim(),
            description: values.description.trim() || undefined,
          }
        : {
            name: values.name.trim(),
            description: values.description.trim() || undefined,
          };

    await onSubmit({
      role: rolePayload,
      permissionCodes: [...selectedCodes],
    });
  };

  return (
    <form className="role-form" onSubmit={handleSubmit} noValidate>
      {isReadOnly ? (
        <p className="role-form__hint" role="note">
          {SYSTEM_ROLE_READONLY_HINT}
        </p>
      ) : null}

      <div className="role-form__grid">
        <Input
          label="Nazwa"
          name="name"
          value={values.name}
          error={fieldErrors.name}
          onChange={(event) => updateField('name', event.target.value)}
          disabled={isReadOnly}
          required
        />
        <Input
          label="Opis"
          name="description"
          value={values.description}
          onChange={(event) => updateField('description', event.target.value)}
          disabled={isReadOnly}
        />
      </div>

      <div className="role-form__matrix-section">
        <h3 className="role-form__matrix-title">Uprawnienia</h3>

        {permissionsLoading ? (
          <LoadingState label="Ładowanie uprawnień…" />
        ) : permissionsError ? (
          <p className="role-form__error">{permissionsError}</p>
        ) : (
          <div className="role-form__matrix-wrapper">
            <table className="role-form__matrix">
              <thead>
                <tr>
                  <th scope="col">Moduł</th>
                  <th scope="col">Skróty</th>
                  {PERMISSION_MATRIX_ACTIONS.map((action) => (
                    <th key={action} scope="col">
                      {getPermissionActionLabel(action)}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {matrixRows.map((row) => (
                  <tr key={row.module}>
                    <th scope="row">{row.label}</th>
                    <td>
                      <div className="role-form__presets">
                        <Button
                          type="button"
                          variant="secondary"
                          size="sm"
                          disabled={isReadOnly}
                          aria-pressed={isReadPresetSelected(row, selectedCodes)}
                          onClick={() => setSelectedCodes((current) => applyReadPreset(row, current))}
                        >
                          Odczyt
                        </Button>
                        <Button
                          type="button"
                          variant="secondary"
                          size="sm"
                          disabled={isReadOnly}
                          aria-pressed={isEditPresetSelected(row, selectedCodes)}
                          onClick={() => setSelectedCodes((current) => applyEditPreset(row, current))}
                        >
                          Edycja
                        </Button>
                        <Button
                          type="button"
                          variant="secondary"
                          size="sm"
                          disabled={isReadOnly}
                          onClick={() => setSelectedCodes((current) => clearModulePermissions(row, current))}
                        >
                          Wyczyść
                        </Button>
                      </div>
                    </td>
                    {PERMISSION_MATRIX_ACTIONS.map((action) => {
                      const permission = row.permissions[action as PermissionAction];
                      if (!permission) {
                        return (
                          <td key={action} className="role-form__matrix-empty">
                            —
                          </td>
                        );
                      }

                      return (
                        <td key={action}>
                          <label className="role-form__matrix-checkbox">
                            <input
                              type="checkbox"
                              checked={selectedCodes.has(permission.code)}
                              disabled={isReadOnly}
                              onChange={(event) =>
                                togglePermission(permission.code, event.target.checked)
                              }
                              aria-label={`${row.label} — ${getPermissionActionLabel(action)}`}
                            />
                          </label>
                        </td>
                      );
                    })}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {serverError ? <p className="role-form__error">{serverError}</p> : null}

      <div className="role-form__actions">
        <Button type="button" variant="secondary" onClick={onCancel}>
          {isReadOnly ? 'Zamknij' : 'Anuluj'}
        </Button>
        {!isReadOnly ? (
          <Button type="submit" loading={loading}>
            {submitLabel}
          </Button>
        ) : null}
      </div>
    </form>
  );
}
