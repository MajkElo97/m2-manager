export interface Manager {
  id: string;
  code: string;
  name: string;
  phone: string | null;
  email: string | null;
  address: string | null;
  notes: string | null;
  active: boolean;
  supervisorCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateManagerPayload {
  code: string;
  name: string;
  phone?: string;
  email?: string;
  address?: string;
  notes?: string;
}

export type UpdateManagerPayload = CreateManagerPayload;

export interface ManagerListParams {
  search?: string;
  active?: boolean | null;
}
