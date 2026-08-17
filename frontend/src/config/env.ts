export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '';

export const ENABLE_MOCK_PERMISSIONS =
  import.meta.env.VITE_ENABLE_MOCK_PERMISSIONS === 'true';

export const THEME_STORAGE_KEY = 'm2-manager-theme';

export type ThemePreference = 'light' | 'dark' | 'system';
