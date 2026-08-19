import { ApiError } from '@/services/apiError';

export function getScopeErrorMessage(error: unknown): string {
  if (error instanceof ApiError) {
    switch (error.status) {
      case 409:
        return 'Zakres o tym kodzie już istnieje w tym budynku.';
      case 404:
        return 'Zakres nie został znaleziony.';
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
