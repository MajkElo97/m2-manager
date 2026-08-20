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
import { useBuildings } from '@/features/buildings/hooks/useBuildings';
import {
  cancelTransaction,
  createCategory,
  createTransaction,
  deactivateCategory,
  updateCategory,
  updateTransaction,
} from '@/features/finance/api/financeApi';
import { CancelTransactionDialog } from '@/features/finance/components/CancelTransactionDialog';
import { CategoriesMobileList } from '@/features/finance/components/CategoriesMobileList';
import { CategoriesTable } from '@/features/finance/components/CategoriesTable';
import { CategoryForm } from '@/features/finance/components/CategoryForm';
import { DateRangePicker } from '@/features/finance/components/DateRangePicker';
import { DeactivateCategoryDialog } from '@/features/finance/components/DeactivateCategoryDialog';
import { FinanceIncomeExpenseBar } from '@/features/finance/components/FinanceIncomeExpenseBar';
import { FinanceSummaryCards } from '@/features/finance/components/FinanceSummaryCards';
import { TransactionForm } from '@/features/finance/components/TransactionForm';
import { TransactionsMobileList } from '@/features/finance/components/TransactionsMobileList';
import { TransactionsTable } from '@/features/finance/components/TransactionsTable';
import { getFinanceErrorMessage } from '@/features/finance/financeMessages';
import { useCategories } from '@/features/finance/hooks/useCategories';
import { useFinanceSummary } from '@/features/finance/hooks/useFinanceSummary';
import { useTransactions } from '@/features/finance/hooks/useTransactions';
import type {
  CreateCategoryPayload,
  FinancialCategory,
  UpdateCategoryPayload,
} from '@/features/finance/types/category';
import type {
  CreateTransactionPayload,
  FinancialTransaction,
  PaymentStatus,
  TransactionType,
  UpdateTransactionPayload,
} from '@/features/finance/types/transaction';
import {
  getPaymentStatusLabel,
  getTransactionTypeLabel,
} from '@/features/finance/utils/financeLabels';
import {
  getDateRangeForPreset,
  getDefaultDateRange,
  type DateRange,
  type DateRangePreset,
} from '@/features/finance/utils/dateRangePresets';
import { usePermissions } from '@/features/permissions/PermissionProvider';
import './FinancePage.css';

type FinanceTab = 'transactions' | 'categories';
type TypeFilterValue = TransactionType | 'ALL';
type PaymentFilterValue = PaymentStatus | 'ALL';
type BuildingFilterValue = string | 'ALL';
type CategoryFilterValue = string | 'ALL';
type ActiveFilterValue = 'ALL' | 'ACTIVE' | 'INACTIVE';

interface TransactionFormModalState {
  mode: 'create' | 'edit';
  transaction?: FinancialTransaction;
}

interface CategoryFormModalState {
  mode: 'create' | 'edit';
  category?: FinancialCategory;
}

const TRANSACTION_TYPES: TransactionType[] = ['INCOME', 'EXPENSE'];
const PAYMENT_STATUSES: PaymentStatus[] = ['NOT_APPLICABLE', 'TO_PAY', 'PAID', 'OVERDUE'];

