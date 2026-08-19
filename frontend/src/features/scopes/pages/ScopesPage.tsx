import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AppLayoutContainer } from '@/components/layout/AppLayout';
import { EmptyState } from '@/components/ui/EmptyState';
import { ErrorState } from '@/components/ui/ErrorState';
import { Input } from '@/components/ui/Input';
import { LoadingState } from '@/components/ui/LoadingState';
import { PageHeader } from '@/components/ui/PageHeader';
import { useActivities } from '@/features/activities/hooks/useActivities';
import { useBuildings } from '@/features/buildings/hooks/useBuildings';
import { ScopesMobileList } from '@/features/scopes/components/ScopesMobileList';
import { ScopesTable } from '@/features/scopes/components/ScopesTable';
import { useScopes } from '@/features/scopes/hooks/useScopes';
import type { ScopePlanningType, ScopeStatus } from '@/features/scopes/types/scope';
import { filterScopes } from '@/features/scopes/utils/filterScopes';
import './ScopesPage.css';

type BuildingFilterValue = 'ALL' | string;
type ActivityFilterValue = 'ALL' | string;
type PlanningTypeFilterValue = ScopePlanningType | 'ALL';
type StatusFilterValue = ScopeStatus | 'ALL';

export function ScopesPage() {
  const navigate = useNavigate();
  const canEdit = false;

  const [search, setSearch] = useState('');
  const [buildingFilter, setBuildingFilter] = useState<BuildingFilterValue>('ALL');
  const [activityFilter, setActivityFilter] = useState<ActivityFilterValue>('ALL');
  const [planningTypeFilter, setPlanningTypeFilter] = useState<PlanningTypeFilterValue>('ALL');
  const [statusFilter, setStatusFilter] = useState<StatusFilterValue>('ACTIVE');

  const {
    scopes,
    isLoading: scopesLoading,
    error: scopesError,
    forbidden,
    refetch: refetchScopes,
  } = useScopes();

  const {
    buildings,
    isLoading: buildingsLoading,
    error: buildingsError,
    refetch: refetchBuildings,
  } = useBuildings({ status: 'ACTIVE' });

  const {
    activities,
    isLoading: activitiesLoading,
    error: activitiesError,
    refetch: refetchActivities,
  } = useActivities({ active: true });

  const buildingsById = useMemo(
    () => new Map(buildings.map((building) => [building.id, building])),
    [buildings],
  );

  const activitiesById = useMemo(
    () => new Map(activities.map((activity) => [activity.id, activity])),
    [activities],
  );

  const filteredScopes = useMemo(
    () =>
      filterScopes(scopes, buildingsById, activitiesById, {
        search,
        buildingId: buildingFilter,
        activityId: activityFilter,
        planningType: planningTypeFilter,
        status: statusFilter,
      }),
    [
      scopes,
      buildingsById,
      activitiesById,
      search,
      buildingFilter,
      activityFilter,
      planningTypeFilter,
      statusFilter,
    ],
  );

  const isLoading = scopesLoading || buildingsLoading || activitiesLoading;

  const openBuildingScopes = (buildingId: string) => {
    void navigate(`/buildings/${buildingId}/scopes`);
  };

  const handleRetry = () => {
    void refetchScopes();
    void refetchBuildings();
    void refetchActivities();
  };

  const renderContent = () => {
    if (isLoading) {
      return <LoadingState label="Ładowanie zakresów…" />;
    }

    if (forbidden) {
      return (
        <ErrorState
          title="Brak dostępu"
          message={scopesError ?? 'Nie masz uprawnień do tej operacji.'}
        />
      );
    }

    if ((scopesError || buildingsError || activitiesError) && scopes.length === 0) {
      return (
        <ErrorState
          message={scopesError ?? buildingsError ?? activitiesError ?? 'Nie udało się wczytać danych.'}
          onRetry={handleRetry}
        />
      );
    }

    if (filteredScopes.length === 0) {
      return (
        <EmptyState
          title="BRAK ZAKRESÓW"
          description="Nie znaleziono zakresów spełniających kryteria."
        />
      );
    }

    return (
      <>
        <div className="scopes-page__desktop">
          <ScopesTable
            scopes={filteredScopes}
            buildingsById={buildingsById}
            activitiesById={activitiesById}
            showBuilding
            canEdit={canEdit}
            onBuildingClick={openBuildingScopes}
          />
        </div>
        <div className="scopes-page__mobile">
          <ScopesMobileList
            scopes={filteredScopes}
            buildingsById={buildingsById}
            activitiesById={activitiesById}
            showBuilding
            canEdit={canEdit}
            onBuildingClick={openBuildingScopes}
          />
        </div>
      </>
    );
  };

  return (
    <AppLayoutContainer>
      <PageHeader
        title="Zakresy"
        description="Zarządzanie zakresami czynności w organizacji."
      />

      <div className="scopes-page__toolbar">
        <div className="scopes-page__search">
          <Input
            label="Szukaj"
            name="search"
            placeholder="Kod, budynek, czynność…"
            value={search}
            onChange={(event) => setSearch(event.target.value)}
          />
        </div>

        <label className="scopes-page__filter-label">
          <span>Budynek</span>
          <select
            className="scopes-page__filter"
            value={buildingFilter}
            onChange={(event) => setBuildingFilter(event.target.value as BuildingFilterValue)}
            aria-label="Filtr budynku"
          >
            <option value="ALL">Wszystkie budynki</option>
            {buildings.map((building) => (
              <option key={building.id} value={building.id}>
                {building.name} ({building.code})
              </option>
            ))}
          </select>
        </label>

        <label className="scopes-page__filter-label">
          <span>Czynność</span>
          <select
            className="scopes-page__filter"
            value={activityFilter}
            onChange={(event) => setActivityFilter(event.target.value as ActivityFilterValue)}
            aria-label="Filtr czynności"
          >
            <option value="ALL">Wszystkie czynności</option>
            {activities.map((activity) => (
              <option key={activity.id} value={activity.id}>
                {activity.name} ({activity.code})
              </option>
            ))}
          </select>
        </label>

        <label className="scopes-page__filter-label">
          <span>Typ planowania</span>
          <select
            className="scopes-page__filter"
            value={planningTypeFilter}
            onChange={(event) =>
              setPlanningTypeFilter(event.target.value as PlanningTypeFilterValue)
            }
            aria-label="Filtr typu planowania"
          >
            <option value="ALL">Wszystkie</option>
            <option value="WEEKLY">Tygodniowy</option>
            <option value="MONTHLY">Miesięczny</option>
            <option value="YEARLY">Roczny</option>
            <option value="EVENT">Zdarzeniowy</option>
          </select>
        </label>

        <label className="scopes-page__filter-label">
          <span>Status</span>
          <select
            className="scopes-page__filter"
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

      {renderContent()}
    </AppLayoutContainer>
  );
}
