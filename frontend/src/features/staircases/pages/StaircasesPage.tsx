import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AppLayoutContainer } from '@/components/layout/AppLayout';
import { EmptyState } from '@/components/ui/EmptyState';
import { ErrorState } from '@/components/ui/ErrorState';
import { Input } from '@/components/ui/Input';
import { LoadingState } from '@/components/ui/LoadingState';
import { PageHeader } from '@/components/ui/PageHeader';
import { useBuildings } from '@/features/buildings/hooks/useBuildings';
import { StaircasesMobileList } from '@/features/staircases/components/StaircasesMobileList';
import { StaircasesTable } from '@/features/staircases/components/StaircasesTable';
import { useStaircases } from '@/features/staircases/hooks/useStaircases';
import {
  filterStaircases,
  type BooleanFilterValue,
} from '@/features/staircases/utils/filterStaircases';
import './StaircasesPage.css';

type BuildingFilterValue = 'ALL' | string;

export function StaircasesPage() {
  const navigate = useNavigate();
  const canEdit = false;

  const [search, setSearch] = useState('');
  const [buildingFilter, setBuildingFilter] = useState<BuildingFilterValue>('ALL');
  const [elevatorFilter, setElevatorFilter] = useState<BooleanFilterValue>('ALL');
  const [keyRequiredFilter, setKeyRequiredFilter] = useState<BooleanFilterValue>('ALL');

  const {
    staircases,
    isLoading: staircasesLoading,
    error: staircasesError,
    forbidden,
    refetch: refetchStaircases,
  } = useStaircases();

  const {
    buildings,
    isLoading: buildingsLoading,
    error: buildingsError,
    refetch: refetchBuildings,
  } = useBuildings({ status: 'ACTIVE' });

  const buildingsById = useMemo(
    () => new Map(buildings.map((building) => [building.id, building])),
    [buildings],
  );

  const filteredStaircases = useMemo(
    () =>
      filterStaircases(staircases, buildingsById, {
        search,
        buildingId: buildingFilter,
        elevator: elevatorFilter,
        keyRequired: keyRequiredFilter,
      }),
    [staircases, buildingsById, search, buildingFilter, elevatorFilter, keyRequiredFilter],
  );

  const isLoading = staircasesLoading || buildingsLoading;

  const openBuildingStaircases = (buildingId: string) => {
    void navigate(`/buildings/${buildingId}/staircases`);
  };

  const handleRetry = () => {
    void refetchStaircases();
    void refetchBuildings();
  };

  const renderContent = () => {
    if (isLoading) {
      return <LoadingState label="Ładowanie klatek…" />;
    }

    if (forbidden) {
      return (
        <ErrorState
          title="Brak dostępu"
          message={staircasesError ?? 'Nie masz uprawnień do tej operacji.'}
        />
      );
    }

    if ((staircasesError || buildingsError) && staircases.length === 0) {
      return (
        <ErrorState
          message={staircasesError ?? buildingsError ?? 'Nie udało się wczytać danych.'}
          onRetry={handleRetry}
        />
      );
    }

    if (filteredStaircases.length === 0) {
      return (
        <EmptyState
          title="BRAK KLATEK SCHODOWYCH"
          description="Nie znaleziono klatek spełniających kryteria."
        />
      );
    }

    return (
      <>
        <div className="staircases-page__desktop">
          <StaircasesTable
            staircases={filteredStaircases}
            buildingsById={buildingsById}
            showBuilding
            canEdit={canEdit}
            onBuildingClick={openBuildingStaircases}
          />
        </div>
        <div className="staircases-page__mobile">
          <StaircasesMobileList
            staircases={filteredStaircases}
            buildingsById={buildingsById}
            showBuilding
            canEdit={canEdit}
            onBuildingClick={openBuildingStaircases}
          />
        </div>
      </>
    );
  };

  return (
    <AppLayoutContainer>
      <PageHeader
        title="Klatki schodowe"
        description="Zarządzanie klatkami schodowymi w organizacji."
      />

      <div className="staircases-page__toolbar">
        <div className="staircases-page__search">
          <Input
            label="Szukaj"
            name="search"
            placeholder="Kod klatki, budynek, adres..."
            value={search}
            onChange={(event) => setSearch(event.target.value)}
          />
        </div>

        <label className="staircases-page__filter-label">
          <span>Budynek</span>
          <select
            className="staircases-page__filter"
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

        <label className="staircases-page__filter-label">
          <span>Winda</span>
          <select
            className="staircases-page__filter"
            value={elevatorFilter}
            onChange={(event) => setElevatorFilter(event.target.value as BooleanFilterValue)}
            aria-label="Filtr windy"
          >
            <option value="ALL">Wszystkie</option>
            <option value="YES">Tak</option>
            <option value="NO">Nie</option>
          </select>
        </label>

        <label className="staircases-page__filter-label">
          <span>Klucz wymagany</span>
          <select
            className="staircases-page__filter"
            value={keyRequiredFilter}
            onChange={(event) => setKeyRequiredFilter(event.target.value as BooleanFilterValue)}
            aria-label="Filtr klucza wymaganego"
          >
            <option value="ALL">Wszystkie</option>
            <option value="YES">Tak</option>
            <option value="NO">Nie</option>
          </select>
        </label>
      </div>

      {renderContent()}
    </AppLayoutContainer>
  );
}
