import { ApiError } from '@/services/apiError';

export function getRoleErrorMessage(error: unknown): string {
  if (error instanceof ApiError) {
    switch (error.status) {
      case 409:
        return 'Rola o tej nazwie już istnieje.';
      case 404:
        return 'Rola nie została znaleziona.';
      case 403:
        return 'Nie masz uprawnień do tej operacji.';
      case 500:
        return 'Nie udało się wykonać operacji. Spróbuj ponownie.';
      default:
        return error.message;
    }
  }

  if (error instanceof Error) {
    return error.message;
  }

  return 'Nie udało się wykonać operacji. Spróbuj ponownie.';
}

export const SYSTEM_ROLE_READONLY_HINT =
  'Rola systemowa jest tylko do odczytu — nie można edytować uprawnień ani dezaktywować.';
