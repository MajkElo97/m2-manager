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
  createChemical,
  deactivateChemical,
  updateChemical,
} from '@/features/inventory/api/chemicalsApi';
import {
  createEquipment,
  deactivateEquipment,
  updateEquipment,
} from '@/features/inventory/api/equipmentApi';
import { ChemicalForm } from '@/features/inventory/components/ChemicalForm';
import { ChemicalsMobileList } from '@/features/inventory/components/ChemicalsMobileList';
import { ChemicalsTable } from '@/features/inventory/components/ChemicalsTable';
import { DeactivateChemicalDialog } from '@/features/inventory/components/DeactivateChemicalDialog';
import { DeactivateEquipmentDialog } from '@/features/inventory/components/DeactivateEquipmentDialog';
import { EquipmentForm } from '@/features/inventory/components/EquipmentForm';
import { EquipmentMobileList } from '@/features/inventory/components/EquipmentMobileList';
import { EquipmentTable } from '@/features/inventory/components/EquipmentTable';
import { useChemicals } from '@/features/inventory/hooks/useChemicals';
import { useEquipment } from '@/features/inventory/hooks/useEquipment';
import { getInventoryErrorMessage } from '@/features/inventory/inventoryMessages';
import type {
  CreateChemicalPayload,
  Chemical,
  UpdateChemicalPayload,
} from '@/features/inventory/types/chemical';
import type {
  CreateEquipmentPayload,
  Equipment,
  EquipmentCondition,
  UpdateEquipmentPayload,
} from '@/features/inventory/types/equipment';
import { usePermissions } from '@/features/permissions/PermissionProvider';
import './InventoryPage.css';

type InventoryTab = 'equipment' | 'chemicals';
type ActiveFilterValue = 'ALL' | 'ACTIVE' | 'INACTIVE';
type ConditionFilterValue = EquipmentCondition | 'ALL';
type LowStockFilterValue = 'ALL' | 'LOW' | 'OK';

interface EquipmentFormModalState {
  mode: 'create' | 'edit';
  equipment?: Equipment;
}

interface ChemicalFormModalState {
  mode: 'create' | 'edit';
  chemical?: Chemical;
}

