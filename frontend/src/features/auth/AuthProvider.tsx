import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
  type ReactNode,
} from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from './authService';
import type { AuthState, AuthUser, LoginRequest } from './authTypes';
import { tokenStore } from './tokenStore';
import { decodeAccessToken } from '@/utils/jwt';
import { onSessionExpired } from '@/services/apiClient';
import { ensureCsrfCookie } from '@/services/csrf';
import { usePermissions } from '@/features/permissions/PermissionProvider';

interface AuthContextValue extends AuthState {
  login: (credentials: LoginRequest) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const navigate = useNavigate();
  const navigateRef = useRef(navigate);
  navigateRef.current = navigate;

  const { loadPermissions, clearPermissions } = usePermissions();
  const [state, setState] = useState<AuthState>({
    status: 'initializing',
    user: null,
    accessToken: null,
  });

  const establishSession = useCallback(
    async (accessToken: string) => {
      const user = decodeAccessToken(accessToken);
      if (!user) {
        throw new Error('Invalid access token');
      }

      tokenStore.set(accessToken);
      setState({
        status: 'authenticated',
        user,
        accessToken,
      });
      await loadPermissions();
    },
    [loadPermissions],
  );

  const clearSession = useCallback(() => {
    authService.clearSession();
    clearPermissions();
    setState({
      status: 'unauthenticated',
      user: null,
      accessToken: null,
    });
  }, [clearPermissions]);

  const establishSessionRef = useRef(establishSession);
  establishSessionRef.current = establishSession;

  const clearSessionRef = useRef(clearSession);
  clearSessionRef.current = clearSession;

  useEffect(() => {
    let active = true;

    async function initialize() {
      try {
        await ensureCsrfAndRefresh();
        const token = tokenStore.get();
        if (!token) {
          throw new Error('No token after refresh');
        }
        if (active) {
          await establishSessionRef.current(token);
        }
      } catch {
        if (active) {
          clearSessionRef.current();
        }
      }
    }

    void initialize();

    return () => {
      active = false;
    };
  }, []);

  useEffect(() => {
    return onSessionExpired(() => {
      clearSessionRef.current();
      navigateRef.current('/login', { replace: true });
    });
  }, []);

  const login = useCallback(
    async (credentials: LoginRequest) => {
      const response = await authService.login(credentials);
      await establishSession(response.accessToken);
    },
    [establishSession],
  );

  const logout = useCallback(async () => {
    try {
      await authService.logout();
    } finally {
      clearSession();
      navigate('/login', { replace: true });
    }
  }, [clearSession, navigate]);

  const value = useMemo<AuthContextValue>(
    () => ({
      ...state,
      login,
      logout,
    }),
    [state, login, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

async function ensureCsrfAndRefresh(): Promise<void> {
  await ensureCsrfCookie();
  await authService.refresh();
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
}

export type { AuthUser };
