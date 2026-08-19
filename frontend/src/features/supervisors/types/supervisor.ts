export interface Supervisor {
  id: string;
  managerId: string;
  managerCode: string;
  managerName: string;
  code: string;
  firstName: string;
  lastName: string;
  phone: string | null;
  email: string | null;
  notes: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateSupervisorPayload {
  managerId: string;
  code: string;
  firstName: string;
  lastName: string;
  phone?: string;
  email?: string;
  notes?: string;
}

export interface UpdateSupervisorPayload extends CreateSupervisorPayload {
  active: boolean;
}

export interface SupervisorListParams {
  search?: string;
  managerId?: string | null;
  active?: boolean | null;
}
