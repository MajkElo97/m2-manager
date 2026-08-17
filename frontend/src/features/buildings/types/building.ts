export type BuildingStatus = 'ACTIVE' | 'INACTIVE';

export interface Building {
  id: string;
  code: string;
  name: string;
  address: string;
  city: string;
  nip: string | null;
  phone: string | null;
  email: string | null;
  managerCode: string | null;
  supervisorCode: string | null;
  employeeCode: string | null;
  contractSignedAt: string | null;
  serviceStartDate: string | null;
  noticePeriodMonths: number;
  status: BuildingStatus;
  notes: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateBuildingPayload {
  code: string;
  name: string;
  address: string;
  city: string;
  nip?: string;
  phone?: string;
  email?: string;
  managerCode?: string;
  supervisorCode?: string;
  employeeCode?: string;
  contractSignedAt?: string | null;
  serviceStartDate?: string | null;
  noticePeriodMonths: number;
  notes?: string;
}

export interface UpdateBuildingPayload extends CreateBuildingPayload {
  status: BuildingStatus;
}

export interface BuildingListParams {
  status?: BuildingStatus | null;
  search?: string;
}
