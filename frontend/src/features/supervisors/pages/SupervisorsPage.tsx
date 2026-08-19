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
import { useManagers } from '@/features/managers/hooks/useManagers';
import {
  createSupervisor,
  deactivateSupervisor,
  updateSupervisor,
} from '@/features/supervisors/api/supervisorsApi';
import { DeactivateSupervisorDialog } from '@/features/supervisors/components/DeactivateSupervisorDialog';
import { SupervisorForm } from '@/features/supervisors/components/SupervisorForm';
import { SupervisorsMobileList } from '@/features/supervisors/components/SupervisorsMobileList';
import { SupervisorsTable } from '@/features/supervisors/components/SupervisorsTable';
import { useSupervisors } from '@/features/supervisors/hooks/useSupervisors';
import { getSupervisorErrorMessage } from '@/features/supervisors/supervisorsMessages';
import type {
  CreateSupervisorPayload,
  Supervisor,
  UpdateSupervisorPayload,
} from '@/features/supervisors/types/supervisor';
import { usePermissions } from '@/features/permissions/PermissionProvider';
import './SupervisorsPage.css';

type ManagerFilterValue = 'ALL' | string;
type ActiveFilterValue = 'ALL' | 'ACTIVE' | 'INACTIVE';

interface FormModalState {
  mode: 'create' | 'edit';
  supervisor?: Supervisor;
}

