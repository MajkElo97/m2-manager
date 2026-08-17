export interface AuthUser {
  userId: string;
  organizationId: string;
  email: string;
}

export interface LoginRequest {
  organizationSlug: string;
  email: string;
  password: string;
}

export interface AuthenticationResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
}

export type AuthStatus = 'initializing' | 'authenticated' | 'unauthenticated';

export interface AuthState {
  status: AuthStatus;
  user: AuthUser | null;
  accessToken: string | null;
}
