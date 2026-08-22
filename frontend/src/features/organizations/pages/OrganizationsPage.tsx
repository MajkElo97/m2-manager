import { useMemo, useState } from 'react';
import { Plus } from 'lucide-react';
import { Navigate } from 'react-router-dom';
import { AppLayoutContainer } from '@/components/layout/AppLayout';
import { Button } from '@/components/ui/Button';
import { EmptyState } from '@/components/ui/EmptyState';
import { ErrorState } from '@/components/ui/ErrorState';
import { Input } from '@/components/ui/Input';
import { LoadingState } from '@/components/ui/LoadingState';
import { Modal } from '@/components/ui/Modal';
import { PageHeader } from '@/components/ui/PageHeader';
import { useAuth } from '@/features/auth/AuthProvider';
import {
  createOrganization,
  deactivateOrganization,
  resetAdminPassword,
  updateOrganization,
} from '@/features/organizations/api/organizationsApi';
import { CredentialsDialog } from '@/features/organizations/components/CredentialsDialog';
import { DeactivateOrganizationDialog } from '@/features/organizations/components/DeactivateOrganizationDialog';
import { OrganizationForm } from '@/features/organizations/components/OrganizationForm';
import { OrganizationsMobileList } from '@/features/organizations/components/OrganizationsMobileList';
import { OrganizationsTable } from '@/features/organizations/components/OrganizationsTable';
import { useOrganizations } from '@/features/organizations/hooks/useOrganizations';
import type {
  CreateOrganizationPayload,
  CreateOrganizationResult,
  OrganizationListItem,
  ResetAdminPasswordResult,
  UpdateOrganizationPayload,
} from '@/features/organizations/types/organization';
import { ApiError } from '@/services/apiError';
import './OrganizationsPage.css';

type ActiveFilterValue = 'ALL' | 'ACTIVE' | 'INACTIVE';

interface FormModalState {
  mode: 'create' | 'edit';
  organization?: OrganizationListItem;
}

interface CredentialsState {
  title: string;
  organizationName: string;
  login: string;
  temporaryPassword: string;
}

