import type { AuthenticationResponse, AuthContextResponse, ChangePasswordPayload, LoginRequest } from '@/features/auth/authTypes';
import { tokenStore } from '@/features/auth/tokenStore';
import { apiClient, awaitPendingRefresh } from '@/services/apiClient';
import { ensureCsrfCookie } from '@/services/csrf';
import { ApiError, parseApiError } from '@/services/apiError';

export const authService = {
  async login(credentials: LoginRequest): Promise<AuthenticationResponse> {
    await ensureCsrfCookie();

    const response = await fetch(`${import.meta.env.VITE_API_BASE_URL ?? ''}/api/auth/login`, {
      method: 'POST',
      credentials: 'include',
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(credentials),
    });

    if (!response.ok) {
      throw await parseApiError(response);
    }

    const data = (await response.json()) as AuthenticationResponse;
    tokenStore.set(data.accessToken);
    return data;
  },

  async refresh(): Promise<AuthenticationResponse> {
    await ensureCsrfCookie();
    const accessToken = await apiClient.refreshAccessToken();
    return {
      accessToken,
      tokenType: 'Bearer',
      expiresIn: 900,
      mustChangePassword: false,
    };
  },

  async logout(): Promise<void> {
    try {
      await ensureCsrfCookie();
      await apiClient.post<void>('/api/auth/logout');
    } catch (error) {
      if (!(error instanceof ApiError) || (error.status !== 401 && error.status !== 403)) {
        throw error;
      }
    } finally {
      tokenStore.clear();
    }
  },

  clearSession(): void {
    tokenStore.clear();
  },

  async getContext(accessToken?: string): Promise<AuthContextResponse> {
    return apiClient.get<AuthContextResponse>('/api/auth/context', { accessToken });
  },

  async switchOrganization(organizationId: string): Promise<AuthenticationResponse> {
    await ensureCsrfCookie();
    await awaitPendingRefresh();
    const response = await apiClient.post<AuthenticationResponse>('/api/auth/context/organization', {
      organizationId,
    });
    tokenStore.set(response.accessToken);
    return response;
  },

  async changePassword(payload: ChangePasswordPayload): Promise<void> {
    await apiClient.post<void>('/api/auth/change-password', payload);
  },
};
