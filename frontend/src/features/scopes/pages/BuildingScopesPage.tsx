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
import { useActivities } from '@/features/activities/hooks/useActivities';
import {
  createScope,
  deactivateScope,
  updateScope,
} from '@/features/scopes/api/scopesApi';
import { DeactivateScopeDialog } from '@/features/scopes/components/DeactivateScopeDialog';
import { ScopeForm } from '@/features/scopes/components/ScopeForm';
import { ScopesMobileList } from '@/features/scopes/components/ScopesMobileList';
import { ScopesTable } from '@/features/scopes/components/ScopesTable';
import { useScopes } from '@/features/scopes/hooks/useScopes';
import { getScopeErrorMessage } from '@/features/scopes/scopesMessages';
import type {
  CreateScopePayload,
  Scope,
  UpdateScopePayload,
} from '@/features/scopes/types/scope';
import { useBuildingContext } from '@/features/staircases/hooks/useBuildingContext';
import { usePermissions } from '@/features/permissions/PermissionProvider';
import './ScopesPage.css';

interface FormModalState {
  mode: 'create' | 'edit';
  scope?: Scope;
}

export function BuildingScopesPage() {
  const { buildingId = '' } = useParams<{ buildingId: string }>();
  const { hasPermission } = usePermissions();
  const canCreate = hasPermission('SCOPES_CREATE');
  const canEdit = hasPermission('SCOPES_EDIT');
  const canDelete = hasPermission('SCOPES_DELETE');

  const {
    building,
    isLoading: buildingLoading,
    error: buildingError,
    notFound: buildingNotFound,
    refetch: refetchBuilding,
  } = useBuildingContext(buildingId);

  const {
    scopes,
    isLoading: scopesLoading,
    error: scopesError,
    forbidden,
    refetch: refetchScopes,
  } = useScopes(buildingId);

  const {
    activities,
    isLoading: activitiesLoading,
    error: activitiesError,
    refetch: refetchActivities,
  } = useActivities({ active: true });

  const activitiesById = new Map(activities.map((activity) => [activity.id, activity]));

  const [formModal, setFormModal] = useState<FormModalState | null>(null);
  const [deactivateTarget, setDeactivateTarget] = useState<Scope | null>(null);
  const [formLoading, setFormLoading] = useState(false);
  const [deactivateLoading, setDeactivateLoading] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const isLoading = buildingLoading || scopesLoading || activitiesLoading;

  const openCreateModal = () => {
    setFormError(null);
    setFormModal({ mode: 'create' });
  };

  const openEditModal = (scope: Scope) => {
    setFormError(null);
    setFormModal({ mode: 'edit', scope });
  };

  const closeFormModal = () => {
    if (!formLoading) {
      setFormModal(null);
      setFormError(null);
    }
  };

  const handleCreate = async (payload: CreateScopePayload | UpdateScopePayload) => {
    setFormLoading(true);
    setFormError(null);

    try {
      await createScope({
        ...(payload as CreateScopePayload),
        buildingId,
      });
      setFormModal(null);
      setSuccessMessage('Zakres został dodany.');
      await refetchScopes();
    } catch (err) {
      setFormError(getScopeErrorMessage(err));
    } finally {
      setFormLoading(false);
    }
  };

  const handleUpdate = async (payload: CreateScopePayload | UpdateScopePayload) => {
    if (!formModal?.scope) {
      return;
    }

    setFormLoading(true);
    setFormError(null);

    try {
      await updateScope(formModal.scope.id, payload as UpdateScopePayload);
      setFormModal(null);
      setSuccessMessage('Zakres został zaktualizowany.');
      await refetchScopes();
    } catch (err) {
      setFormError(getScopeErrorMessage(err));
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
      await deactivateScope(deactivateTarget.id);
      setDeactivateTarget(null);
      setSuccessMessage('Zakres został dezaktywowany.');
      await refetchScopes();
    } catch (err) {
      setSuccessMessage(null);
      setFormError(getScopeErrorMessage(err));
    } finally {
      setDeactivateLoading(false);
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
      return <LoadingState label="Ładowanie zakresów…" />;
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
          message={scopesError ?? 'Nie masz uprawnień do tej operacji.'}
        />
      );
    }

    if ((scopesError || activitiesError) && scopes.length === 0) {
      return (
        <ErrorState
          message={scopesError ?? activitiesError ?? 'Nie udało się wczytać danych.'}
          onRetry={() => {
            void refetchScopes();
            void refetchActivities();
          }}
        />
      );
    }

    if (scopes.length === 0) {
      return (
        <EmptyState
          title="BRAK ZAKRESÓW"
          description="Nie znaleziono zakresów w tym budynku."
          action={
            canCreate ? (
              <Button onClick={openCreateModal}>Dodaj pierwszy zakres</Button>
            ) : undefined
          }
        />
      );
    }

    return (
      <>
        <div className="scopes-page__desktop">
          <ScopesTable
            scopes={scopes}
            activitiesById={activitiesById}
            canEdit={canEdit}
            canDelete={canDelete}
            onEdit={openEditModal}
            onDeactivate={setDeactivateTarget}
          />
        </div>
        <div className="scopes-page__mobile">
          <ScopesMobileList
            scopes={scopes}
            activitiesById={activitiesById}
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
      <div className="scopes-page__back">
        <Link to="/buildings" className="scopes-page__back-link">
          <ArrowLeft size={16} aria-hidden="true" />
          Wróć do budynku
        </Link>
      </div>

      <PageHeader
        title="Zakresy"
        description={pageDescription}
        actions={
          canCreate ? (
            <Button onClick={openCreateModal}>
              <Plus size={16} aria-hidden="true" />
              Dodaj zakres
            </Button>
          ) : undefined
        }
      />

      {successMessage ? (
        <p className="scopes-page__feedback" role="status">
          {successMessage}
        </p>
      ) : null}

      {formError && !formModal ? (
        <p className="scopes-page__feedback scopes-page__feedback--error" role="alert">
          {formError}
        </p>
      ) : null}

      {renderContent()}

      <Modal
        isOpen={formModal !== null}
        title={formModal?.mode === 'edit' ? 'Edytuj zakres' : 'Dodaj zakres'}
        onClose={closeFormModal}
        size="large"
      >
        {formModal && building ? (
          <ScopeForm
            key={formModal.scope?.id ?? 'create'}
            mode={formModal.mode}
            initialScope={formModal.scope}
            buildings={[building]}
            activities={activities}
            fixedBuildingId={buildingId}
            submitLabel={formModal.mode === 'edit' ? 'Zapisz zmiany' : 'Dodaj zakres'}
            loading={formLoading}
            serverError={formError}
            onSubmit={formModal.mode === 'edit' ? handleUpdate : handleCreate}
            onCancel={closeFormModal}
          />
        ) : null}
      </Modal>

      <DeactivateScopeDialog
        scope={deactivateTarget}
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
