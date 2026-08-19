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
  createManager,
  deactivateManager,
  updateManager,
} from '@/features/managers/api/managersApi';
import { DeactivateManagerDialog } from '@/features/managers/components/DeactivateManagerDialog';
import { ManagerForm } from '@/features/managers/components/ManagerForm';
import { ManagersMobileList } from '@/features/managers/components/ManagersMobileList';
import { ManagersTable } from '@/features/managers/components/ManagersTable';
import { useManagers } from '@/features/managers/hooks/useManagers';
import { getManagerErrorMessage } from '@/features/managers/managersMessages';
import type {
  CreateManagerPayload,
  Manager,
  UpdateManagerPayload,
} from '@/features/managers/types/manager';
import { usePermissions } from '@/features/permissions/PermissionProvider';
import './ManagersPage.css';

type ActiveFilterValue = 'ALL' | 'ACTIVE' | 'INACTIVE';

interface FormModalState {
  mode: 'create' | 'edit';
  manager?: Manager;
}

export function ManagersPage() {
  const { hasPermission } = usePermissions();
  const canCreate = hasPermission('MANAGERS_CREATE');
  const canEdit = hasPermission('MANAGERS_EDIT');
  const canDelete = hasPermission('MANAGERS_DELETE');

  const [search, setSearch] = useState('');
  const [activeFilter, setActiveFilter] = useState<ActiveFilterValue>('ACTIVE');
  const [formModal, setFormModal] = useState<FormModalState | null>(null);
  const [deactivateTarget, setDeactivateTarget] = useState<Manager | null>(null);
  const [formLoading, setFormLoading] = useState(false);
  const [deactivateLoading, setDeactivateLoading] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const listParams = useMemo(
    () => ({
      search,
      active:
        activeFilter === 'ALL'
          ? null
          : activeFilter === 'ACTIVE',
    }),
    [search, activeFilter],
  );

  const { managers, isLoading, error, forbidden, unauthorized, refetch } = useManagers(listParams);

  const openCreateModal = () => {
    setFormError(null);
    setFormModal({ mode: 'create' });
  };

  const openEditModal = (manager: Manager) => {
    setFormError(null);
    setFormModal({ mode: 'edit', manager });
  };

  const closeFormModal = () => {
    if (!formLoading) {
      setFormModal(null);
      setFormError(null);
    }
  };

  const handleCreate = async (payload: CreateManagerPayload | UpdateManagerPayload) => {
    setFormLoading(true);
    setFormError(null);

    try {
      await createManager(payload as CreateManagerPayload);
      setFormModal(null);
      setSuccessMessage('Zarządca został dodany.');
      await refetch();
    } catch (err) {
      setFormError(getManagerErrorMessage(err));
    } finally {
      setFormLoading(false);
    }
  };

  const handleUpdate = async (payload: CreateManagerPayload | UpdateManagerPayload) => {
    if (!formModal?.manager) {
      return;
    }

    setFormLoading(true);
    setFormError(null);

    try {
      await updateManager(formModal.manager.id, payload as UpdateManagerPayload);
      setFormModal(null);
      setSuccessMessage('Zarządca został zaktualizowany.');
      await refetch();
    } catch (err) {
      setFormError(getManagerErrorMessage(err));
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
      await deactivateManager(deactivateTarget.id);
      setDeactivateTarget(null);
      setSuccessMessage('Zarządca został dezaktywowany.');
      await refetch();
    } catch (err) {
      setSuccessMessage(null);
      setFormError(getManagerErrorMessage(err));
    } finally {
      setDeactivateLoading(false);
    }
  };

  const renderContent = () => {
    if (isLoading) {
      return <LoadingState label="Ładowanie zarządców…" />;
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

    if (managers.length === 0) {
      return (
        <EmptyState
          title="BRAK ZARZĄDCÓW"
          description="Nie znaleziono zarządców spełniających kryteria."
          action={
            canCreate ? (
              <Button onClick={openCreateModal}>Dodaj pierwszego zarządcę</Button>
            ) : undefined
          }
        />
      );
    }

    return (
      <>
        <div className="managers-page__desktop">
          <ManagersTable
            managers={managers}
            canEdit={canEdit}
            canDelete={canDelete}
            onEdit={openEditModal}
            onDeactivate={setDeactivateTarget}
          />
        </div>
        <div className="managers-page__mobile">
          <ManagersMobileList
            managers={managers}
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
        title="Zarządcy"
        description="Zarządzanie zarządcami w organizacji."
        actions={
          canCreate ? (
            <Button onClick={openCreateModal}>
              <Plus size={16} aria-hidden="true" />
              Dodaj zarządcę
            </Button>
          ) : undefined
        }
      />

      <div className="managers-page__toolbar">
        <div className="managers-page__search">
          <Input
            label="Szukaj"
            name="search"
            placeholder="Nazwa, telefon, e-mail…"
            value={search}
            onChange={(event) => setSearch(event.target.value)}
          />
        </div>

        <label className="managers-page__filter-label">
          <span>Status</span>
          <select
            className="managers-page__filter"
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
        <p className="managers-page__feedback" role="status">
          {successMessage}
        </p>
      ) : null}

      {renderContent()}

      <Modal
        isOpen={formModal !== null}
        title={formModal?.mode === 'edit' ? 'Edytuj zarządcę' : 'Dodaj zarządcę'}
        onClose={closeFormModal}
      >
        {formModal ? (
          <ManagerForm
            key={formModal.manager?.id ?? 'create'}
            mode={formModal.mode}
            initialManager={formModal.manager}
            submitLabel={formModal.mode === 'edit' ? 'Zapisz zmiany' : 'Dodaj zarządcę'}
            loading={formLoading}
            serverError={formError}
            onSubmit={formModal.mode === 'edit' ? handleUpdate : handleCreate}
            onCancel={closeFormModal}
          />
        ) : null}
      </Modal>

      <DeactivateManagerDialog
        manager={deactivateTarget}
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
