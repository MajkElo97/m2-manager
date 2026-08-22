export interface AuthUser {
  userId: string;
  organizationId: string;
  email: string;
}

export interface OrganizationSummary {
  id: string;
  name: string;
  slug: string;
}

export interface AuthContextUser {
  id: string;
  name: string;
  email: string;
}

export interface AuthContextResponse {
  user: AuthContextUser;
  activeOrganization: OrganizationSummary | null;
  availableOrganizations: OrganizationSummary[];
  canSwitchOrganizations: boolean;
  mustChangePassword: boolean;
  superAdmin: boolean;
}

export interface AuthenticationResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  mustChangePassword: boolean;
}

export interface LoginRequest {
  organizationSlug: string;
  email: string;
  password: string;
}

export interface ChangePasswordPayload {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

export type AuthStatus = 'initializing' | 'authenticated' | 'unauthenticated';

export interface AuthState {
  status: AuthStatus;
  user: AuthUser | null;
  accessToken: string | null;
  context: AuthContextResponse | null;
  organizationContextKey: string | null;
}
