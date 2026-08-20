import { useMemo, useState } from 'react';
import { ArrowLeft, Plus } from 'lucide-react';
import { Link, useParams } from 'react-router-dom';
import { AppLayoutContainer } from '@/components/layout/AppLayout';
import { Button } from '@/components/ui/Button';
import { EmptyState } from '@/components/ui/EmptyState';
import { ErrorState } from '@/components/ui/ErrorState';
import { Input } from '@/components/ui/Input';
import { LoadingState } from '@/components/ui/LoadingState';
import { Modal } from '@/components/ui/Modal';
import { PageHeader } from '@/components/ui/PageHeader';
import {
  cancelTransaction,
  createTransaction,
  updateTransaction,
} from '@/features/finance/api/financeApi';
import { CancelTransactionDialog } from '@/features/finance/components/CancelTransactionDialog';
import { DateRangePicker } from '@/features/finance/components/DateRangePicker';
import { FinanceIncomeExpenseBar } from '@/features/finance/components/FinanceIncomeExpenseBar';
import { FinanceSummaryCards } from '@/features/finance/components/FinanceSummaryCards';
import { TransactionForm } from '@/features/finance/components/TransactionForm';
import { TransactionsMobileList } from '@/features/finance/components/TransactionsMobileList';
import { TransactionsTable } from '@/features/finance/components/TransactionsTable';
import { getFinanceErrorMessage } from '@/features/finance/financeMessages';
import { useCategories } from '@/features/finance/hooks/useCategories';
import { useTransactions } from '@/features/finance/hooks/useTransactions';
import type {
  CreateTransactionPayload,
  FinancialTransaction,
  PaymentStatus,
  TransactionType,
  UpdateTransactionPayload,
} from '@/features/finance/types/transaction';
import { computeSummaryFromTransactions } from '@/features/finance/utils/financeCalculations';
import {
  getDateRangeForPreset,
  getDefaultDateRange,
  type DateRange,
  type DateRangePreset,
} from '@/features/finance/utils/dateRangePresets';
import {
  getPaymentStatusLabel,
  getTransactionTypeLabel,
} from '@/features/finance/utils/financeLabels';
import { usePermissions } from '@/features/permissions/PermissionProvider';
import { useBuildingContext } from '@/features/staircases/hooks/useBuildingContext';
import './FinancePage.css';

type TypeFilterValue = TransactionType | 'ALL';
type PaymentFilterValue = PaymentStatus | 'ALL';
type CategoryFilterValue = string | 'ALL';

interface FormModalState {
  mode: 'create' | 'edit';
  transaction?: FinancialTransaction;
}

const TRANSACTION_TYPES: TransactionType[] = ['INCOME', 'EXPENSE'];
const PAYMENT_STATUSES: PaymentStatus[] = ['NOT_APPLICABLE', 'TO_PAY', 'PAID', 'OVERDUE'];

