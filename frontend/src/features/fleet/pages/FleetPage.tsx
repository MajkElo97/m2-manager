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
import { useEmployees } from '@/features/employees/hooks/useEmployees';
import { getFullName } from '@/features/employees/utils/employeeLabels';
import {
  createVehicle,
  deactivateVehicle,
  updateVehicle,
} from '@/features/fleet/api/fleetApi';
import { DeactivateVehicleDialog } from '@/features/fleet/components/DeactivateVehicleDialog';
import { FleetMobileList } from '@/features/fleet/components/FleetMobileList';
import { FleetTable } from '@/features/fleet/components/FleetTable';
import { VehicleForm } from '@/features/fleet/components/VehicleForm';
import { getFleetErrorMessage } from '@/features/fleet/fleetMessages';
import { useFleet } from '@/features/fleet/hooks/useFleet';
import type {
  CreateVehiclePayload,
  UpdateVehiclePayload,
  Vehicle,
  VehicleStatus,
} from '@/features/fleet/types/vehicle';
import { getVehicleStatusLabel } from '@/features/fleet/utils/vehicleLabels';
import { usePermissions } from '@/features/permissions/PermissionProvider';
import './FleetPage.css';

type StatusFilterValue = VehicleStatus | 'ALL';
type EmployeeFilterValue = string | 'ALL';

interface FormModalState {
  mode: 'create' | 'edit';
  vehicle?: Vehicle;
}

const VEHICLE_STATUSES: VehicleStatus[] = ['ACTIVE', 'IN_SERVICE', 'INACTIVE', 'SOLD'];

