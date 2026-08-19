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
import { useRoles } from '@/features/roles/hooks/useRoles';
import { usePermissions } from '@/features/permissions/PermissionProvider';
import {
  createUser,
  deactivateUser,
  updateUser,
} from '@/features/users/api/usersApi';
import { DeactivateUserDialog } from '@/features/users/components/DeactivateUserDialog';
import { UserForm } from '@/features/users/components/UserForm';
import { UsersMobileList } from '@/features/users/components/UsersMobileList';
import { UsersTable } from '@/features/users/components/UsersTable';
import { useUsers } from '@/features/users/hooks/useUsers';
import { getUserErrorMessage } from '@/features/users/usersMessages';
import type {
  CreateUserPayload,
  UpdateUserPayload,
  User,
} from '@/features/users/types/user';
import './UsersPage.css';

type ActiveFilterValue = 'ALL' | 'ACTIVE' | 'INACTIVE';

interface FormModalState {
  mode: 'create' | 'edit';
  user?: User;
}

export function UsersPage() {
  const { hasPermission } = usePermissions();
  const canCreate = hasPermission('USERS_CREATE');
  const canEdit = hasPermission('USERS_EDIT');
  const canDelete = hasPermission('USERS_DELETE');

  const [search, setSearch] = useState('');
  const [roleFilter, setRoleFilter] = useState<string>('ALL');
  const [activeFilter, setActiveFilter] = useState<ActiveFilterValue>('ACTIVE');
  const [formModal, setFormModal] = useState<FormModalState | null>(null);
  const [deactivateTarget, setDeactivateTarget] = useState<User | null>(null);
  const [formLoading, setFormLoading] = useState(false);
  const [deactivateLoading, setDeactivateLoading] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const { roles } = useRoles();

  const listParams = useMemo(
    () => ({
      search,
      active:
        activeFilter === 'ALL'
          ? null
          : activeFilter === 'ACTIVE',
      roleId: roleFilter === 'ALL' ? null : roleFilter,
    }),
    [search, activeFilter, roleFilter],
  );

  const { users, isLoading, error, forbidden, unauthorized, refetch } = useUsers(listParams);

  const roleFilterOptions = useMemo(
    () => roles.filter((role) => role.active),
    [roles],
  );

  const openCreateModal = () => {
    setFormError(null);
    setFormModal({ mode: 'create' });
  };

  const openEditModal = (user: User) => {
    setFormError(null);
    setFormModal({ mode: 'edit', user });
  };

  const closeFormModal = () => {
    if (!formLoading) {
      setFormModal(null);
      setFormError(null);
    }
  };

  const handleCreate = async (payload: CreateUserPayload | UpdateUserPayload) => {
    setFormLoading(true);
    setFormError(null);

    try {
      await createUser(payload as CreateUserPayload);
      setFormModal(null);
      setSuccessMessage('Użytkownik został dodany.');
      await refetch();
    } catch (err) {
      setFormError(getUserErrorMessage(err));
    } finally {
      setFormLoading(false);
    }
  };

  const handleUpdate = async (payload: CreateUserPayload | UpdateUserPayload) => {
    if (!formModal?.user) {
      return;
    }

    setFormLoading(true);
    setFormError(null);

    try {
      await updateUser(formModal.user.id, payload as UpdateUserPayload);
      setFormModal(null);
      setSuccessMessage('Użytkownik został zaktualizowany.');
      await refetch();
    } catch (err) {
      setFormError(getUserErrorMessage(err));
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
      await deactivateUser(deactivateTarget.id);
      setDeactivateTarget(null);
      setSuccessMessage('Użytkownik został dezaktywowany.');
      await refetch();
    } catch (err) {
      setSuccessMessage(null);
      setFormError(getUserErrorMessage(err));
    } finally {
      setDeactivateLoading(false);
    }
  };

  const renderContent = () => {
    if (isLoading) {
      return <LoadingState label="Ładowanie użytkowników…" />;
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

    if (users.length === 0) {
      return (
        <EmptyState
          title="BRAK UŻYTKOWNIKÓW"
          description="Nie znaleziono użytkowników spełniających kryteria."
          action={
            canCreate ? (
              <Button onClick={openCreateModal}>Dodaj pierwszego użytkownika</Button>
            ) : undefined
          }
        />
      );
    }

    return (
      <>
        <div className="users-page__desktop">
          <UsersTable
            users={users}
            canEdit={canEdit}
            canDelete={canDelete}
            onEdit={openEditModal}
            onDeactivate={setDeactivateTarget}
          />
        </div>
        <div className="users-page__mobile">
          <UsersMobileList
            users={users}
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
      <PageHeader
        title="Użytkownicy"
        description="Zarządzanie kontami użytkowników w organizacji."
        actions={
          canCreate ? (
            <Button onClick={openCreateModal}>
              <Plus size={16} aria-hidden="true" />
              Dodaj użytkownika
            </Button>
          ) : undefined
        }
      />

      <div className="users-page__toolbar">
        <div className="users-page__search">
          <Input
            label="Szukaj"
            name="search"
            placeholder="Imię, nazwisko, e-mail…"
            value={search}
            onChange={(event) => setSearch(event.target.value)}
          />
        </div>

        <label className="users-page__filter-label">
          <span>Rola</span>
          <select
            className="users-page__filter"
            value={roleFilter}
            onChange={(event) => setRoleFilter(event.target.value)}
            aria-label="Filtr roli"
          >
            <option value="ALL">Wszystkie</option>
            {roleFilterOptions.map((role) => (
              <option key={role.id} value={role.id}>
                {role.name}
              </option>
            ))}
          </select>
        </label>

        <label className="users-page__filter-label">
          <span>Status</span>
          <select
            className="users-page__filter"
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
        <p className="users-page__feedback" role="status">
          {successMessage}
        </p>
      ) : null}

      {renderContent()}

      <Modal
        isOpen={formModal !== null}
        title={formModal?.mode === 'edit' ? 'Edytuj użytkownika' : 'Dodaj użytkownika'}
        onClose={closeFormModal}
      >
        {formModal ? (
          <UserForm
            key={formModal.user?.id ?? 'create'}
            mode={formModal.mode}
            initialUser={formModal.user}
            submitLabel={formModal.mode === 'edit' ? 'Zapisz zmiany' : 'Dodaj użytkownika'}
            loading={formLoading}
            serverError={formError}
            onSubmit={formModal.mode === 'edit' ? handleUpdate : handleCreate}
            onCancel={closeFormModal}
          />
        ) : null}
      </Modal>

      <DeactivateUserDialog
        user={deactivateTarget}
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