export function BuildingFinancePage() {
  const { buildingId = '' } = useParams<{ buildingId: string }>();
  const { hasPermission } = usePermissions();
  const canCreate = hasPermission('FINANCES_CREATE');
  const canEdit = hasPermission('FINANCES_EDIT');
  const canDelete = hasPermission('FINANCES_DELETE');

  const [datePreset, setDatePreset] = useState<DateRangePreset>('CURRENT_MONTH');
  const [dateRange, setDateRange] = useState<DateRange>(getDefaultDateRange);
  const [transactionSearch, setTransactionSearch] = useState('');
  const [typeFilter, setTypeFilter] = useState<TypeFilterValue>('ALL');
  const [categoryFilter, setCategoryFilter] = useState<CategoryFilterValue>('ALL');
  const [paymentFilter, setPaymentFilter] = useState<PaymentFilterValue>('ALL');

  const [formModal, setFormModal] = useState<FormModalState | null>(null);
  const [cancelTarget, setCancelTarget] = useState<FinancialTransaction | null>(null);
  const [formLoading, setFormLoading] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const {
    building,
    isLoading: buildingLoading,
    error: buildingError,
    notFound: buildingNotFound,
    refetch: refetchBuilding,
  } = useBuildingContext(buildingId);

  const transactionListParams = useMemo(
    () => ({
      search: transactionSearch,
      type: typeFilter === 'ALL' ? null : typeFilter,
      categoryId: categoryFilter === 'ALL' ? null : categoryFilter,
      buildingId,
      paymentStatus: paymentFilter === 'ALL' ? null : paymentFilter,
      status: 'ACTIVE' as const,
      dateFrom: dateRange.dateFrom,
      dateTo: dateRange.dateTo,
    }),
    [
      transactionSearch,
      typeFilter,
      categoryFilter,
      buildingId,
      paymentFilter,
      dateRange.dateFrom,
      dateRange.dateTo,
    ],
  );

  const {
    transactions,
    isLoading: transactionsLoading,
    error: transactionsError,
    forbidden,
    refetch: refetchTransactions,
  } = useTransactions(transactionListParams);

  const { categories: allCategories } = useCategories({ active: true });

  const summary = useMemo(() => computeSummaryFromTransactions(transactions), [transactions]);

  const isLoading = buildingLoading || transactionsLoading;

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
    setFormModal({ mode: 'create' });
  };

  const openEditModal = (transaction: FinancialTransaction) => {
    setFormError(null);
    setFormModal({ mode: 'edit', transaction });
  };

  const closeFormModal = () => {
    if (!formLoading) {
      setFormModal(null);
      setFormError(null);
    }
  };

  const handleCreate = async (payload: CreateTransactionPayload | UpdateTransactionPayload) => {
    setFormLoading(true);
    setFormError(null);

    try {
      await createTransaction({ ...(payload as CreateTransactionPayload), buildingId });
      setFormModal(null);
      setSuccessMessage('Operacja została dodana.');
      await refetchTransactions();
    } catch (err) {
      setFormError(getFinanceErrorMessage(err));
    } finally {
      setFormLoading(false);
    }
  };

  const handleUpdate = async (payload: CreateTransactionPayload | UpdateTransactionPayload) => {
    if (!formModal?.transaction) {
      return;
    }

    setFormLoading(true);
    setFormError(null);

    try {
      await updateTransaction(formModal.transaction.id, {
        ...(payload as UpdateTransactionPayload),
        buildingId,
      });
      setFormModal(null);
      setSuccessMessage('Operacja została zaktualizowana.');
      await refetchTransactions();
    } catch (err) {
      setFormError(getFinanceErrorMessage(err));
    } finally {
      setFormLoading(false);
    }
  };

  const handleCancel = async () => {
    if (!cancelTarget) {
      return;
    }

    setActionLoading(true);

    try {
      await cancelTransaction(cancelTarget.id);
      setCancelTarget(null);
      setSuccessMessage('Operacja została anulowana.');
      await refetchTransactions();
    } catch (err) {
      setSuccessMessage(null);
      setFormError(getFinanceErrorMessage(err));
    } finally {
      setActionLoading(false);
    }
  };

  const buildingContextLine = building ? `${building.name} · ${building.code}` : null;
  const buildingAddressLine = building ? `${building.address}, ${building.city}` : null;
  const pageDescription =
    buildingContextLine && buildingAddressLine
      ? `${buildingContextLine} — ${buildingAddressLine}`
      : buildingError ?? undefined;

  const renderContent = () => {
    if (isLoading) {
      return <LoadingState label="Ładowanie operacji finansowych…" />;
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
          message={transactionsError ?? 'Nie masz uprawnień do tej operacji.'}
        />
      );
    }

    if (transactionsError && transactions.length === 0) {
      return (
        <ErrorState
          message={transactionsError ?? 'Nie udało się wczytać danych.'}
          onRetry={() => void refetchTransactions()}
        />
      );
    }

    if (transactions.length === 0) {
      return (
        <EmptyState
          title="BRAK OPERACJI"
          description="Nie znaleziono operacji finansowych dla tego budynku."
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
            onEdit={openEditModal}
            onCancel={setCancelTarget}
          />
        </div>
        <div className="finance-page__mobile">
          <TransactionsMobileList
            transactions={transactions}
            canEdit={canEdit}
            canDelete={canDelete}
            onEdit={openEditModal}
            onCancel={setCancelTarget}
          />
        </div>
      </>
    );
  };

  return (
    <AppLayoutContainer>
      <div className="finance-page__back">
        <Link to="/buildings" className="finance-page__back-link">
          <ArrowLeft size={16} aria-hidden="true" />
          Wróć do budynku
        </Link>
      </div>

      <PageHeader
        title="Finanse"
        description={pageDescription}
        actions={
          canCreate ? (
            <Button onClick={openCreateModal}>
              <Plus size={16} aria-hidden="true" />
              Dodaj operację
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

      <FinanceSummaryCards summary={summary} loading={transactionsLoading} />
      <FinanceIncomeExpenseBar summary={summary} />

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

      {successMessage ? (
        <p className="finance-page__feedback" role="status">
          {successMessage}
        </p>
      ) : null}

      {formError && !formModal ? (
        <p className="finance-page__feedback finance-page__feedback--error" role="alert">
          {formError}
        </p>
      ) : null}

      {renderContent()}

      <Modal
        isOpen={formModal !== null}
        title={formModal?.mode === 'edit' ? 'Edytuj operację' : 'Dodaj operację'}
        onClose={closeFormModal}
        size="large"
      >
        {formModal && building ? (
          <TransactionForm
            key={formModal.transaction?.id ?? 'create'}
            mode={formModal.mode}
            initialTransaction={formModal.transaction}
            categories={allCategories}
            buildings={[building]}
            fixedBuildingId={buildingId}
            submitLabel={formModal.mode === 'edit' ? 'Zapisz zmiany' : 'Dodaj operację'}
            loading={formLoading}
            serverError={formError}
            onSubmit={formModal.mode === 'edit' ? handleUpdate : handleCreate}
            onCancel={closeFormModal}
          />
        ) : null}
      </Modal>

      <CancelTransactionDialog
        transaction={cancelTarget}
        loading={actionLoading}
        onConfirm={() => void handleCancel()}
        onCancel={() => {
          if (!actionLoading) {
            setCancelTarget(null);
          }
        }}
      />
    </AppLayoutContainer>
  );
}
