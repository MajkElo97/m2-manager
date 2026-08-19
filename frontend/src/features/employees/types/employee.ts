export type EmployeeRole = 'PRACOWNIK' | 'ADMIN';

export type EmploymentType = 'ZLECENIE';

export type RemunerationUnit = 'HOURLY';

export interface Employee {
  id: string;
  code: string;
  firstName: string;
  lastName: string | null;
  phone: string | null;
  email: string | null;
  googleEmail: string | null;
  position: string | null;
  role: EmployeeRole;
  employmentType: EmploymentType | null;
  employmentStartDate: string | null;
  remunerationAmount: number | null;
  remunerationUnit: RemunerationUnit | null;
  remunerationNet: boolean | null;
  calendarColor: string | null;
  notes: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateEmployeePayload {
  code: string;
  firstName: string;
  lastName?: string;
  phone?: string;
  email?: string;
  googleEmail?: string;
  position?: string;
  role: EmployeeRole;
  employmentType?: EmploymentType | null;
  employmentStartDate?: string | null;
  remunerationAmount?: number | null;
  remunerationUnit?: RemunerationUnit | null;
  remunerationNet?: boolean | null;
  calendarColor?: string;
  notes?: string;
}

export interface UpdateEmployeePayload extends CreateEmployeePayload {
  active: boolean;
}

export interface EmployeeListParams {
  search?: string;
  position?: string;
  role?: EmployeeRole | null;
  employmentType?: EmploymentType | null;
  active?: boolean | null;
}
