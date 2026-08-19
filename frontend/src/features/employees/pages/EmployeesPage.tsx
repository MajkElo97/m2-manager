import { useMemo, useState } from 'react';
import { Plus } from 'lucide-react';
import { AppLayoutContainer } from '@/components/layout/AppLayout';
import { Button } from '@/components/ui/Button';
import { EmptyState } from '@/components/ui/EmptyState';
import { ErrorState } from '@/components/ui/ErrorState';
import { Input } from '@/components/ui/Input';
import { LoadingState } from '@/components/ui/LoadingState';
import { Modal } from '@/components/ui/Modal';
import { PageHeader } from '@/components/ui/PageHeader';
import {
  createEmployee,
  deactivateEmployee,
  updateEmployee,
} from '@/features/employees/api/employeesApi';
import { DeactivateEmployeeDialog } from '@/features/employees/components/DeactivateEmployeeDialog';
import { EmployeeForm } from '@/features/employees/components/EmployeeForm';
import { EmployeesMobileList } from '@/features/employees/components/EmployeesMobileList';
import { EmployeesTable } from '@/features/employees/components/EmployeesTable';
import { getEmployeeErrorMessage } from '@/features/employees/employeesMessages';
import { useEmployees } from '@/features/employees/hooks/useEmployees';
import type {
  CreateEmployeePayload,
  Employee,
  EmployeeRole,
  EmploymentType,
  UpdateEmployeePayload,
} from '@/features/employees/types/employee';
import { usePermissions } from '@/features/permissions/PermissionProvider';
import './EmployeesPage.css';

type RoleFilterValue = EmployeeRole | 'ALL';
type EmploymentTypeFilterValue = EmploymentType | 'ALL';
type ActiveFilterValue = 'ALL' | 'ACTIVE' | 'INACTIVE';

interface FormModalState {
  mode: 'create' | 'edit';
  employee?: Employee;
}

