import type { AuthUser } from '@/features/auth/authTypes';

interface JwtPayload {
  sub?: string;
  organization_id?: string;
  email?: string;
  exp?: number;
}

export function decodeAccessToken(token: string): AuthUser | null {
  try {
    const parts = token.split('.');
    if (parts.length !== 3) {
      return null;
    }

    const payloadJson = atob(parts[1].replace(/-/g, '+').replace(/_/g, '/'));
    const payload = JSON.parse(payloadJson) as JwtPayload;

    if (!payload.sub || !payload.organization_id || !payload.email) {
      return null;
    }

    return {
      userId: payload.sub,
      organizationId: payload.organization_id,
      email: payload.email,
    };
  } catch {
    return null;
  }
}

export function isTokenExpired(token: string): boolean {
  try {
    const parts = token.split('.');
    if (parts.length !== 3) {
      return true;
    }

    const payloadJson = atob(parts[1].replace(/-/g, '+').replace(/_/g, '/'));
    const payload = JSON.parse(payloadJson) as JwtPayload;

    if (!payload.exp) {
      return false;
    }

    return payload.exp * 1000 <= Date.now();
  } catch {
    return true;
  }
}
