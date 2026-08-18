import { useState } from 'react';
import { ArrowLeft, Plus } from 'lucide-react';
import { Link, useParams } from 'react-router-dom';
import { AppLayoutContainer } from '@/components/layout/AppLayout';
import { Button } from '@/components/ui/Button';
import { EmptyState } from '@/components/ui/EmptyState';
import { ErrorState } from '@/components/ui/ErrorState';
import { LoadingState } from '@/components/ui/LoadingState';
import { Modal } from '@/components/ui/Modal';
import { PageHeader } from '@/components/ui/PageHeader';
import {
  createStaircase,
  deleteStaircase,
  updateStaircase,
} from '@/features/staircases/api/staircasesApi';
import { DeleteStaircaseDialog } from '@/features/staircases/components/DeleteStaircaseDialog';
import { StaircaseForm } from '@/features/staircases/components/StaircaseForm';
import { StaircasesMobileList } from '@/features/staircases/components/StaircasesMobileList';
import { StaircasesTable } from '@/features/staircases/components/StaircasesTable';
import { useBuildingContext } from '@/features/staircases/hooks/useBuildingContext';
import { useStaircases } from '@/features/staircases/hooks/useStaircases';
import { getStaircaseErrorMessage } from '@/features/staircases/staircasesMessages';
import type {
  CreateStaircasePayload,
  Staircase,
  UpdateStaircasePayload,
} from '@/features/staircases/types/staircase';
import { usePermissions } from '@/features/permissions/PermissionProvider';
import './StaircasesPage.css';

interface FormModalState {
  mode: 'create' | 'edit';
  staircase?: Staircase;
}