export function InventoryPage() {
  const { hasPermission } = usePermissions();
  const canCreate = hasPermission('WAREHOUSE_CREATE');
  const canEdit = hasPermission('WAREHOUSE_EDIT');
  const canDelete = hasPermission('WAREHOUSE_DELETE');

  const [activeTab, setActiveTab] = useState<InventoryTab>('equipment');

  const [equipmentSearch, setEquipmentSearch] = useState('');
  const [equipmentCategory, setEquipmentCategory] = useState('');
  const [equipmentEmployeeId, setEquipmentEmployeeId] = useState('');
  const [equipmentConditionFilter, setEquipmentConditionFilter] =
    useState<ConditionFilterValue>('ALL');
  const [equipmentActiveFilter, setEquipmentActiveFilter] = useState<ActiveFilterValue>('ACTIVE');

  const [chemicalSearch, setChemicalSearch] = useState('');
  const [chemicalCategory, setChemicalCategory] = useState('');
  const [chemicalActiveFilter, setChemicalActiveFilter] = useState<ActiveFilterValue>('ACTIVE');
  const [chemicalLowStockFilter, setChemicalLowStockFilter] =
    useState<LowStockFilterValue>('ALL');

  const [equipmentFormModal, setEquipmentFormModal] = useState<EquipmentFormModalState | null>(
    null,
  );
  const [chemicalFormModal, setChemicalFormModal] = useState<ChemicalFormModalState | null>(null);
  const [deactivateEquipmentTarget, setDeactivateEquipmentTarget] = useState<Equipment | null>(
    null,
  );
  const [deactivateChemicalTarget, setDeactivateChemicalTarget] = useState<Chemical | null>(null);
  const [formLoading, setFormLoading] = useState(false);
  const [deactivateLoading, setDeactivateLoading] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const equipmentListParams = useMemo(
    () => ({
      search: equipmentSearch,
      category: equipmentCategory,
      employeeId: equipmentEmployeeId || null,
      condition: equipmentConditionFilter === 'ALL' ? null : equipmentConditionFilter,
      active:
        equipmentActiveFilter === 'ALL'
          ? null
          : equipmentActiveFilter === 'ACTIVE',
    }),
    [
      equipmentSearch,
      equipmentCategory,
      equipmentEmployeeId,
      equipmentConditionFilter,
      equipmentActiveFilter,
    ],
  );

  const chemicalListParams = useMemo(
    () => ({
      search: chemicalSearch,
      category: chemicalCategory,
      active:
        chemicalActiveFilter === 'ALL'
          ? null
          : chemicalActiveFilter === 'ACTIVE',
      lowStock:
        chemicalLowStockFilter === 'ALL'
          ? null
          : chemicalLowStockFilter === 'LOW',
    }),
    [chemicalSearch, chemicalCategory, chemicalActiveFilter, chemicalLowStockFilter],
  );

  const {
    equipment,
    isLoading: equipmentLoading,
    error: equipmentError,
    forbidden: equipmentForbidden,
    unauthorized: equipmentUnauthorized,
    refetch: refetchEquipment,
  } = useEquipment(equipmentListParams);

  const {
    chemicals,
    isLoading: chemicalsLoading,
    error: chemicalsError,
    forbidden: chemicalsForbidden,
    unauthorized: chemicalsUnauthorized,
    refetch: refetchChemicals,
  } = useChemicals(chemicalListParams);

  const { employees } = useEmployees({ active: true });

  const openCreateModal = () => {
    setFormError(null);
    if (activeTab === 'equipment') {
      setEquipmentFormModal({ mode: 'create' });
    } else {
      setChemicalFormModal({ mode: 'create' });
    }
  };

  const openEditEquipmentModal = (item: Equipment) => {
    setFormError(null);
    setEquipmentFormModal({ mode: 'edit', equipment: item });
  };

  const openEditChemicalModal = (item: Chemical) => {
    setFormError(null);
    setChemicalFormModal({ mode: 'edit', chemical: item });
  };

  const closeEquipmentFormModal = () => {
    if (!formLoading) {
      setEquipmentFormModal(null);
      setFormError(null);
    }
  };

  const closeChemicalFormModal = () => {
    if (!formLoading) {
      setChemicalFormModal(null);
      setFormError(null);
    }
  };

  const handleCreateEquipment = async (
    payload: CreateEquipmentPayload | UpdateEquipmentPayload,
  ) => {
    setFormLoading(true);
    setFormError(null);

    try {
      await createEquipment(payload as CreateEquipmentPayload);
      setEquipmentFormModal(null);
      setSuccessMessage('Sprzęt został dodany.');
      await refetchEquipment();
    } catch (err) {
      setFormError(getInventoryErrorMessage(err));
    } finally {
      setFormLoading(false);
    }
  };

  const handleUpdateEquipment = async (
    payload: CreateEquipmentPayload | UpdateEquipmentPayload,
  ) => {
    if (!equipmentFormModal?.equipment) {
      return;
    }

    setFormLoading(true);
    setFormError(null);

    try {
      await updateEquipment(
        equipmentFormModal.equipment.id,
        payload as UpdateEquipmentPayload,
      );
      setEquipmentFormModal(null);
      setSuccessMessage('Sprzęt został zaktualizowany.');
      await refetchEquipment();
    } catch (err) {
      setFormError(getInventoryErrorMessage(err));
    } finally {
      setFormLoading(false);
    }
  };

  const handleCreateChemical = async (payload: CreateChemicalPayload | UpdateChemicalPayload) => {
    setFormLoading(true);
    setFormError(null);

    try {
      await createChemical(payload as CreateChemicalPayload);
      setChemicalFormModal(null);
      setSuccessMessage('Chemia została dodana.');
      await refetchChemicals();
    } catch (err) {
      setFormError(getInventoryErrorMessage(err));
    } finally {
      setFormLoading(false);
    }
  };

  const handleUpdateChemical = async (payload: CreateChemicalPayload | UpdateChemicalPayload) => {
    if (!chemicalFormModal?.chemical) {
      return;
    }

    setFormLoading(true);
    setFormError(null);

    try {
      await updateChemical(chemicalFormModal.chemical.id, payload as UpdateChemicalPayload);
      setChemicalFormModal(null);
      setSuccessMessage('Chemia została zaktualizowana.');
      await refetchChemicals();
    } catch (err) {
      setFormError(getInventoryErrorMessage(err));
    } finally {
      setFormLoading(false);
    }
  };

  const handleDeactivateEquipment = async () => {
    if (!deactivateEquipmentTarget) {
      return;
    }

    setDeactivateLoading(true);

    try {
      await deactivateEquipment(deactivateEquipmentTarget.id);
      setDeactivateEquipmentTarget(null);
      setSuccessMessage('Sprzęt został dezaktywowany.');
      await refetchEquipment();
    } catch (err) {
      setSuccessMessage(null);
      setFormError(getInventoryErrorMessage(err));
    } finally {
      setDeactivateLoading(false);
    }
  };

  const handleDeactivateChemical = async () => {
    if (!deactivateChemicalTarget) {
      return;
    }

    setDeactivateLoading(true);

    try {
      await deactivateChemical(deactivateChemicalTarget.id);
      setDeactivateChemicalTarget(null);
      setSuccessMessage('Chemia została dezaktywowana.');
      await refetchChemicals();
    } catch (err) {
      setSuccessMessage(null);
      setFormError(getInventoryErrorMessage(err));
    } finally {
      setDeactivateLoading(false);
    }
  };

  const renderEquipmentContent = () => {
    if (equipmentLoading) {
      return <LoadingState label="Ładowanie sprzętu…" />;
    }

    if (equipmentForbidden) {
      return (
        <ErrorState
          title="Brak dostępu"
          message={equipmentError ?? 'Nie masz uprawnień do tej operacji.'}
        />
      );
    }

    if (equipmentUnauthorized) {
      return (
        <ErrorState
          title="Sesja wygasła"
          message={equipmentError ?? 'Sesja wygasła. Zaloguj się ponownie.'}
        />
      );
    }

    if (equipmentError) {
      return <ErrorState message={equipmentError} onRetry={() => void refetchEquipment()} />;
    }

    if (equipment.length === 0) {
      return (
        <EmptyState
          title="BRAK SPRZĘTU"
          description="Nie znaleziono sprzętu spełniającego kryteria."
          action={
            canCreate ? (
              <Button onClick={openCreateModal}>Dodaj pierwszy sprzęt</Button>
            ) : undefined
          }
        />
      );
    }

    return (
      <>
        <div className="inventory-page__desktop">
          <EquipmentTable
            equipment={equipment}
            canEdit={canEdit}
            canDelete={canDelete}
            onEdit={openEditEquipmentModal}
            onDeactivate={setDeactivateEquipmentTarget}
          />
        </div>
        <div className="inventory-page__mobile">
          <EquipmentMobileList
            equipment={equipment}
            canEdit={canEdit}
            canDelete={canDelete}
            onEdit={openEditEquipmentModal}
            onDeactivate={setDeactivateEquipmentTarget}
          />
        </div>
      </>
    );
  };

  const renderChemicalsContent = () => {
    if (chemicalsLoading) {
      return <LoadingState label="Ładowanie chemii…" />;
    }

    if (chemicalsForbidden) {
      return (
        <ErrorState
          title="Brak dostępu"
          message={chemicalsError ?? 'Nie masz uprawnień do tej operacji.'}
        />
      );
    }

    if (chemicalsUnauthorized) {
      return (
        <ErrorState
          title="Sesja wygasła"
          message={chemicalsError ?? 'Sesja wygasła. Zaloguj się ponownie.'}
        />
      );
    }

    if (chemicalsError) {
      return <ErrorState message={chemicalsError} onRetry={() => void refetchChemicals()} />;
    }

    if (chemicals.length === 0) {
      return (
        <EmptyState
          title="BRAK CHEMII"
          description="Nie znaleziono pozycji chemicznych spełniających kryteria."
          action={
            canCreate ? (
              <Button onClick={openCreateModal}>Dodaj pierwszą pozycję</Button>
            ) : undefined
          }
        />
      );
    }

    return (
      <>
        <div className="inventory-page__desktop">
          <ChemicalsTable
            chemicals={chemicals}
            canEdit={canEdit}
            canDelete={canDelete}
            onEdit={openEditChemicalModal}
            onDeactivate={setDeactivateChemicalTarget}
          />
        </div>
        <div className="inventory-page__mobile">
          <ChemicalsMobileList
            chemicals={chemicals}
            canEdit={canEdit}
            canDelete={canDelete}
            onEdit={openEditChemicalModal}
            onDeactivate={setDeactivateChemicalTarget}
          />
        </div>
      </>
    );
  };

  const createButtonLabel =
    activeTab === 'equipment' ? 'Dodaj sprzęt' : 'Dodaj chemię';

  return (
    <AppLayoutContainer>
      <PageHeader
        title="Magazyn"
        description="Zarządzanie sprzętem i chemią."
        actions={
          canCreate ? (
            <Button onClick={openCreateModal}>
              <Plus size={16} aria-hidden="true" />
              {createButtonLabel}
            </Button>
          ) : undefined
        }
      />

      <div className="inventory-page__tabs" role="tablist" aria-label="Sekcje magazynu">
        <button
          type="button"
          role="tab"
          aria-selected={activeTab === 'equipment'}
          className={`inventory-page__tab${activeTab === 'equipment' ? ' inventory-page__tab--active' : ''}`}
          onClick={() => {
            setActiveTab('equipment');
            setSuccessMessage(null);
          }}
        >
          Sprzęt
        </button>
        <button
          type="button"
          role="tab"
          aria-selected={activeTab === 'chemicals'}
          className={`inventory-page__tab${activeTab === 'chemicals' ? ' inventory-page__tab--active' : ''}`}
          onClick={() => {
            setActiveTab('chemicals');
            setSuccessMessage(null);
          }}
        >
          Chemia
        </button>
      </div>

      {activeTab === 'equipment' ? (
        <div className="inventory-page__toolbar">
          <div className="inventory-page__search">
            <Input
              label="Szukaj"
              name="equipmentSearch"
              placeholder="Kod, nazwa, kategoria…"
              value={equipmentSearch}
              onChange={(event) => setEquipmentSearch(event.target.value)}
            />
          </div>

          <div className="inventory-page__search">
            <Input
              label="Kategoria"
              name="equipmentCategory"
              placeholder="Filtruj po kategorii…"
              value={equipmentCategory}
              onChange={(event) => setEquipmentCategory(event.target.value)}
            />
          </div>

          <label className="inventory-page__filter-label">
            <span>Pracownik</span>
            <select
              className="inventory-page__filter"
              value={equipmentEmployeeId}
              onChange={(event) => setEquipmentEmployeeId(event.target.value)}
              aria-label="Filtr pracownika"
            >
              <option value="">Wszyscy</option>
              {employees.map((employee) => (
                <option key={employee.id} value={employee.id}>
                  {getFullName(employee.firstName, employee.lastName)} ({employee.code})
                </option>
              ))}
            </select>
          </label>

          <label className="inventory-page__filter-label">
            <span>Stan</span>
            <select
              className="inventory-page__filter"
              value={equipmentConditionFilter}
              onChange={(event) =>
                setEquipmentConditionFilter(event.target.value as ConditionFilterValue)
              }
              aria-label="Filtr stanu"
            >
              <option value="ALL">Wszystkie</option>
              <option value="GOOD">Dobry</option>
              <option value="USED">Używany</option>
              <option value="DAMAGED">Uszkodzony</option>
              <option value="OUT_OF_SERVICE">Wyłączony z użytku</option>
            </select>
          </label>

          <label className="inventory-page__filter-label">
            <span>Status</span>
            <select
              className="inventory-page__filter"
              value={equipmentActiveFilter}
              onChange={(event) =>
                setEquipmentActiveFilter(event.target.value as ActiveFilterValue)
              }
              aria-label="Filtr statusu"
            >
              <option value="ALL">Wszystkie</option>
              <option value="ACTIVE">Aktywne</option>
              <option value="INACTIVE">Nieaktywne</option>
            </select>
          </label>
        </div>
      ) : (
        <div className="inventory-page__toolbar">
          <div className="inventory-page__search">
            <Input
              label="Szukaj"
              name="chemicalSearch"
              placeholder="Kod, nazwa, kategoria…"
              value={chemicalSearch}
              onChange={(event) => setChemicalSearch(event.target.value)}
            />
          </div>

          <div className="inventory-page__search">
            <Input
              label="Kategoria"
              name="chemicalCategory"
              placeholder="Filtruj po kategorii…"
              value={chemicalCategory}
              onChange={(event) => setChemicalCategory(event.target.value)}
            />
          </div>

          <label className="inventory-page__filter-label">
            <span>Status magazynowy</span>
            <select
              className="inventory-page__filter"
              value={chemicalLowStockFilter}
              onChange={(event) =>
                setChemicalLowStockFilter(event.target.value as LowStockFilterValue)
              }
              aria-label="Filtr statusu magazynowego"
            >
              <option value="ALL">Wszystkie</option>
              <option value="LOW">Niski stan</option>
              <option value="OK">W normie</option>
            </select>
          </label>

          <label className="inventory-page__filter-label">
            <span>Status</span>
            <select
              className="inventory-page__filter"
              value={chemicalActiveFilter}
              onChange={(event) => setChemicalActiveFilter(event.target.value as ActiveFilterValue)}
              aria-label="Filtr statusu"
            >
              <option value="ALL">Wszystkie</option>
              <option value="ACTIVE">Aktywne</option>
              <option value="INACTIVE">Nieaktywne</option>
            </select>
          </label>
        </div>
      )}

      {successMessage ? (
        <p className="inventory-page__feedback" role="status">
          {successMessage}
        </p>
      ) : null}

      {activeTab === 'equipment' ? renderEquipmentContent() : renderChemicalsContent()}

      <Modal
        isOpen={equipmentFormModal !== null}
        title={
          equipmentFormModal?.mode === 'edit' ? 'Edytuj sprzęt' : 'Dodaj sprzęt'
        }
        onClose={closeEquipmentFormModal}
        size="large"
      >
        {equipmentFormModal ? (
          <EquipmentForm
            key={equipmentFormModal.equipment?.id ?? 'create'}
            mode={equipmentFormModal.mode}
            initialEquipment={equipmentFormModal.equipment}
            submitLabel={
              equipmentFormModal.mode === 'edit' ? 'Zapisz zmiany' : 'Dodaj sprzęt'
            }
            loading={formLoading}
            serverError={formError}
            onSubmit={
              equipmentFormModal.mode === 'edit' ? handleUpdateEquipment : handleCreateEquipment
            }
            onCancel={closeEquipmentFormModal}
          />
        ) : null}
      </Modal>

      <Modal
        isOpen={chemicalFormModal !== null}
        title={chemicalFormModal?.mode === 'edit' ? 'Edytuj chemię' : 'Dodaj chemię'}
        onClose={closeChemicalFormModal}
        size="large"
      >
        {chemicalFormModal ? (
          <ChemicalForm
            key={chemicalFormModal.chemical?.id ?? 'create'}
            mode={chemicalFormModal.mode}
            initialChemical={chemicalFormModal.chemical}
            submitLabel={
              chemicalFormModal.mode === 'edit' ? 'Zapisz zmiany' : 'Dodaj chemię'
            }
            loading={formLoading}
            serverError={formError}
            onSubmit={
              chemicalFormModal.mode === 'edit' ? handleUpdateChemical : handleCreateChemical
            }
            onCancel={closeChemicalFormModal}
          />
        ) : null}
      </Modal>

      <DeactivateEquipmentDialog
        equipment={deactivateEquipmentTarget}
        loading={deactivateLoading}
        onConfirm={() => void handleDeactivateEquipment()}
        onCancel={() => {
          if (!deactivateLoading) {
            setDeactivateEquipmentTarget(null);
          }
        }}
      />

      <DeactivateChemicalDialog
        chemical={deactivateChemicalTarget}
        loading={deactivateLoading}
        onConfirm={() => void handleDeactivateChemical()}
        onCancel={() => {
          if (!deactivateLoading) {
            setDeactivateChemicalTarget(null);
          }
        }}
      />
    </AppLayoutContainer>
  );
}
