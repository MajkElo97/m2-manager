import type { AuthenticationResponse, LoginRequest } from '@/features/auth/authTypes';
import { tokenStore } from '@/features/auth/tokenStore';
import { apiClient } from '@/services/apiClient';
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
};
