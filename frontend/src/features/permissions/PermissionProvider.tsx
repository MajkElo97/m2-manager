import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import {
  hasAllPermissions as checkAllPermissions,
  hasAnyPermission as checkAnyPermission,
  hasModuleAdmin as checkModuleAdmin,
  hasPermission as checkPermission,
} from './permissionUtils';
import { defaultPermissionsAdapter, type PermissionsAdapter } from './permissionsAdapter';

interface PermissionContextValue {
  permissions: ReadonlySet<string>;
  isLoading: boolean;
  loadPermissions: () => Promise<void>;
  clearPermissions: () => void;
  hasPermission: (permissionCode: string) => boolean;
  hasAnyPermission: (...permissionCodes: string[]) => boolean;
  hasAllPermissions: (...permissionCodes: string[]) => boolean;
  hasModuleAdmin: (module: string) => boolean;
}

const PermissionContext = createContext<PermissionContextValue | null>(null);

interface PermissionProviderProps {
  children: ReactNode;
  adapter?: PermissionsAdapter;
}

export function PermissionProvider({
  children,
  adapter = defaultPermissionsAdapter,
}: PermissionProviderProps) {
  const [permissions, setPermissions] = useState<ReadonlySet<string>>(new Set());
  const [isLoading, setIsLoading] = useState(false);

  const loadPermissions = useCallback(async () => {
    setIsLoading(true);
    try {
      const codes = await adapter.loadPermissions();
      setPermissions(new Set(codes));
    } finally {
      setIsLoading(false);
    }
  }, [adapter]);

  const clearPermissions = useCallback(() => {
    setPermissions(new Set());
  }, []);

  const value = useMemo<PermissionContextValue>(
    () => ({
      permissions,
      isLoading,
      loadPermissions,
      clearPermissions,
      hasPermission: (permissionCode: string) => checkPermission(permissions, permissionCode),
      hasAnyPermission: (...permissionCodes: string[]) =>
        checkAnyPermission(permissions, permissionCodes),
      hasAllPermissions: (...permissionCodes: string[]) =>
        checkAllPermissions(permissions, permissionCodes),
      hasModuleAdmin: (module: string) => checkModuleAdmin(permissions, module),
    }),
    [permissions, isLoading, loadPermissions, clearPermissions],
  );

  return <PermissionContext.Provider value={value}>{children}</PermissionContext.Provider>;
}

export function usePermissions(): PermissionContextValue {
  const context = useContext(PermissionContext);
  if (!context) {
    throw new Error('usePermissions must be used within PermissionProvider');
  }
  return context;
}
