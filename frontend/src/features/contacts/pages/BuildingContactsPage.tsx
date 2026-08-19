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
import {
  createContact,
  deactivateContact,
  updateContact,
} from '@/features/contacts/api/contactsApi';
import { ContactForm } from '@/features/contacts/components/ContactForm';
import { ContactsMobileList } from '@/features/contacts/components/ContactsMobileList';
import { ContactsTable } from '@/features/contacts/components/ContactsTable';
import { DeactivateContactDialog } from '@/features/contacts/components/DeactivateContactDialog';
import { getContactErrorMessage } from '@/features/contacts/contactsMessages';
import { useContacts } from '@/features/contacts/hooks/useContacts';
import type {
  Contact,
  CreateContactPayload,
  UpdateContactPayload,
} from '@/features/contacts/types/contact';
import { useBuildingContext } from '@/features/staircases/hooks/useBuildingContext';
import { usePermissions } from '@/features/permissions/PermissionProvider';
import './ContactsPage.css';

interface FormModalState {
  mode: 'create' | 'edit';
  contact?: Contact;
}

export function BuildingContactsPage() {
  const { buildingId = '' } = useParams<{ buildingId: string }>();
  const { hasPermission } = usePermissions();
  const canCreate = hasPermission('CONTACTS_CREATE');
  const canEdit = hasPermission('CONTACTS_EDIT');
  const canDelete = hasPermission('CONTACTS_DELETE');

  const {
    building,
    isLoading: buildingLoading,
    error: buildingError,
    notFound: buildingNotFound,
    refetch: refetchBuilding,
  } = useBuildingContext(buildingId);

  const {
    contacts,
    isLoading: contactsLoading,
    error: contactsError,
    forbidden,
    refetch: refetchContacts,
  } = useContacts({ buildingId });

  const [formModal, setFormModal] = useState<FormModalState | null>(null);
  const [deactivateTarget, setDeactivateTarget] = useState<Contact | null>(null);
  const [formLoading, setFormLoading] = useState(false);
  const [deactivateLoading, setDeactivateLoading] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const isLoading = buildingLoading || contactsLoading;

  const openCreateModal = () => {
    setFormError(null);
    setFormModal({ mode: 'create' });
  };

  const openEditModal = (contact: Contact) => {
    setFormError(null);
    setFormModal({ mode: 'edit', contact });
  };

  const closeFormModal = () => {
    if (!formLoading) {
      setFormModal(null);
      setFormError(null);
    }
  };

  const handleCreate = async (payload: CreateContactPayload | UpdateContactPayload) => {
    setFormLoading(true);
    setFormError(null);

    try {
      await createContact({
        ...(payload as CreateContactPayload),
        buildingId,
      });
      setFormModal(null);
      setSuccessMessage('Kontakt został dodany.');
      await refetchContacts();
    } catch (err) {
      setFormError(getContactErrorMessage(err));
    } finally {
      setFormLoading(false);
    }
  };

  const handleUpdate = async (payload: CreateContactPayload | UpdateContactPayload) => {
    if (!formModal?.contact) {
      return;
    }

    setFormLoading(true);
    setFormError(null);

    try {
      await updateContact(formModal.contact.id, payload as UpdateContactPayload);
      setFormModal(null);
      setSuccessMessage('Kontakt został zaktualizowany.');
      await refetchContacts();
    } catch (err) {
      setFormError(getContactErrorMessage(err));
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
      await deactivateContact(deactivateTarget.id);
      setDeactivateTarget(null);
      setSuccessMessage('Kontakt został dezaktywowany.');
      await refetchContacts();
    } catch (err) {
      setSuccessMessage(null);
      setFormError(getContactErrorMessage(err));
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
      return <LoadingState label="Ładowanie kontaktów…" />;
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
          message={contactsError ?? 'Nie masz uprawnień do tej operacji.'}
        />
      );
    }

    if (contactsError && contacts.length === 0) {
      return (
        <ErrorState
          message={contactsError ?? 'Nie udało się wczytać danych.'}
          onRetry={() => void refetchContacts()}
        />
      );
    }

    if (contacts.length === 0) {
      return (
        <EmptyState
          title="BRAK KONTAKTÓW"
          description="Nie znaleziono kontaktów w tym budynku."
          action={
            canCreate ? (
              <Button onClick={openCreateModal}>Dodaj pierwszy kontakt</Button>
            ) : undefined
          }
        />
      );
    }

    return (
      <>
        <div className="contacts-page__desktop">
          <ContactsTable
            contacts={contacts}
            canEdit={canEdit}
            canDelete={canDelete}
            onEdit={openEditModal}
            onDeactivate={setDeactivateTarget}
          />
        </div>
        <div className="contacts-page__mobile">
          <ContactsMobileList
            contacts={contacts}
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
      <div className="contacts-page__back">
        <Link to="/buildings" className="contacts-page__back-link">
          <ArrowLeft size={16} aria-hidden="true" />
          Wróć do budynku
        </Link>
      </div>

      <PageHeader
        title="Kontakty"
        description={pageDescription}
        actions={
          canCreate ? (
            <Button onClick={openCreateModal}>
              <Plus size={16} aria-hidden="true" />
              Dodaj kontakt
            </Button>
          ) : undefined
        }
      />

      {successMessage ? (
        <p className="contacts-page__feedback" role="status">
          {successMessage}
        </p>
      ) : null}

      {formError && !formModal ? (
        <p className="contacts-page__feedback contacts-page__feedback--error" role="alert">
          {formError}
        </p>
      ) : null}

      {renderContent()}

      <Modal
        isOpen={formModal !== null}
        title={formModal?.mode === 'edit' ? 'Edytuj kontakt' : 'Dodaj kontakt'}
        onClose={closeFormModal}
      >
        {formModal && building ? (
          <ContactForm
            key={formModal.contact?.id ?? 'create'}
            mode={formModal.mode}
            initialContact={formModal.contact}
            buildings={[building]}
            fixedBuildingId={buildingId}
            submitLabel={formModal.mode === 'edit' ? 'Zapisz zmiany' : 'Dodaj kontakt'}
            loading={formLoading}
            serverError={formError}
            onSubmit={formModal.mode === 'edit' ? handleUpdate : handleCreate}
            onCancel={closeFormModal}
          />
        ) : null}
      </Modal>

      <DeactivateContactDialog
        contact={deactivateTarget}
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
