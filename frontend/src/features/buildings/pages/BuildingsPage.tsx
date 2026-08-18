import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
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
  createBuilding,
  deactivateBuilding,
  updateBuilding,
} from '@/features/buildings/api/buildingsApi';
import { BuildingForm } from '@/features/buildings/components/BuildingForm';
import { BuildingsMobileList } from '@/features/buildings/components/BuildingsMobileList';
import { BuildingsTable } from '@/features/buildings/components/BuildingsTable';
import { DeactivateBuildingDialog } from '@/features/buildings/components/DeactivateBuildingDialog';
import { getBuildingErrorMessage } from '@/features/buildings/buildingsMessages';
import { useBuildings } from '@/features/buildings/hooks/useBuildings';
import type {
  Building,
  BuildingStatus,
  CreateBuildingPayload,
  UpdateBuildingPayload,
} from '@/features/buildings/types/building';
import { usePermissions } from '@/features/permissions/PermissionProvider';
import './BuildingsPage.css';

type StatusFilterValue = BuildingStatus | 'ALL';

interface FormModalState {
  mode: 'create' | 'edit';
  building?: Building;
}

export function BuildingsPage() {
  const navigate = useNavigate();
  const { hasPermission } = usePermissions();
  const canViewStaircases = hasPermission('BUILDINGS_VIEW');
  const canCreate = hasPermission('BUILDINGS_CREATE');
  const canEdit = hasPermission('BUILDINGS_EDIT');
  const canDelete = hasPermission('BUILDINGS_DELETE');

  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState<StatusFilterValue>('ACTIVE');
  const [formModal, setFormModal] = useState<FormModalState | null>(null);
  const [deactivateTarget, setDeactivateTarget] = useState<Building | null>(null);
  const [formLoading, setFormLoading] = useState(false);
  const [deactivateLoading, setDeactivateLoading] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const listParams = useMemo(
    () => ({
      search,
      status: statusFilter === 'ALL' ? null : statusFilter,
    }),
    [search, statusFilter],
  );

  const { buildings, isLoading, error, forbidden, unauthorized, refetch } = useBuildings(listParams);

  const openCreateModal = () => {
    setFormError(null);
    setFormModal({ mode: 'create' });
  };

  const openEditModal = (building: Building) => {
    setFormError(null);
    setFormModal({ mode: 'edit', building });
  };

  const openStaircases = (building: Building) => {
    void navigate(`/buildings/${building.id}/staircases`);
  };

  const closeFormModal = () => {
    if (!formLoading) {
      setFormModal(null);
      setFormError(null);
    }
  };

  const handleCreate = async (payload: CreateBuildingPayload | UpdateBuildingPayload) => {
    setFormLoading(true);
    setFormError(null);

    try {
      await createBuilding(payload as CreateBuildingPayload);
      setFormModal(null);
      setSuccessMessage('Budynek został dodany.');
      await refetch();
    } catch (err) {
      setFormError(getBuildingErrorMessage(err));
    } finally {
      setFormLoading(false);
    }
  };

  const handleUpdate = async (payload: CreateBuildingPayload | UpdateBuildingPayload) => {
    if (!formModal?.building) {
      return;
    }

    setFormLoading(true);
    setFormError(null);

    try {
      await updateBuilding(formModal.building.id, payload as UpdateBuildingPayload);
      setFormModal(null);
      setSuccessMessage('Budynek został zaktualizowany.');
      await refetch();
    } catch (err) {
      setFormError(getBuildingErrorMessage(err));
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
      await deactivateBuilding(deactivateTarget.id);
      setDeactivateTarget(null);
      setSuccessMessage('Budynek został dezaktywowany.');
      await refetch();
    } catch (err) {
      setSuccessMessage(null);
      setFormError(getBuildingErrorMessage(err));
    } finally {
      setDeactivateLoading(false);
    }
  };

  const renderContent = () => {
    if (isLoading) {
      return <LoadingState label="Ładowanie budynków…" />;
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

    if (buildings.length === 0) {
      return (
        <EmptyState
          title="BRAK BUDYNKÓW"
          description="Nie znaleziono budynków spełniających kryteria."
          action={
            canCreate ? (
              <Button onClick={openCreateModal}>Dodaj pierwszy budynek</Button>
            ) : undefined
          }
        />
      );
    }

    return (
      <>
        <div className="buildings-page__desktop">
          <BuildingsTable
            buildings={buildings}
            canViewStaircases={canViewStaircases}
            canEdit={canEdit}
            canDelete={canDelete}
            onStaircases={openStaircases}
            onEdit={openEditModal}
            onDeactivate={setDeactivateTarget}
          />
        </div>
        <div className="buildings-page__mobile">
          <BuildingsMobileList
            buildings={buildings}
            canViewStaircases={canViewStaircases}
            canEdit={canEdit}
            canDelete={canDelete}
            onStaircases={openStaircases}
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
        title="Budynki"
        description="Zarządzanie budynkami w organizacji."
        actions={
          canCreate ? (
            <Button onClick={openCreateModal}>
              <Plus size={16} aria-hidden="true" />
              Dodaj budynek
            </Button>
          ) : undefined
        }
      />

      <div className="buildings-page__toolbar">
        <div className="buildings-page__search">
          <Input
            label="Szukaj"
            name="search"
            placeholder="Kod, nazwa, adres, miasto…"
            value={search}
            onChange={(event) => setSearch(event.target.value)}
          />
        </div>

        <label className="buildings-page__filter-label">
          <span>Status</span>
          <select
            className="buildings-page__filter"
            value={statusFilter}
            onChange={(event) => setStatusFilter(event.target.value as StatusFilterValue)}
            aria-label="Filtr statusu"
          >
            <option value="ALL">Wszystkie</option>
            <option value="ACTIVE">Aktywne</option>
            <option value="INACTIVE">Nieaktywne</option>
          </select>
        </label>
      </div>

      {successMessage ? (
        <p className="buildings-page__feedback" role="status">
          {successMessage}
        </p>
      ) : null}

      {renderContent()}

      <Modal
        isOpen={formModal !== null}
        title={formModal?.mode === 'edit' ? 'Edytuj budynek' : 'Dodaj budynek'}
        onClose={closeFormModal}
      >
        {formModal ? (
          <BuildingForm
            key={formModal.building?.id ?? 'create'}
            mode={formModal.mode}
            initialBuilding={formModal.building}
            submitLabel={formModal.mode === 'edit' ? 'Zapisz zmiany' : 'Dodaj budynek'}
            loading={formLoading}
            serverError={formError}
            onSubmit={formModal.mode === 'edit' ? handleUpdate : handleCreate}
            onCancel={closeFormModal}
          />
        ) : null}
      </Modal>

      <DeactivateBuildingDialog
        building={deactivateTarget}
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