export function StaircasesPage() {
  const { buildingId = '' } = useParams<{ buildingId: string }>();
  const { hasPermission } = usePermissions();
  const canEdit = hasPermission('BUILDINGS_EDIT');

  const {
    building,
    isLoading: buildingLoading,
    error: buildingError,
    notFound: buildingNotFound,
    refetch: refetchBuilding,
  } = useBuildingContext(buildingId);

  const {
    staircases,
    isLoading: staircasesLoading,
    error: staircasesError,
    forbidden,
    refetch: refetchStaircases,
  } = useStaircases(buildingId);

  const [formModal, setFormModal] = useState<FormModalState | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<Staircase | null>(null);
  const [formLoading, setFormLoading] = useState(false);
  const [deleteLoading, setDeleteLoading] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const isLoading = buildingLoading || staircasesLoading;

  const openCreateModal = () => {
    setFormError(null);
    setFormModal({ mode: 'create' });
  };

  const openEditModal = (staircase: Staircase) => {
    setFormError(null);
    setFormModal({ mode: 'edit', staircase });
  };

  const closeFormModal = () => {
    if (!formLoading) {
      setFormModal(null);
      setFormError(null);
    }
  };

  const handleCreate = async (payload: CreateStaircasePayload | UpdateStaircasePayload) => {
    setFormLoading(true);
    setFormError(null);

    try {
      await createStaircase({
        ...(payload as CreateStaircasePayload),
        buildingId,
      });
      setFormModal(null);
      setSuccessMessage('Klatka została dodana.');
      await refetchStaircases();
    } catch (err) {
      setFormError(getStaircaseErrorMessage(err));
    } finally {
      setFormLoading(false);
    }
  };

  const handleUpdate = async (payload: CreateStaircasePayload | UpdateStaircasePayload) => {
    if (!formModal?.staircase) {
      return;
    }

    setFormLoading(true);
    setFormError(null);

    try {
      await updateStaircase(formModal.staircase.id, payload as UpdateStaircasePayload);
      setFormModal(null);
      setSuccessMessage('Klatka została zaktualizowana.');
      await refetchStaircases();
    } catch (err) {
      setFormError(getStaircaseErrorMessage(err));
    } finally {
      setFormLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!deleteTarget) {
      return;
    }

    setDeleteLoading(true);

    try {
      await deleteStaircase(deleteTarget.id);
      setDeleteTarget(null);
      setSuccessMessage('Klatka została usunięta.');
      await refetchStaircases();
    } catch (err) {
      setSuccessMessage(null);
      setFormError(getStaircaseErrorMessage(err));
    } finally {
      setDeleteLoading(false);
    }
  };

  const buildingContextLine = building
    ? `${building.name} · ${building.code}`
    : null;
  const buildingAddressLine = building ? `${building.address}, ${building.city}` : null;
  const pageDescription =
    buildingContextLine && buildingAddressLine
      ? `${buildingContextLine} — ${buildingAddressLine}`
      : buildingError ?? undefined;

  const renderContent = () => {
    if (isLoading) {
      return <LoadingState label="Ładowanie klatek…" />;
    }

    if (buildingNotFound) {
      return (
        <ErrorState
          title="Budynek nie znaleziony"
          message={buildingError ?? 'Nie znaleziono budynku.'}
          onRetry={() => void refetchBuilding()}
        />
      );
    }

    if (forbidden) {
      return (
        <ErrorState
          title="Brak dostępu"
          message={staircasesError ?? 'Nie masz uprawnień do tej operacji.'}
        />
      );
    }

    if (staircasesError && staircases.length === 0) {
      return (
        <ErrorState
          message={staircasesError}
          onRetry={() => void refetchStaircases()}
        />
      );
    }

    if (staircases.length === 0) {
      return (
        <EmptyState
          title="BRAK KLATEK"
          description="Nie znaleziono klatek w tym budynku."
          action={
            canEdit ? (
              <Button onClick={openCreateModal}>Dodaj pierwszą klatkę</Button>
            ) : undefined
          }
        />
      );
    }

    return (
      <>
        <div className="staircases-page__desktop">
          <StaircasesTable
            staircases={staircases}
            canEdit={canEdit}
            onEdit={openEditModal}
            onDelete={setDeleteTarget}
          />
        </div>
        <div className="staircases-page__mobile">
          <StaircasesMobileList
            staircases={staircases}
            canEdit={canEdit}
            onEdit={openEditModal}
            onDelete={setDeleteTarget}
          />
        </div>
      </>
    );
  };

  return (
    <AppLayoutContainer>
      <div className="staircases-page__back">
        <Link to="/buildings" className="staircases-page__back-link">
          <ArrowLeft size={16} aria-hidden="true" />
          Wróć do budynku
        </Link>
      </div>

      <PageHeader
        title="Klatki schodowe"
        description={pageDescription}
        actions={
          canEdit ? (
            <Button onClick={openCreateModal}>
              <Plus size={16} aria-hidden="true" />
              Dodaj klatkę
            </Button>
          ) : undefined
        }
      />

      {successMessage ? (
        <p className="staircases-page__feedback" role="status">
          {successMessage}
        </p>
      ) : null}

      {formError && !formModal ? (
        <p className="staircases-page__feedback staircases-page__feedback--error" role="alert">
          {formError}
        </p>
      ) : null}

      {renderContent()}

      <Modal
        isOpen={formModal !== null}
        title={formModal?.mode === 'edit' ? 'Edytuj klatkę' : 'Dodaj klatkę'}
        onClose={closeFormModal}
      >
        {formModal ? (
          <StaircaseForm
            key={formModal.staircase?.id ?? 'create'}
            mode={formModal.mode}
            initialStaircase={formModal.staircase}
            submitLabel={formModal.mode === 'edit' ? 'Zapisz zmiany' : 'Dodaj klatkę'}
            loading={formLoading}
            serverError={formError}
            onSubmit={formModal.mode === 'edit' ? handleUpdate : handleCreate}
            onCancel={closeFormModal}
          />
        ) : null}
      </Modal>

      <DeleteStaircaseDialog
        staircase={deleteTarget}
        loading={deleteLoading}
        onConfirm={() => void handleDelete()}
        onCancel={() => {
          if (!deleteLoading) {
            setDeleteTarget(null);
          }
        }}
      />
    </AppLayoutContainer>
  );
}