export function FinancePage() {
  const { hasPermission } = usePermissions();
  const canCreate = hasPermission('FINANCES_CREATE');
  const canEdit = hasPermission('FINANCES_EDIT');
  const canDelete = hasPermission('FINANCES_DELETE');

  const [activeTab, setActiveTab] = useState<FinanceTab>('transactions');
  const [datePreset, setDatePreset] = useState<DateRangePreset>('CURRENT_MONTH');
  const [dateRange, setDateRange] = useState<DateRange>(getDefaultDateRange);

  const [transactionSearch, setTransactionSearch] = useState('');
  const [typeFilter, setTypeFilter] = useState<TypeFilterValue>('ALL');
  const [categoryFilter, setCategoryFilter] = useState<CategoryFilterValue>('ALL');
  const [buildingFilter, setBuildingFilter] = useState<BuildingFilterValue>('ALL');
  const [paymentFilter, setPaymentFilter] = useState<PaymentFilterValue>('ALL');

  const [categorySearch, setCategorySearch] = useState('');
  const [categoryTypeFilter, setCategoryTypeFilter] = useState<TypeFilterValue>('ALL');
  const [categoryActiveFilter, setCategoryActiveFilter] = useState<ActiveFilterValue>('ACTIVE');

  const [transactionFormModal, setTransactionFormModal] = useState<TransactionFormModalState | null>(
    null,
  );
  const [categoryFormModal, setCategoryFormModal] = useState<CategoryFormModalState | null>(null);
  const [cancelTarget, setCancelTarget] = useState<FinancialTransaction | null>(null);
  const [deactivateCategoryTarget, setDeactivateCategoryTarget] = useState<FinancialCategory | null>(
    null,
  );
  const [formLoading, setFormLoading] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const transactionListParams = useMemo(
    () => ({
      search: transactionSearch,
      type: typeFilter === 'ALL' ? null : typeFilter,
      categoryId: categoryFilter === 'ALL' ? null : categoryFilter,
      buildingId: buildingFilter === 'ALL' ? null : buildingFilter,
      paymentStatus: paymentFilter === 'ALL' ? null : paymentFilter,
      status: 'ACTIVE' as const,
      dateFrom: dateRange.dateFrom,
      dateTo: dateRange.dateTo,
    }),
    [
      transactionSearch,
      typeFilter,
      categoryFilter,
      buildingFilter,
      paymentFilter,
      dateRange.dateFrom,
      dateRange.dateTo,
    ],
  );

  const categoryListParams = useMemo(
    () => ({
      search: categorySearch,
      type: categoryTypeFilter === 'ALL' ? null : categoryTypeFilter,
      active:
        categoryActiveFilter === 'ALL'
          ? null
          : categoryActiveFilter === 'ACTIVE',
    }),
    [categorySearch, categoryTypeFilter, categoryActiveFilter],
  );

  const {
    transactions,
    isLoading: transactionsLoading,
    error: transactionsError,
    forbidden: transactionsForbidden,
    unauthorized: transactionsUnauthorized,
    refetch: refetchTransactions,
  } = useTransactions(transactionListParams);

  const {
    categories,
    isLoading: categoriesLoading,
    error: categoriesError,
    forbidden: categoriesForbidden,
    unauthorized: categoriesUnauthorized,
    refetch: refetchCategories,
  } = useCategories(categoryListParams);

  const {
    summary,
    isLoading: summaryLoading,
    error: summaryError,
    forbidden: summaryForbidden,
    refetch: refetchSummary,
  } = useFinanceSummary(dateRange);

  const { buildings } = useBuildings({ status: 'ACTIVE' });

  const allCategoriesParams = useMemo(() => ({ active: true }), []);
  const { categories: allCategories } = useCategories(allCategoriesParams);

  const handlePresetChange = (preset: DateRangePreset) => {
    setDatePreset(preset);
    if (preset !== 'CUSTOM') {
      setDateRange(getDateRangeForPreset(preset));
    }
  };

  const handleDateRangeChange = (range: DateRange) => {
    setDateRange(range);
    setDatePreset('CUSTOM');
  };

  const openCreateModal = () => {
    setFormError(null);
    if (activeTab === 'transactions') {
      setTransactionFormModal({ mode: 'create' });
    } else {
      setCategoryFormModal({ mode: 'create' });
    }
  };

  const openEditTransactionModal = (transaction: FinancialTransaction) => {
    setFormError(null);
    setTransactionFormModal({ mode: 'edit', transaction });
  };

  const openEditCategoryModal = (category: FinancialCategory) => {
    setFormError(null);
    setCategoryFormModal({ mode: 'edit', category });
  };

  const closeTransactionFormModal = () => {
    if (!formLoading) {
      setTransactionFormModal(null);
      setFormError(null);
    }
  };

  const closeCategoryFormModal = () => {
    if (!formLoading) {
      setCategoryFormModal(null);
      setFormError(null);
    }
  };

  const handleCreateTransaction = async (
    payload: CreateTransactionPayload | UpdateTransactionPayload,
  ) => {
    setFormLoading(true);
    setFormError(null);

    try {
      await createTransaction(payload as CreateTransactionPayload);
      setTransactionFormModal(null);
      setSuccessMessage('Operacja została dodana.');
      await Promise.all([refetchTransactions(), refetchSummary()]);
    } catch (err) {
      setFormError(getFinanceErrorMessage(err));
    } finally {
      setFormLoading(false);
    }
  };

  const handleUpdateTransaction = async (
    payload: CreateTransactionPayload | UpdateTransactionPayload,
  ) => {
    if (!transactionFormModal?.transaction) {
      return;
    }

    setFormLoading(true);
    setFormError(null);

    try {
      await updateTransaction(
        transactionFormModal.transaction.id,
        payload as UpdateTransactionPayload,
      );
      setTransactionFormModal(null);
      setSuccessMessage('Operacja została zaktualizowana.');
      await Promise.all([refetchTransactions(), refetchSummary()]);
    } catch (err) {
      setFormError(getFinanceErrorMessage(err));
    } finally {
      setFormLoading(false);
    }
  };

  const handleCancelTransaction = async () => {
    if (!cancelTarget) {
      return;
    }

    setActionLoading(true);

    try {
      await cancelTransaction(cancelTarget.id);
      setCancelTarget(null);
      setSuccessMessage('Operacja została anulowana.');
      await Promise.all([refetchTransactions(), refetchSummary()]);
    } catch (err) {
      setSuccessMessage(null);
      setFormError(getFinanceErrorMessage(err));
    } finally {
      setActionLoading(false);
    }
  };

  const handleCreateCategory = async (payload: CreateCategoryPayload | UpdateCategoryPayload) => {
    setFormLoading(true);
    setFormError(null);

    try {
      await createCategory(payload as CreateCategoryPayload);
      setCategoryFormModal(null);
      setSuccessMessage('Kategoria została dodana.');
      await refetchCategories();
    } catch (err) {
      setFormError(getFinanceErrorMessage(err));
    } finally {
      setFormLoading(false);
    }
  };

  const handleUpdateCategory = async (payload: CreateCategoryPayload | UpdateCategoryPayload) => {
    if (!categoryFormModal?.category) {
      return;
    }

    setFormLoading(true);
    setFormError(null);

    try {
      await updateCategory(categoryFormModal.category.id, payload as UpdateCategoryPayload);
      setCategoryFormModal(null);
      setSuccessMessage('Kategoria została zaktualizowana.');
      await refetchCategories();
    } catch (err) {
      setFormError(getFinanceErrorMessage(err));
    } finally {
      setFormLoading(false);
    }
  };

  const handleDeactivateCategory = async () => {
    if (!deactivateCategoryTarget) {
      return;
    }

    setActionLoading(true);

    try {
      await deactivateCategory(deactivateCategoryTarget.id);
      setDeactivateCategoryTarget(null);
      setSuccessMessage('Kategoria została dezaktywowana.');
      await refetchCategories();
    } catch (err) {
      setSuccessMessage(null);
      setFormError(getFinanceErrorMessage(err));
    } finally {
      setActionLoading(false);
    }
  };

  const renderTransactionsContent = () => {
    if (transactionsLoading) {
      return <LoadingState label="Ładowanie operacji…" />;
    }

    if (transactionsForbidden) {
      return (
        <ErrorState
          title="Brak dostępu"
          message={transactionsError ?? 'Nie masz uprawnień do tej operacji.'}
        />
      );
    }

    if (transactionsUnauthorized) {
      return (
        <ErrorState
          title="Sesja wygasła"
          message={transactionsError ?? 'Sesja wygasła. Zaloguj się ponownie.'}
        />
      );
    }

    if (transactionsError) {
      return <ErrorState message={transactionsError} onRetry={() => void refetchTransactions()} />;
    }

    if (transactions.length === 0) {
      return (
        <EmptyState
          title="BRAK OPERACJI"
          description="Nie znaleziono operacji spełniających kryteria."
          action={
            canCreate ? (
              <Button onClick={openCreateModal}>Dodaj pierwszą operację</Button>
            ) : undefined
          }
        />
      );
    }

    return (
      <>
        <div className="finance-page__desktop">
          <TransactionsTable
            transactions={transactions}
            canEdit={canEdit}
            canDelete={canDelete}
            onEdit={openEditTransactionModal}
            onCancel={setCancelTarget}
          />
        </div>
        <div className="finance-page__mobile">
          <TransactionsMobileList
            transactions={transactions}
            canEdit={canEdit}
            canDelete={canDelete}
            onEdit={openEditTransactionModal}
            onCancel={setCancelTarget}
          />
        </div>
      </>
    );
  };

  const renderCategoriesContent = () => {
    if (categoriesLoading) {
      return <LoadingState label="Ładowanie kategorii…" />;
    }

    if (categoriesForbidden) {
      return (
        <ErrorState
          title="Brak dostępu"
          message={categoriesError ?? 'Nie masz uprawnień do tej operacji.'}
        />
      );
    }

    if (categoriesUnauthorized) {
      return (
        <ErrorState
          title="Sesja wygasła"
          message={categoriesError ?? 'Sesja wygasła. Zaloguj się ponownie.'}
        />
      );
    }

    if (categoriesError) {
      return <ErrorState message={categoriesError} onRetry={() => void refetchCategories()} />;
    }

    if (categories.length === 0) {
      return (
        <EmptyState
          title="BRAK KATEGORII"
          description="Nie znaleziono kategorii spełniających kryteria."
          action={
            canCreate ? (
              <Button onClick={openCreateModal}>Dodaj pierwszą kategorię</Button>
            ) : undefined
          }
        />
      );
    }

    return (
      <>
        <div className="finance-page__desktop">
          <CategoriesTable
            categories={categories}
            canEdit={canEdit}
            canDelete={canDelete}
            onEdit={openEditCategoryModal}
            onDeactivate={setDeactivateCategoryTarget}
          />
        </div>
        <div className="finance-page__mobile">
          <CategoriesMobileList
            categories={categories}
            canEdit={canEdit}
            canDelete={canDelete}
            onEdit={openEditCategoryModal}
            onDeactivate={setDeactivateCategoryTarget}
          />
        </div>
      </>
    );
  };

  const createButtonLabel =
    activeTab === 'transactions' ? 'Dodaj operację' : 'Dodaj kategorię';

  return (
    <AppLayoutContainer>
      <PageHeader
        title="Finanse"
        description="Przychody, koszty i rozliczenia operacyjne."
        actions={
          canCreate ? (
            <Button onClick={openCreateModal}>
              <Plus size={16} aria-hidden="true" />
              {createButtonLabel}
            </Button>
          ) : undefined
        }
      />

      <DateRangePicker
        preset={datePreset}
        dateRange={dateRange}
        onPresetChange={handlePresetChange}
        onDateRangeChange={handleDateRangeChange}
      />

      {summaryForbidden ? (
        <ErrorState
          title="Brak dostępu"
          message={summaryError ?? 'Nie masz uprawnień do podsumowania finansowego.'}
        />
      ) : summaryError && !summary ? (
        <ErrorState message={summaryError} onRetry={() => void refetchSummary()} />
      ) : (
        <>
          <FinanceSummaryCards summary={summary} loading={summaryLoading} />
          <FinanceIncomeExpenseBar summary={summary} />
        </>
      )}

      <div className="finance-page__tabs" role="tablist" aria-label="Sekcje finansów">
        <button
          type="button"
          role="tab"
          aria-selected={activeTab === 'transactions'}
          className={`finance-page__tab${activeTab === 'transactions' ? ' finance-page__tab--active' : ''}`}
          onClick={() => {
            setActiveTab('transactions');
            setSuccessMessage(null);
          }}
        >
          Operacje
        </button>
        <button
          type="button"
          role="tab"
          aria-selected={activeTab === 'categories'}
          className={`finance-page__tab${activeTab === 'categories' ? ' finance-page__tab--active' : ''}`}
          onClick={() => {
            setActiveTab('categories');
            setSuccessMessage(null);
          }}
        >
          Kategorie
        </button>
      </div>

      {activeTab === 'transactions' ? (
        <div className="finance-page__toolbar">
          <div className="finance-page__search">
            <Input
              label="Szukaj"
              name="transactionSearch"
              placeholder="Kod, kontrahent, dokument…"
              value={transactionSearch}
              onChange={(event) => setTransactionSearch(event.target.value)}
            />
          </div>

          <label className="finance-page__filter-label">
            <span>Typ</span>
            <select
              className="finance-page__filter"
              value={typeFilter}
              onChange={(event) => setTypeFilter(event.target.value as TypeFilterValue)}
              aria-label="Filtr typu"
            >
              <option value="ALL">Wszystkie</option>
              {TRANSACTION_TYPES.map((type) => (
                <option key={type} value={type}>
                  {getTransactionTypeLabel(type)}
                </option>
              ))}
            </select>
          </label>

          <label className="finance-page__filter-label">
            <span>Kategoria</span>
            <select
              className="finance-page__filter"
              value={categoryFilter}
              onChange={(event) => setCategoryFilter(event.target.value as CategoryFilterValue)}
              aria-label="Filtr kategorii"
            >
              <option value="ALL">Wszystkie</option>
              {allCategories.map((category) => (
                <option key={category.id} value={category.id}>
                  {category.name}
                </option>
              ))}
            </select>
          </label>

          <label className="finance-page__filter-label">
            <span>Budynek</span>
            <select
              className="finance-page__filter"
              value={buildingFilter}
              onChange={(event) => setBuildingFilter(event.target.value as BuildingFilterValue)}
              aria-label="Filtr budynku"
            >
              <option value="ALL">Wszystkie</option>
              {buildings.map((building) => (
                <option key={building.id} value={building.id}>
                  {building.name} ({building.code})
                </option>
              ))}
            </select>
          </label>

          <label className="finance-page__filter-label">
            <span>Płatność</span>
            <select
              className="finance-page__filter"
              value={paymentFilter}
              onChange={(event) => setPaymentFilter(event.target.value as PaymentFilterValue)}
              aria-label="Filtr płatności"
            >
              <option value="ALL">Wszystkie</option>
              {PAYMENT_STATUSES.map((status) => (
                <option key={status} value={status}>
                  {getPaymentStatusLabel(status)}
                </option>
              ))}
            </select>
          </label>
        </div>
      ) : (
        <div className="finance-page__toolbar">
          <div className="finance-page__search">
            <Input
              label="Szukaj"
              name="categorySearch"
              placeholder="Kod, nazwa…"
              value={categorySearch}
              onChange={(event) => setCategorySearch(event.target.value)}
            />
          </div>

          <label className="finance-page__filter-label">
            <span>Typ</span>
            <select
              className="finance-page__filter"
              value={categoryTypeFilter}
              onChange={(event) => setCategoryTypeFilter(event.target.value as TypeFilterValue)}
              aria-label="Filtr typu kategorii"
            >
              <option value="ALL">Wszystkie</option>
              {TRANSACTION_TYPES.map((type) => (
                <option key={type} value={type}>
                  {getTransactionTypeLabel(type)}
                </option>
              ))}
            </select>
          </label>

          <label className="finance-page__filter-label">
            <span>Status</span>
            <select
              className="finance-page__filter"
              value={categoryActiveFilter}
              onChange={(event) =>
                setCategoryActiveFilter(event.target.value as ActiveFilterValue)
              }
              aria-label="Filtr statusu kategorii"
            >
              <option value="ALL">Wszystkie</option>
              <option value="ACTIVE">Aktywne</option>
              <option value="INACTIVE">Nieaktywne</option>
            </select>
          </label>
        </div>
      )}

      {successMessage ? (
        <p className="finance-page__feedback" role="status">
          {successMessage}
        </p>
      ) : null}

      {formError && !transactionFormModal && !categoryFormModal ? (
        <p className="finance-page__feedback finance-page__feedback--error" role="alert">
          {formError}
        </p>
      ) : null}

      {activeTab === 'transactions' ? renderTransactionsContent() : renderCategoriesContent()}

      <Modal
        isOpen={transactionFormModal !== null}
        title={transactionFormModal?.mode === 'edit' ? 'Edytuj operację' : 'Dodaj operację'}
        onClose={closeTransactionFormModal}
        size="large"
      >
        {transactionFormModal ? (
          <TransactionForm
            key={transactionFormModal.transaction?.id ?? 'create'}
            mode={transactionFormModal.mode}
            initialTransaction={transactionFormModal.transaction}
            categories={allCategories}
            submitLabel={
              transactionFormModal.mode === 'edit' ? 'Zapisz zmiany' : 'Dodaj operację'
            }
            loading={formLoading}
            serverError={formError}
            onSubmit={
              transactionFormModal.mode === 'edit'
                ? handleUpdateTransaction
                : handleCreateTransaction
            }
            onCancel={closeTransactionFormModal}
          />
        ) : null}
      </Modal>

      <Modal
        isOpen={categoryFormModal !== null}
        title={categoryFormModal?.mode === 'edit' ? 'Edytuj kategorię' : 'Dodaj kategorię'}
        onClose={closeCategoryFormModal}
      >
        {categoryFormModal ? (
          <CategoryForm
            key={categoryFormModal.category?.id ?? 'create'}
            mode={categoryFormModal.mode}
            initialCategory={categoryFormModal.category}
            submitLabel={categoryFormModal.mode === 'edit' ? 'Zapisz zmiany' : 'Dodaj kategorię'}
            loading={formLoading}
            serverError={formError}
            onSubmit={
              categoryFormModal.mode === 'edit' ? handleUpdateCategory : handleCreateCategory
            }
            onCancel={closeCategoryFormModal}
          />
        ) : null}
      </Modal>

      <CancelTransactionDialog
        transaction={cancelTarget}
        loading={actionLoading}
        onConfirm={() => void handleCancelTransaction()}
        onCancel={() => {
          if (!actionLoading) {
            setCancelTarget(null);
          }
        }}
      />

      <DeactivateCategoryDialog
        category={deactivateCategoryTarget}
        loading={actionLoading}
        onConfirm={() => void handleDeactivateCategory()}
        onCancel={() => {
          if (!actionLoading) {
            setDeactivateCategoryTarget(null);
          }
        }}
      />
    </AppLayoutContainer>
  );
}
