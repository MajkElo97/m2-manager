import { beforeEach, describe, expect, it, vi } from 'vitest';
import { tokenStore } from '@/features/auth/tokenStore';
import { createMockJwt } from '@/test/testUtils';

describe('apiClient refresh flow', () => {
  beforeEach(() => {
    tokenStore.clear();
    vi.restoreAllMocks();
  });

  it('retries request once after 401 refresh', async () => {
    const initialToken = createMockJwt({
      sub: 'user-1',
      organization_id: 'org-1',
      email: 'user@example.com',
      exp: Math.floor(Date.now() / 1000) + 60,
    });
    const refreshedToken = createMockJwt({
      sub: 'user-1',
      organization_id: 'org-1',
      email: 'user@example.com',
      exp: Math.floor(Date.now() / 1000) + 3600,
    });

    tokenStore.set(initialToken);

    let protectedCallCount = 0;
    let refreshCallCount = 0;

    vi.stubGlobal(
      'fetch',
      vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
        const url = String(input);

        if (url.includes('/actuator/health')) {
          return new Response(null, { status: 200 });
        }

        if (url.includes('/api/auth/refresh')) {
          refreshCallCount += 1;
          return Response.json({
            accessToken: refreshedToken,
            tokenType: 'Bearer',
            expiresIn: 900,
          });
        }

        if (url.includes('/api/protected')) {
          protectedCallCount += 1;
          let authHeader: string | null = null;
          if (init?.headers instanceof Headers) {
            authHeader = init.headers.get('Authorization');
          }

          if (protectedCallCount === 1) {
            expect(authHeader).toBe(`Bearer ${initialToken}`);
            return new Response(null, { status: 401 });
          }

          expect(authHeader).toBe(`Bearer ${refreshedToken}`);
          return Response.json({ ok: true });
        }

        return new Response(null, { status: 404 });
      }),
    );

    const { apiClient } = await import('@/services/apiClient');
    const result = await apiClient.get<{ ok: boolean }>('/api/protected');

    expect(result.ok).toBe(true);
    expect(refreshCallCount).toBe(1);
    expect(protectedCallCount).toBe(2);
    expect(tokenStore.get()).toBe(refreshedToken);

    vi.unstubAllGlobals();
  });
});
