import type { AuthenticationResponse } from '@/features/auth/authTypes';
import { tokenStore } from '@/features/auth/tokenStore';
import { ApiError, parseApiError } from '@/services/apiError';
import { buildApiUrl, ensureCsrfCookie, getCsrfTokenFromCookie } from '@/services/csrf';

type HttpMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';

export interface RequestOptions {
  body?: unknown;
  headers?: Record<string, string>;
  skipAuth?: boolean;
  skipRefresh?: boolean;
}

type SessionExpiredListener = () => void;

let sessionExpiredListener: SessionExpiredListener | null = null;
let refreshInFlight: Promise<string> | null = null;

export function onSessionExpired(listener: SessionExpiredListener): () => void {
  sessionExpiredListener = listener;
  return () => {
    if (sessionExpiredListener === listener) {
      sessionExpiredListener = null;
    }
  };
}

function notifySessionExpired(): void {
  tokenStore.clear();
  sessionExpiredListener?.();
}

function buildHeaders(options: RequestOptions, method: HttpMethod): Headers {
  const headers = new Headers(options.headers);

  if (!headers.has('Accept')) {
    headers.set('Accept', 'application/json');
  }

  if (options.body !== undefined && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }

  if (!options.skipAuth) {
    const token = tokenStore.get();
    if (token) {
      headers.set('Authorization', `Bearer ${token}`);
    }
  }

  if (method !== 'GET') {
    const csrfToken = getCsrfTokenFromCookie();
    if (csrfToken) {
      headers.set('X-XSRF-TOKEN', csrfToken);
    }
  }

  return headers;
}

async function performFetch(
  method: HttpMethod,
  path: string,
  options: RequestOptions,
): Promise<Response> {
  const response = await fetch(buildApiUrl(path), {
    method,
    credentials: 'include',
    headers: buildHeaders(options, method),
    body: options.body !== undefined ? JSON.stringify(options.body) : undefined,
  });

  return response;
}

async function refreshAccessToken(): Promise<string> {
  if (!refreshInFlight) {
    refreshInFlight = (async () => {
      await ensureCsrfCookie();

      const response = await performFetch('POST', '/api/auth/refresh', {
        skipAuth: true,
        skipRefresh: true,
      });

      if (!response.ok) {
        throw await parseApiError(response);
      }

      const data = (await response.json()) as AuthenticationResponse;
      tokenStore.set(data.accessToken);
      return data.accessToken;
    })().finally(() => {
      refreshInFlight = null;
    });
  }

  return refreshInFlight;
}

async function request<T>(
  method: HttpMethod,
  path: string,
  options: RequestOptions = {},
  retried = false,
): Promise<T> {
  const response = await performFetch(method, path, options);

  if (response.status === 401 && !options.skipAuth && !options.skipRefresh && !retried) {
    try {
      await refreshAccessToken();
      return request<T>(method, path, options, true);
    } catch {
      notifySessionExpired();
      throw await parseApiError(response);
    }
  }

  if (response.status === 204) {
    return undefined as T;
  }

  if (!response.ok) {
    throw await parseApiError(response);
  }

  if (response.headers.get('content-type')?.includes('application/json')) {
    return (await response.json()) as T;
  }

  return undefined as T;
}

export const apiClient = {
  get<T>(path: string, options?: Omit<RequestOptions, 'body'>): Promise<T> {
    return request<T>('GET', path, options);
  },
  post<T>(path: string, body?: unknown, options?: Omit<RequestOptions, 'body'>): Promise<T> {
    return request<T>('POST', path, { ...options, body });
  },
  put<T>(path: string, body?: unknown, options?: Omit<RequestOptions, 'body'>): Promise<T> {
    return request<T>('PUT', path, { ...options, body });
  },
  patch<T>(path: string, body?: unknown, options?: Omit<RequestOptions, 'body'>): Promise<T> {
    return request<T>('PATCH', path, { ...options, body });
  },
  delete<T>(path: string, options?: Omit<RequestOptions, 'body'>): Promise<T> {
    return request<T>('DELETE', path, options);
  },
  refreshAccessToken,
  notifySessionExpired,
};

export { ApiError };