export function FleetPage() {
  const { hasPermission } = usePermissions();
  const canCreate = hasPermission('FLEET_CREATE');
  const canEdit = hasPermission('FLEET_EDIT');
  const canDelete = hasPermission('FLEET_DELETE');

  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState<StatusFilterValue>('ACTIVE');
  const [employeeFilter, setEmployeeFilter] = useState<EmployeeFilterValue>('ALL');
  const [formModal, setFormModal] = useState<FormModalState | null>(null);
  const [deactivateTarget, setDeactivateTarget] = useState<Vehicle | null>(null);
  const [formLoading, setFormLoading] = useState(false);
  const [deactivateLoading, setDeactivateLoading] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const employeeListParams = useMemo(() => ({ active: true }), []);

  const listParams = useMemo(
    () => ({
      search,
      status: statusFilter === 'ALL' ? null : statusFilter,
      employeeId: employeeFilter === 'ALL' ? null : employeeFilter,
    }),
    [search, statusFilter, employeeFilter],
  );

  const { vehicles, isLoading, error, forbidden, unauthorized, refetch } = useFleet(listParams);
  const { employees } = useEmployees(employeeListParams);

  const openCreateModal = () => {
    setFormError(null);
    setFormModal({ mode: 'create' });
  };

  const openEditModal = (vehicle: Vehicle) => {
    setFormError(null);
    setFormModal({ mode: 'edit', vehicle });
  };

  const closeFormModal = () => {
    if (!formLoading) {
      setFormModal(null);
      setFormError(null);
    }
  };

  const handleCreate = async (payload: CreateVehiclePayload | UpdateVehiclePayload) => {
    setFormLoading(true);
    setFormError(null);

    try {
      await createVehicle(payload as CreateVehiclePayload);
      setFormModal(null);
      setSuccessMessage('Pojazd został dodany.');
      await refetch();
    } catch (err) {
      setFormError(getFleetErrorMessage(err));
    } finally {
      setFormLoading(false);
    }
  };

  const handleUpdate = async (payload: CreateVehiclePayload | UpdateVehiclePayload) => {
    if (!formModal?.vehicle) {
      return;
    }

    setFormLoading(true);
    setFormError(null);

    try {
      await updateVehicle(formModal.vehicle.id, payload as UpdateVehiclePayload);
      setFormModal(null);
      setSuccessMessage('Pojazd został zaktualizowany.');
      await refetch();
    } catch (err) {
      setFormError(getFleetErrorMessage(err));
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
      await deactivateVehicle(deactivateTarget.id);
      setDeactivateTarget(null);
      setSuccessMessage('Pojazd został dezaktywowany.');
      await refetch();
    } catch (err) {
      setSuccessMessage(null);
      setFormError(getFleetErrorMessage(err));
    } finally {
      setDeactivateLoading(false);
    }
  };

  const renderContent = () => {
    if (isLoading) {
      return <LoadingState label="Ładowanie floty…" />;
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

    if (vehicles.length === 0) {
      return (
        <EmptyState
          title="BRAK POJAZDÓW"
          description="Nie znaleziono pojazdów spełniających kryteria."
          action={
            canCreate ? (
              <Button onClick={openCreateModal}>Dodaj pierwszy pojazd</Button>
            ) : undefined
          }
        />
      );
    }

    return (
      <>
        <div className="fleet-page__desktop">
          <FleetTable
            vehicles={vehicles}
            canEdit={canEdit}
            canDelete={canDelete}
            onEdit={openEditModal}
            onDeactivate={setDeactivateTarget}
          />
        </div>
        <div className="fleet-page__mobile">
          <FleetMobileList
            vehicles={vehicles}
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
        title="Flota"
        description="Zarządzanie pojazdami w organizacji."
        actions={
          canCreate ? (
            <Button onClick={openCreateModal}>
              <Plus size={16} aria-hidden="true" />
              Dodaj samochód
            </Button>
          ) : undefined
        }
      />

      <div className="fleet-page__toolbar">
        <div className="fleet-page__search">
          <Input
            label="Szukaj"
            name="search"
            placeholder="Kod, rejestracja, marka, model…"
            value={search}
            onChange={(event) => setSearch(event.target.value)}
          />
        </div>

        <label className="fleet-page__filter-label">
          <span>Status</span>
          <select
            className="fleet-page__filter"
            value={statusFilter}
            onChange={(event) => setStatusFilter(event.target.value as StatusFilterValue)}
            aria-label="Filtr statusu"
          >
            <option value="ALL">Wszystkie</option>
            {VEHICLE_STATUSES.map((status) => (
              <option key={status} value={status}>
                {getVehicleStatusLabel(status)}
              </option>
            ))}
          </select>
        </label>

        <label className="fleet-page__filter-label">
          <span>Pracownik</span>
          <select
            className="fleet-page__filter"
            value={employeeFilter}
            onChange={(event) => setEmployeeFilter(event.target.value as EmployeeFilterValue)}
            aria-label="Filtr pracownika"
          >
            <option value="ALL">Wszyscy</option>
            {employees.map((employee) => (
              <option key={employee.id} value={employee.id}>
                {getFullName(employee.firstName, employee.lastName)} ({employee.code})
              </option>
            ))}
          </select>
        </label>
      </div>

      {successMessage ? (
        <p className="fleet-page__feedback" role="status">
          {successMessage}
        </p>
      ) : null}

      {renderContent()}

      <Modal
        isOpen={formModal !== null}
        title={formModal?.mode === 'edit' ? 'Edytuj pojazd' : 'Dodaj samochód'}
        onClose={closeFormModal}
        size="large"
      >
        {formModal ? (
          <VehicleForm
            key={formModal.vehicle?.id ?? 'create'}
            mode={formModal.mode}
            initialVehicle={formModal.vehicle}
            employees={employees}
            submitLabel={formModal.mode === 'edit' ? 'Zapisz zmiany' : 'Dodaj samochód'}
            loading={formLoading}
            serverError={formError}
            onSubmit={formModal.mode === 'edit' ? handleUpdate : handleCreate}
            onCancel={closeFormModal}
          />
        ) : null}
      </Modal>

      <DeactivateVehicleDialog
        vehicle={deactivateTarget}
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