export function EmployeesPage() {
  const { hasPermission } = usePermissions();
  const canCreate = hasPermission('EMPLOYEES_CREATE');
  const canEdit = hasPermission('EMPLOYEES_EDIT');
  const canDelete = hasPermission('EMPLOYEES_DELETE');

  const [search, setSearch] = useState('');
  const [position, setPosition] = useState('');
  const [roleFilter, setRoleFilter] = useState<RoleFilterValue>('ALL');
  const [employmentTypeFilter, setEmploymentTypeFilter] =
    useState<EmploymentTypeFilterValue>('ALL');
  const [activeFilter, setActiveFilter] = useState<ActiveFilterValue>('ACTIVE');
  const [formModal, setFormModal] = useState<FormModalState | null>(null);
  const [deactivateTarget, setDeactivateTarget] = useState<Employee | null>(null);
  const [formLoading, setFormLoading] = useState(false);
  const [deactivateLoading, setDeactivateLoading] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const listParams = useMemo(
    () => ({
      search,
      position,
      role: roleFilter === 'ALL' ? null : roleFilter,
      employmentType: employmentTypeFilter === 'ALL' ? null : employmentTypeFilter,
      active:
        activeFilter === 'ALL'
          ? null
          : activeFilter === 'ACTIVE',
    }),
    [search, position, roleFilter, employmentTypeFilter, activeFilter],
  );

  const { employees, isLoading, error, forbidden, unauthorized, refetch } =
    useEmployees(listParams);

  const openCreateModal = () => {
    setFormError(null);
    setFormModal({ mode: 'create' });
  };

  const openEditModal = (employee: Employee) => {
    setFormError(null);
    setFormModal({ mode: 'edit', employee });
  };

  const closeFormModal = () => {
    if (!formLoading) {
      setFormModal(null);
      setFormError(null);
    }
  };

  const handleCreate = async (payload: CreateEmployeePayload | UpdateEmployeePayload) => {
    setFormLoading(true);
    setFormError(null);

    try {
      await createEmployee(payload as CreateEmployeePayload);
      setFormModal(null);
      setSuccessMessage('Pracownik został dodany.');
      await refetch();
    } catch (err) {
      setFormError(getEmployeeErrorMessage(err));
    } finally {
      setFormLoading(false);
    }
  };

  const handleUpdate = async (payload: CreateEmployeePayload | UpdateEmployeePayload) => {
    if (!formModal?.employee) {
      return;
    }

    setFormLoading(true);
    setFormError(null);

    try {
      await updateEmployee(formModal.employee.id, payload as UpdateEmployeePayload);
      setFormModal(null);
      setSuccessMessage('Pracownik został zaktualizowany.');
      await refetch();
    } catch (err) {
      setFormError(getEmployeeErrorMessage(err));
    } finally {
      setFormLoading(false);
    }
  };

  const handleDeactivate = async () => {
    if (!deactivateTarget) {
      return;
    }

    setDeactivateLoading(true);

    try {
      await deactivateEmployee(deactivateTarget.id);
      setDeactivateTarget(null);
      setSuccessMessage('Pracownik został dezaktywowany.');
      await refetch();
    } catch (err) {
      setSuccessMessage(null);
      setFormError(getEmployeeErrorMessage(err));
    } finally {
      setDeactivateLoading(false);
    }
  };

  const renderContent = () => {
    if (isLoading) {
      return <LoadingState label="Ładowanie pracowników…" />;
    }

    if (forbidden) {
      return (
        <ErrorState
          title="Brak dostępu"
          message={error ?? 'Nie masz uprawnień do tej operacji.'}
        />
      );
    }

    if (unauthorized) {
      return (
        <ErrorState
          title="Sesja wygasła"
          message={error ?? 'Sesja wygasła. Zaloguj się ponownie.'}
        />
      );
    }

    if (error) {
      return <ErrorState message={error} onRetry={() => void refetch()} />;
    }

    if (employees.length === 0) {
      return (
        <EmptyState
          title="BRAK PRACOWNIKÓW"
          description="Nie znaleziono pracowników spełniających kryteria."
          action={
            canCreate ? (
              <Button onClick={openCreateModal}>Dodaj pierwszego pracownika</Button>
            ) : undefined
          }
        />
      );
    }

    return (
      <>
        <div className="employees-page__desktop">
          <EmployeesTable
            employees={employees}
            canEdit={canEdit}
            canDelete={canDelete}
            onEdit={openEditModal}
            onDeactivate={setDeactivateTarget}
          />
        </div>
        <div className="employees-page__mobile">
          <EmployeesMobileList
            employees={employees}
            canEdit={canEdit}
            canDelete={canDelete}
            onEdit={openEditModal}
            onDeactivate={setDeactivateTarget}
          />
        </div>
      </>
    );
  };

  return (
    <AppLayoutContainer>
      <PageHeader
        title="Pracownicy"
        description="Zarządzanie pracownikami w organizacji."
        actions={
          canCreate ? (
            <Button onClick={openCreateModal}>
              <Plus size={16} aria-hidden="true" />
              Dodaj pracownika
            </Button>
          ) : undefined
        }
      />

      <div className="employees-page__toolbar">
        <div className="employees-page__search">
          <Input
            label="Szukaj"
            name="search"
            placeholder="Imię, nazwisko, kod, e-mail…"
            value={search}
            onChange={(event) => setSearch(event.target.value)}
          />
        </div>

        <div className="employees-page__search">
          <Input
            label="Stanowisko"
            name="position"
            placeholder="Filtruj po stanowisku…"
            value={position}
            onChange={(event) => setPosition(event.target.value)}
          />
        </div>

        <label className="employees-page__filter-label">
          <span>Rola</span>
          <select
            className="employees-page__filter"
            value={roleFilter}
            onChange={(event) => setRoleFilter(event.target.value as RoleFilterValue)}
            aria-label="Filtr roli"
          >
            <option value="ALL">Wszystkie</option>
            <option value="PRACOWNIK">Pracownik</option>
            <option value="ADMIN">Admin</option>
          </select>
        </label>

        <label className="employees-page__filter-label">
          <span>Forma zatrudnienia</span>
          <select
            className="employees-page__filter"
            value={employmentTypeFilter}
            onChange={(event) =>
              setEmploymentTypeFilter(event.target.value as EmploymentTypeFilterValue)
            }
            aria-label="Filtr formy zatrudnienia"
          >
            <option value="ALL">Wszystkie</option>
            <option value="ZLECENIE">Zlecenie</option>
          </select>
        </label>

        <label className="employees-page__filter-label">
          <span>Status</span>
          <select
            className="employees-page__filter"
            value={activeFilter}
            onChange={(event) => setActiveFilter(event.target.value as ActiveFilterValue)}
            aria-label="Filtr statusu"
          >
            <option value="ALL">Wszystkie</option>
            <option value="ACTIVE">Aktywne</option>
            <option value="INACTIVE">Nieaktywne</option>
          </select>
        </label>
      </div>

      {successMessage ? (
        <p className="employees-page__feedback" role="status">
          {successMessage}
        </p>
      ) : null}

      {renderContent()}

      <Modal
        isOpen={formModal !== null}
        title={formModal?.mode === 'edit' ? 'Edytuj pracownika' : 'Dodaj pracownika'}
        onClose={closeFormModal}
      >
        {formModal ? (
          <EmployeeForm
            key={formModal.employee?.id ?? 'create'}
            mode={formModal.mode}
            initialEmployee={formModal.employee}
            submitLabel={formModal.mode === 'edit' ? 'Zapisz zmiany' : 'Dodaj pracownika'}
            loading={formLoading}
            serverError={formError}
            onSubmit={formModal.mode === 'edit' ? handleUpdate : handleCreate}
            onCancel={closeFormModal}
          />
        ) : null}
      </Modal>

      <DeactivateEmployeeDialog
        employee={deactivateTarget}
        loading={deactivateLoading}
        onConfirm={() => void handleDeactivate()}
        onCancel={() => {
          if (!deactivateLoading) {
            setDeactivateTarget(null);
          }
        }}
      />
    </AppLayoutContainer>
  );
}