export function SupervisorsPage() {
  const { hasPermission } = usePermissions();
  const canCreate = hasPermission('SUPERVISORS_CREATE');
  const canEdit = hasPermission('SUPERVISORS_EDIT');
  const canDelete = hasPermission('SUPERVISORS_DELETE');

  const [search, setSearch] = useState('');
  const [managerFilter, setManagerFilter] = useState<ManagerFilterValue>('ALL');
  const [activeFilter, setActiveFilter] = useState<ActiveFilterValue>('ACTIVE');
  const [formModal, setFormModal] = useState<FormModalState | null>(null);
  const [deactivateTarget, setDeactivateTarget] = useState<Supervisor | null>(null);
  const [formLoading, setFormLoading] = useState(false);
  const [deactivateLoading, setDeactivateLoading] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const listParams = useMemo(
    () => ({
      search,
      managerId: managerFilter === 'ALL' ? null : managerFilter,
      active:
        activeFilter === 'ALL'
          ? null
          : activeFilter === 'ACTIVE',
    }),
    [search, managerFilter, activeFilter],
  );

  const { supervisors, isLoading, error, forbidden, unauthorized, refetch } =
    useSupervisors(listParams);

  const {
    managers,
    isLoading: managersLoading,
    error: managersError,
    refetch: refetchManagers,
  } = useManagers({ active: true });

  const openCreateModal = () => {
    setFormError(null);
    setFormModal({ mode: 'create' });
  };

  const openEditModal = (supervisor: Supervisor) => {
    setFormError(null);
    setFormModal({ mode: 'edit', supervisor });
  };

  const closeFormModal = () => {
    if (!formLoading) {
      setFormModal(null);
      setFormError(null);
    }
  };

  const handleCreate = async (payload: CreateSupervisorPayload | UpdateSupervisorPayload) => {
    setFormLoading(true);
    setFormError(null);

    try {
      await createSupervisor(payload as CreateSupervisorPayload);
      setFormModal(null);
      setSuccessMessage('Opiekun został dodany.');
      await refetch();
    } catch (err) {
      setFormError(getSupervisorErrorMessage(err));
    } finally {
      setFormLoading(false);
    }
  };

  const handleUpdate = async (payload: CreateSupervisorPayload | UpdateSupervisorPayload) => {
    if (!formModal?.supervisor) {
      return;
    }

    setFormLoading(true);
    setFormError(null);

    try {
      await updateSupervisor(formModal.supervisor.id, payload as UpdateSupervisorPayload);
      setFormModal(null);
      setSuccessMessage('Opiekun został zaktualizowany.');
      await refetch();
    } catch (err) {
      setFormError(getSupervisorErrorMessage(err));
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
      await deactivateSupervisor(deactivateTarget.id);
      setDeactivateTarget(null);
      setSuccessMessage('Opiekun został dezaktywowany.');
      await refetch();
    } catch (err) {
      setSuccessMessage(null);
      setFormError(getSupervisorErrorMessage(err));
    } finally {
      setDeactivateLoading(false);
    }
  };

  const isLoadingAll = isLoading || managersLoading;

  const renderContent = () => {
    if (isLoadingAll) {
      return <LoadingState label="Ładowanie opiekunów…" />;
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

    if ((error || managersError) && supervisors.length === 0) {
      return (
        <ErrorState
          message={error ?? managersError ?? 'Nie udało się wczytać danych.'}
          onRetry={() => {
            void refetch();
            void refetchManagers();
          }}
        />
      );
    }

    if (supervisors.length === 0) {
      return (
        <EmptyState
          title="BRAK OPIEKUNÓW"
          description="Nie znaleziono opiekunów spełniających kryteria."
          action={
            canCreate ? (
              <Button onClick={openCreateModal}>Dodaj pierwszego opiekuna</Button>
            ) : undefined
          }
        />
      );
    }

    return (
      <>
        <div className="supervisors-page__desktop">
          <SupervisorsTable
            supervisors={supervisors}
            canEdit={canEdit}
            canDelete={canDelete}
            onEdit={openEditModal}
            onDeactivate={setDeactivateTarget}
          />
        </div>
        <div className="supervisors-page__mobile">
          <SupervisorsMobileList
            supervisors={supervisors}
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
        title="Opiekunowie"
        description="Zarządzanie opiekunami w organizacji."
        actions={
          canCreate ? (
            <Button onClick={openCreateModal}>
              <Plus size={16} aria-hidden="true" />
              Dodaj opiekuna
            </Button>
          ) : undefined
        }
      />

      <div className="supervisors-page__toolbar">
        <div className="supervisors-page__search">
          <Input
            label="Szukaj"
            name="search"
            placeholder="Imię, nazwisko, kod, e-mail…"
            value={search}
            onChange={(event) => setSearch(event.target.value)}
          />
        </div>

        <label className="supervisors-page__filter-label">
          <span>Zarządca</span>
          <select
            className="supervisors-page__filter"
            value={managerFilter}
            onChange={(event) => setManagerFilter(event.target.value as ManagerFilterValue)}
            aria-label="Filtr zarządcy"
          >
            <option value="ALL">Wszyscy zarządcy</option>
            {managers.map((manager) => (
              <option key={manager.id} value={manager.id}>
                {manager.name} ({manager.code})
              </option>
            ))}
          </select>
        </label>

        <label className="supervisors-page__filter-label">
          <span>Status</span>
          <select
            className="supervisors-page__filter"
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
        <p className="supervisors-page__feedback" role="status">
          {successMessage}
        </p>
      ) : null}

      {renderContent()}

      <Modal
        isOpen={formModal !== null}
        title={formModal?.mode === 'edit' ? 'Edytuj opiekuna' : 'Dodaj opiekuna'}
        onClose={closeFormModal}
      >
        {formModal ? (
          <SupervisorForm
            key={formModal.supervisor?.id ?? 'create'}
            mode={formModal.mode}
            initialSupervisor={formModal.supervisor}
            managers={managers}
            submitLabel={formModal.mode === 'edit' ? 'Zapisz zmiany' : 'Dodaj opiekuna'}
            loading={formLoading}
            serverError={formError}
            onSubmit={formModal.mode === 'edit' ? handleUpdate : handleCreate}
            onCancel={closeFormModal}
          />
        ) : null}
      </Modal>

      <DeactivateSupervisorDialog
        supervisor={deactivateTarget}
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
