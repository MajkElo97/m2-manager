export interface UserRoleSummary {
  id: string;
  name: string;
  systemRole: boolean;
}

export interface User {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  active: boolean;
  roles: UserRoleSummary[];
  employeeId: string | null;
  employeeCode: string | null;
  employeeDisplayName: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateUserPayload {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  roleIds: string[];
  employeeId?: string | null;
}

export interface UpdateUserPayload {
  firstName: string;
  lastName: string;
  email: string;
  password?: string;
  roleIds: string[];
  employeeId?: string | null;
  active: boolean;
}

export interface UserListParams {
  search?: string;
  active?: boolean | null;
  roleId?: string | null;
}
