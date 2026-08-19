import { useState } from 'react';
import { Plus } from 'lucide-react';
import { AppLayoutContainer } from '@/components/layout/AppLayout';
import { Button } from '@/components/ui/Button';
import { EmptyState } from '@/components/ui/EmptyState';
import { ErrorState } from '@/components/ui/ErrorState';
import { LoadingState } from '@/components/ui/LoadingState';
import { Modal } from '@/components/ui/Modal';
import { PageHeader } from '@/components/ui/PageHeader';
import { usePermissions } from '@/features/permissions/PermissionProvider';
import {
  createRole,
  deactivateRole,
  updateRole,
  updateRolePermissions,
} from '@/features/roles/api/rolesApi';
import { DeactivateRoleDialog } from '@/features/roles/components/DeactivateRoleDialog';
import { RoleForm, type RoleFormSubmitPayload } from '@/features/roles/components/RoleForm';
import { RolesMobileList } from '@/features/roles/components/RolesMobileList';
import { RolesTable } from '@/features/roles/components/RolesTable';
import { useRoles } from '@/features/roles/hooks/useRoles';
import { getRoleErrorMessage } from '@/features/roles/rolesMessages';
import type { CreateRolePayload, Role } from '@/features/roles/types/role';
import './RolesPage.css';

interface FormModalState {
  mode: 'create' | 'edit';
  role?: Role;
}

export function RolesPage() {
  const { hasPermission } = usePermissions();
  const canCreate = hasPermission('ROLES_CREATE');
  const canEdit = hasPermission('ROLES_EDIT');
  const canDelete = hasPermission('ROLES_DELETE');

  const [formModal, setFormModal] = useState<FormModalState | null>(null);
  const [deactivateTarget, setDeactivateTarget] = useState<Role | null>(null);
  const [formLoading, setFormLoading] = useState(false);
  const [deactivateLoading, setDeactivateLoading] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const { roles, isLoading, error, forbidden, unauthorized, refetch } = useRoles();

  const openCreateModal = () => {
    setFormError(null);
    setFormModal({ mode: 'create' });
  };

  const openEditModal = (role: Role) => {
    setFormError(null);
    setFormModal({ mode: 'edit', role });
  };

  const closeFormModal = () => {
    if (!formLoading) {
      setFormModal(null);
      setFormError(null);
    }
  };

  const handleCreate = async ({ role, permissionCodes }: RoleFormSubmitPayload) => {
    setFormLoading(true);
    setFormError(null);

    try {
      const createdRole = await createRole(role as CreateRolePayload);
      await updateRolePermissions(createdRole.id, { permissionCodes });
      setFormModal(null);
      setSuccessMessage('Rola została dodana.');
      await refetch();
    } catch (err) {
      setFormError(getRoleErrorMessage(err));
    } finally {
      setFormLoading(false);
    }
  };

  const handleUpdate = async ({ role, permissionCodes }: RoleFormSubmitPayload) => {
    if (!formModal?.role) {
      return;
    }

    setFormLoading(true);
    setFormError(null);

    try {
      await updateRole(formModal.role.id, role);
      if (!formModal.role.systemRole) {
        await updateRolePermissions(formModal.role.id, { permissionCodes });
      }
      setFormModal(null);
      setSuccessMessage('Rola została zaktualizowana.');
      await refetch();
    } catch (err) {
      setFormError(getRoleErrorMessage(err));
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
      await deactivateRole(deactivateTarget.id);
      setDeactivateTarget(null);
      setSuccessMessage('Rola została dezaktywowana.');
      await refetch();
    } catch (err) {
      setSuccessMessage(null);
      setFormError(getRoleErrorMessage(err));
    } finally {
      setDeactivateLoading(false);
    }
  };

  const renderContent = () => {
    if (isLoading) {
      return <LoadingState label="Ładowanie ról…" />;
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

    if (roles.length === 0) {
      return (
        <EmptyState
          title="BRAK RÓL"
          description="Nie znaleziono ról w organizacji."
          action={
            canCreate ? (
              <Button onClick={openCreateModal}>Dodaj pierwszą rolę</Button>
            ) : undefined
          }
        />
      );
    }

    return (
      <>
        <div className="roles-page__desktop">
          <RolesTable
            roles={roles}
            canEdit={canEdit}
            canDelete={canDelete}
            onEdit={openEditModal}
            onDeactivate={setDeactivateTarget}
          />
        </div>
        <div className="roles-page__mobile">
          <RolesMobileList
            roles={roles}
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
        title="Role"
        description="Zarządzanie rolami i uprawnieniami w organizacji."
        actions={
          canCreate ? (
            <Button onClick={openCreateModal}>
              <Plus size={16} aria-hidden="true" />
              Dodaj rolę
            </Button>
          ) : undefined
        }
      />

      {successMessage ? (
        <p className="roles-page__feedback" role="status">
          {successMessage}
        </p>
      ) : null}

      {renderContent()}

      <Modal
        isOpen={formModal !== null}
        title={formModal?.mode === 'edit' ? 'Edytuj rolę' : 'Dodaj rolę'}
        onClose={closeFormModal}
        size="xlarge"
      >
        {formModal ? (
          <RoleForm
            key={formModal.role?.id ?? 'create'}
            mode={formModal.mode}
            initialRole={formModal.role}
            submitLabel={formModal.mode === 'edit' ? 'Zapisz zmiany' : 'Dodaj rolę'}
            loading={formLoading}
            serverError={formError}
            onSubmit={formModal.mode === 'edit' ? handleUpdate : handleCreate}
            onCancel={closeFormModal}
          />
        ) : null}
      </Modal>

      <DeactivateRoleDialog
        role={deactivateTarget}
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
