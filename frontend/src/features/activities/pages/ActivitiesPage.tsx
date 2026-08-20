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
  createActivity,
  deactivateActivity,
  updateActivity,
} from '@/features/activities/api/activitiesApi';
import { ActivitiesMobileList } from '@/features/activities/components/ActivitiesMobileList';
import { ActivitiesTable } from '@/features/activities/components/ActivitiesTable';
import { ActivityForm } from '@/features/activities/components/ActivityForm';
import { DeactivateActivityDialog } from '@/features/activities/components/DeactivateActivityDialog';
import { getActivityErrorMessage } from '@/features/activities/activitiesMessages';
import { useActivities } from '@/features/activities/hooks/useActivities';
import type {
  Activity,
  ActivityPlanningType,
  CreateActivityPayload,
  UpdateActivityPayload,
} from '@/features/activities/types/activity';
import { usePermissions } from '@/features/permissions/PermissionProvider';
import './ActivitiesPage.css';

type PlanningTypeFilterValue = ActivityPlanningType | 'ALL';
type ActiveFilterValue = 'ALL' | 'ACTIVE' | 'INACTIVE';

interface FormModalState {
  mode: 'create' | 'edit';
  activity?: Activity;
}

export function ActivitiesPage() {
  const { hasPermission } = usePermissions();
  const canCreate = hasPermission('ACTIVITIES_CREATE');
  const canCreateSystemActivity = hasPermission('ACTIVITIES_ADMIN');
  const canDelete = hasPermission('ACTIVITIES_DELETE');

  const [search, setSearch] = useState('');
  const [category, setCategory] = useState('');
  const [planningTypeFilter, setPlanningTypeFilter] = useState<PlanningTypeFilterValue>('ALL');
  const [activeFilter, setActiveFilter] = useState<ActiveFilterValue>('ACTIVE');
  const [formModal, setFormModal] = useState<FormModalState | null>(null);
  const [deactivateTarget, setDeactivateTarget] = useState<Activity | null>(null);
  const [formLoading, setFormLoading] = useState(false);
  const [deactivateLoading, setDeactivateLoading] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const listParams = useMemo(
    () => ({
      search,
      category,
      planningType: planningTypeFilter === 'ALL' ? null : planningTypeFilter,
      active:
        activeFilter === 'ALL'
          ? null
          : activeFilter === 'ACTIVE',
    }),
    [search, category, planningTypeFilter, activeFilter],
  );

  const { activities, isLoading, error, forbidden, unauthorized, refetch } =
    useActivities(listParams);

  const openCreateModal = () => {
    setFormError(null);
    setFormModal({ mode: 'create' });
  };

  const openEditModal = (activity: Activity) => {
    setFormError(null);
    setFormModal({ mode: 'edit', activity });
  };

  const closeFormModal = () => {
    if (!formLoading) {
      setFormModal(null);
      setFormError(null);
    }
  };

  const handleCreate = async (payload: CreateActivityPayload | UpdateActivityPayload) => {
    setFormLoading(true);
    setFormError(null);

    try {
      await createActivity(payload as CreateActivityPayload);
      setFormModal(null);
      setSuccessMessage('Czynność została dodana.');
      await refetch();
    } catch (err) {
      setFormError(getActivityErrorMessage(err));
    } finally {
      setFormLoading(false);
    }
  };

  const handleUpdate = async (payload: CreateActivityPayload | UpdateActivityPayload) => {
    if (!formModal?.activity) {
      return;
    }

    setFormLoading(true);
    setFormError(null);

    try {
      await updateActivity(formModal.activity.id, payload as UpdateActivityPayload);
      setFormModal(null);
      setSuccessMessage('Czynność została zaktualizowana.');
      await refetch();
    } catch (err) {
      setFormError(getActivityErrorMessage(err));
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
      await deactivateActivity(deactivateTarget.id);
      setDeactivateTarget(null);
      setSuccessMessage('Czynność została dezaktywowana.');
      await refetch();
    } catch (err) {
      setSuccessMessage(null);
      setFormError(getActivityErrorMessage(err));
    } finally {
      setDeactivateLoading(false);
    }
  };

  const renderContent = () => {
    if (isLoading) {
      return <LoadingState label="Ładowanie czynności…" />;
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

    if (activities.length === 0) {
      return (
        <EmptyState
          title="BRAK CZYNNOŚCI"
          description="Nie znaleziono czynności spełniających kryteria."
          action={
            canCreate ? (
              <Button onClick={openCreateModal}>Dodaj pierwszą czynność</Button>
            ) : undefined
          }
        />
      );
    }

    return (
      <>
        <div className="activities-page__desktop">
          <ActivitiesTable
            activities={activities}
            canDelete={canDelete}
            onEdit={openEditModal}
            onDeactivate={setDeactivateTarget}
          />
        </div>
        <div className="activities-page__mobile">
          <ActivitiesMobileList
            activities={activities}
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
        title="Katalog czynności"
        description="Katalog czynności systemowych M2 oraz czynności własnych organizacji."
        actions={
          canCreate ? (
            <Button onClick={openCreateModal}>
              <Plus size={16} aria-hidden="true" />
              Dodaj czynność
            </Button>
          ) : undefined
        }
      />

      <div className="activities-page__toolbar">
        <div className="activities-page__search">
          <Input
            label="Szukaj"
            name="search"
            placeholder="Kod, nazwa, kategoria…"
            value={search}
            onChange={(event) => setSearch(event.target.value)}
          />
        </div>

        <div className="activities-page__search">
          <Input
            label="Kategoria"
            name="category"
            placeholder="Filtruj po kategorii…"
            value={category}
            onChange={(event) => setCategory(event.target.value)}
          />
        </div>

        <label className="activities-page__filter-label">
          <span>Typ planowania</span>
          <select
            className="activities-page__filter"
            value={planningTypeFilter}
            onChange={(event) =>
              setPlanningTypeFilter(event.target.value as PlanningTypeFilterValue)
            }
            aria-label="Filtr typu planowania"
          >
            <option value="ALL">Wszystkie</option>
            <option value="CYCLIC">Cykliczna</option>
            <option value="PERIODIC">Okresowa</option>
            <option value="ON_DEMAND">Na żądanie</option>
          </select>
        </label>

        <label className="activities-page__filter-label">
          <span>Status</span>
          <select
            className="activities-page__filter"
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
        <p className="activities-page__feedback" role="status">
          {successMessage}
        </p>
      ) : null}

      {renderContent()}

      <Modal
        isOpen={formModal !== null}
        title={formModal?.mode === 'edit' ? 'Edytuj czynność' : 'Dodaj czynność'}
        onClose={closeFormModal}
      >
        {formModal ? (
          <ActivityForm
            key={formModal.activity?.id ?? 'create'}
            mode={formModal.mode}
            initialActivity={formModal.activity}
            canCreateSystemActivity={canCreateSystemActivity}
            submitLabel={formModal.mode === 'edit' ? 'Zapisz zmiany' : 'Dodaj czynność'}
            loading={formLoading}
            serverError={formError}
            onSubmit={formModal.mode === 'edit' ? handleUpdate : handleCreate}
            onCancel={closeFormModal}
          />
        ) : null}
      </Modal>

      <DeactivateActivityDialog
        activity={deactivateTarget}
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
