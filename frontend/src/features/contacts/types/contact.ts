export interface Contact {
  id: string;
  buildingId: string;
  buildingCode: string;
  buildingName: string;
  firstName: string | null;
  lastName: string | null;
  functionTitle: string | null;
  phone: string | null;
  email: string | null;
  notes: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateContactPayload {
  buildingId: string;
  firstName?: string;
  lastName?: string;
  functionTitle?: string;
  phone?: string;
  email?: string;
  notes?: string;
}

export interface UpdateContactPayload extends CreateContactPayload {
  active: boolean;
}

export interface ContactListParams {
  buildingId?: string | null;
}
