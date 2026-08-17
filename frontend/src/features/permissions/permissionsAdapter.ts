import { ENABLE_MOCK_PERMISSIONS } from '@/config/env';
import { apiClient } from '@/services/apiClient';
import { ApiError } from '@/services/apiError';
import { buildAllPermissionCodes } from './permissionUtils';

export interface PermissionsAdapter {
  loadPermissions(): Promise<string[]>;
}

/**
 * Loads effective permissions from the backend.
 * Planned endpoint: GET /api/auth/permissions (not yet implemented on backend).
 */
export class HttpPermissionsAdapter implements PermissionsAdapter {
  async loadPermissions(): Promise<string[]> {
    try {
      const response = await apiClient.get<{ permissions: string[] }>('/api/auth/permissions', {
        skipRefresh: false,
      });
      return response.permissions ?? [];
    } catch (error) {
      if (error instanceof ApiError && (error.status === 404 || error.status === 401)) {
        return [];
      }
      throw error;
    }
  }
}

/** Temporary adapter for local UI development until backend exposes permissions. */
export class MockPermissionsAdapter implements PermissionsAdapter {
  async loadPermissions(): Promise<string[]> {
    return buildAllPermissionCodes();
  }
}

export function createPermissionsAdapter(): PermissionsAdapter {
  if (ENABLE_MOCK_PERMISSIONS) {
    return new MockPermissionsAdapter();
  }
  return new HttpPermissionsAdapter();
}

export const defaultPermissionsAdapter = createPermissionsAdapter();
