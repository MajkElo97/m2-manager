import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
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
import { filterContacts } from '@/features/contacts/utils/filterContacts';
import { usePermissions } from '@/features/permissions/PermissionProvider';
import './ContactsPage.css';

type BuildingFilterValue = 'ALL' | string;
type ActiveFilterValue = 'ALL' | 'ACTIVE' | 'INACTIVE';

interface FormModalState {
  mode: 'create' | 'edit';
  contact?: Contact;
}

export function ContactsPage() {
  const navigate = useNavigate();
  const { hasPermission } = usePermissions();
  const canCreate = hasPermission('CONTACTS_CREATE');
  const canEdit = hasPermission('CONTACTS_EDIT');
  const canDelete = hasPermission('CONTACTS_DELETE');

  const [search, setSearch] = useState('');
  const [buildingFilter, setBuildingFilter] = useState<BuildingFilterValue>('ALL');
  const [functionFilter, setFunctionFilter] = useState('');
  const [activeFilter, setActiveFilter] = useState<ActiveFilterValue>('ACTIVE');
  const [formModal, setFormModal] = useState<FormModalState | null>(null);
  const [deactivateTarget, setDeactivateTarget] = useState<Contact | null>(null);
  const [formLoading, setFormLoading] = useState(false);
  const [deactivateLoading, setDeactivateLoading] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const {
    contacts,
    isLoading: contactsLoading,
    error: contactsError,
    forbidden,
    refetch: refetchContacts,
  } = useContacts();

  const {
    buildings,
    isLoading: buildingsLoading,
    error: buildingsError,
    refetch: refetchBuildings,
  } = useBuildings({ status: 'ACTIVE' });

  const filteredContacts = useMemo(
    () =>
      filterContacts(contacts, {
        search,
        buildingId: buildingFilter,
        functionTitle: functionFilter,
        active: activeFilter,
      }),
    [contacts, search, buildingFilter, functionFilter, activeFilter],
  );

  const isLoading = contactsLoading || buildingsLoading;

  const openCreateModal = () => {
    setFormError(null);
    setFormModal({ mode: 'create' });
  };

  const openEditModal = (contact: Contact) => {
    setFormError(null);
    setFormModal({ mode: 'edit', contact });
  };

  const openBuildingContacts = (buildingId: string) => {
    void navigate(`/buildings/${buildingId}/contacts`);
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
      await createContact(payload as CreateContactPayload);
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

  const handleRetry = () => {
    void refetchContacts();
    void refetchBuildings();
  };

  const renderContent = () => {
    if (isLoading) {
      return <LoadingState label="Ładowanie kontaktów…" />;
    }

    if (forbidden) {
      return (
        <ErrorState
          title="Brak dostępu"
          message={contactsError ?? 'Nie masz uprawnień do tej operacji.'}
        />
      );
    }

    if ((contactsError || buildingsError) && contacts.length === 0) {
      return (
        <ErrorState
          message={contactsError ?? buildingsError ?? 'Nie udało się wczytać danych.'}
          onRetry={handleRetry}
        />
      );
    }

    if (filteredContacts.length === 0) {
      return (
        <EmptyState
          title="BRAK KONTAKTÓW"
          description="Nie znaleziono kontaktów spełniających kryteria."
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
            contacts={filteredContacts}
            showBuilding
            canEdit={canEdit}
            canDelete={canDelete}
            onEdit={openEditModal}
            onDeactivate={setDeactivateTarget}
            onBuildingClick={openBuildingContacts}
          />
        </div>
        <div className="contacts-page__mobile">
          <ContactsMobileList
            contacts={filteredContacts}
            showBuilding
            canEdit={canEdit}
            canDelete={canDelete}
            onEdit={openEditModal}
            onDeactivate={setDeactivateTarget}
            onBuildingClick={openBuildingContacts}
          />
        </div>
      </>
    );
  };

  return (
    <AppLayoutContainer>
      <PageHeader
        title="Kontakty"
        description="Zarządzanie kontaktami budynków w organizacji."
        actions={
          canCreate ? (
            <Button onClick={openCreateModal}>
              <Plus size={16} aria-hidden="true" />
              Dodaj kontakt
            </Button>
          ) : undefined
        }
      />

      <div className="contacts-page__toolbar">
        <div className="contacts-page__search">
          <Input
            label="Szukaj"
            name="search"
            placeholder="Imię, nazwisko, budynek, funkcja…"
            value={search}
            onChange={(event) => setSearch(event.target.value)}
          />
        </div>

        <label className="contacts-page__filter-label">
          <span>Budynek</span>
          <select
            className="contacts-page__filter"
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

        <div className="contacts-page__search">
          <Input
            label="Funkcja"
            name="function"
            placeholder="Filtruj po funkcji…"
            value={functionFilter}
            onChange={(event) => setFunctionFilter(event.target.value)}
          />
        </div>

        <label className="contacts-page__filter-label">
          <span>Status</span>
          <select
            className="contacts-page__filter"
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
        <p className="contacts-page__feedback" role="status">
          {successMessage}
        </p>
      ) : null}

      {renderContent()}

      <Modal
        isOpen={formModal !== null}
        title={formModal?.mode === 'edit' ? 'Edytuj kontakt' : 'Dodaj kontakt'}
        onClose={closeFormModal}
      >
        {formModal ? (
          <ContactForm
            key={formModal.contact?.id ?? 'create'}
            mode={formModal.mode}
            initialContact={formModal.contact}
            buildings={buildings}
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