export function OrganizationsPage() {
  const { context } = useAuth();
  const [search, setSearch] = useState('');
  const [activeFilter, setActiveFilter] = useState<ActiveFilterValue>('ALL');
  const [formModal, setFormModal] = useState<FormModalState | null>(null);
  const [deactivateTarget, setDeactivateTarget] = useState<OrganizationListItem | null>(null);
  const [credentials, setCredentials] = useState<CredentialsState | null>(null);
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

  const { organizations, isLoading, error, forbidden, unauthorized, refetch } = useOrganizations(listParams);

  const activeBusinessContext =
    context?.activeOrganization && context.activeOrganization.slug !== 'admin'
      ? context.activeOrganization
      : null;

  if (!context?.superAdmin) {
    return <Navigate to="/dashboard" replace />;
  }

  const openCreateModal = () => {
    setFormError(null);
    setFormModal({ mode: 'create' });
  };

  const openEditModal = (organization: OrganizationListItem) => {
    setFormError(null);
    setFormModal({ mode: 'edit', organization });
  };

  const closeFormModal = () => {
    if (!formLoading) {
      setFormModal(null);
      setFormError(null);
    }
  };

  const showCredentials = (title: string, organizationName: string, login: string, temporaryPassword: string) => {
    setCredentials({ title, organizationName, login, temporaryPassword });
  };

  const handleCreate = async (payload: CreateOrganizationPayload | UpdateOrganizationPayload) => {
    setFormLoading(true);
    setFormError(null);

    try {
      const result: CreateOrganizationResult = await createOrganization(payload as CreateOrganizationPayload);
      setFormModal(null);
      setSuccessMessage('Organizacja została utworzona.');
      showCredentials(
        'Organizacja została utworzona',
        result.name,
        result.adminEmail,
        result.temporaryPassword,
      );
      await refetch();
    } catch (err) {
      setFormError(err instanceof ApiError ? err.message : 'Nie udało się utworzyć organizacji.');
    } finally {
      setFormLoading(false);
    }
  };

  const handleUpdate = async (payload: CreateOrganizationPayload | UpdateOrganizationPayload) => {
    if (!formModal?.organization) {
      return;
    }

    setFormLoading(true);
    setFormError(null);

    try {
      await updateOrganization(formModal.organization.id, payload as UpdateOrganizationPayload);
      setFormModal(null);
      setSuccessMessage('Organizacja została zaktualizowana.');
      await refetch();
    } catch (err) {
      setFormError(err instanceof ApiError ? err.message : 'Nie udało się zaktualizować organizacji.');
    } finally {
      setFormLoading(false);
    }
  };

  const handleResetPassword = async (organization: OrganizationListItem) => {
    try {
      const result: ResetAdminPasswordResult = await resetAdminPassword(organization.id);
      showCredentials(
        'Nowe hasło tymczasowe',
        organization.name,
        result.adminEmail,
        result.temporaryPassword,
      );
    } catch (err) {
      setFormError(err instanceof ApiError ? err.message : 'Nie udało się zresetować hasła.');
    }
  };

  const handleDeactivate = async () => {
    if (!deactivateTarget) {
      return;
    }

    setDeactivateLoading(true);
    try {
      await deactivateOrganization(deactivateTarget.id);
      setDeactivateTarget(null);
      setSuccessMessage('Organizacja została dezaktywowana.');
      await refetch();
    } catch (err) {
      setFormError(err instanceof ApiError ? err.message : 'Nie udało się dezaktywować organizacji.');
    } finally {
      setDeactivateLoading(false);
    }
  };

  const renderContent = () => {
    if (isLoading) {
      return <LoadingState label="Ładowanie organizacji…" />;
    }

    if (unauthorized) {
      return <ErrorState title="Wymagane logowanie" message="Zaloguj się ponownie." />;
    }

    if (forbidden) {
      return <ErrorState title="Brak dostępu" message="Moduł Organizacje jest dostępny wyłącznie dla SUPER_ADMIN." />;
    }

    if (error) {
      return <ErrorState title="Błąd" message={error} />;
    }

    if (organizations.length === 0) {
      return <EmptyState title="Brak organizacji" description="Dodaj pierwszą organizację biznesową." />;
    }

    return (
      <>
        <OrganizationsTable
          organizations={organizations}
          onEdit={openEditModal}
          onResetPassword={(organization) => void handleResetPassword(organization)}
          onDeactivate={setDeactivateTarget}
        />
        <OrganizationsMobileList
          organizations={organizations}
          onEdit={openEditModal}
          onResetPassword={(organization) => void handleResetPassword(organization)}
          onDeactivate={setDeactivateTarget}
        />
      </>
    );
  };

  return (
    <AppLayoutContainer>
      <PageHeader
        title="Organizacje"
        description="Zarządzanie organizacjami biznesowymi systemu."
        actions={
          <Button type="button" onClick={openCreateModal}>
            <Plus size={16} aria-hidden="true" />
            Dodaj organizację
          </Button>
        }
      />

      {successMessage ? (
        <p role="status" className="page-success-message">
          {successMessage}
        </p>
      ) : null}

      {activeBusinessContext ? (
        <p className="organizations-page__context-bar" role="status">
          Aktywny kontekst: <strong>{activeBusinessContext.name}</strong>
        </p>
      ) : null}

      {formError ? (
        <p role="alert" className="page-error-message">
          {formError}
        </p>
      ) : null}

      <div className="organizations-page__toolbar">
        <Input
          label="Szukaj"
          value={search}
          onChange={(event) => setSearch(event.target.value)}
          placeholder="Nazwa lub slug"
        />
        <div className="organizations-page__filters">
          <label htmlFor="organization-active-filter">Status</label>
          <select
            id="organization-active-filter"
            value={activeFilter}
            onChange={(event) => setActiveFilter(event.target.value as ActiveFilterValue)}
          >
            <option value="ALL">Wszystkie</option>
            <option value="ACTIVE">Aktywne</option>
            <option value="INACTIVE">Nieaktywne</option>
          </select>
        </div>
      </div>

      <div className="organizations-page__content">{renderContent()}</div>

      <Modal
        isOpen={formModal != null}
        title={formModal?.mode === 'create' ? 'Dodaj organizację' : 'Edytuj organizację'}
        onClose={closeFormModal}
        size="large"
      >
        {formError ? <p role="alert">{formError}</p> : null}
        {formModal ? (
          <OrganizationForm
            mode={formModal.mode}
            initialValues={
              formModal.mode === 'edit' && formModal.organization
                ? {
                    name: formModal.organization.name,
                    slug: formModal.organization.slug,
                  }
                : undefined
            }
            loading={formLoading}
            onSubmit={formModal.mode === 'create' ? handleCreate : handleUpdate}
            onCancel={closeFormModal}
          />
        ) : null}
      </Modal>

      <DeactivateOrganizationDialog
        organization={deactivateTarget}
        loading={deactivateLoading}
        onConfirm={handleDeactivate}
        onClose={() => {
          if (!deactivateLoading) {
            setDeactivateTarget(null);
          }
        }}
      />

      <CredentialsDialog
        isOpen={credentials != null}
        title={credentials?.title ?? ''}
        organizationName={credentials?.organizationName ?? ''}
        login={credentials?.login ?? ''}
        temporaryPassword={credentials?.temporaryPassword ?? ''}
        onClose={() => setCredentials(null)}
      />
    </AppLayoutContainer>
  );
}
