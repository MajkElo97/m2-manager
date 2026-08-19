import { ApiError } from '@/services/apiError';

export function getContactErrorMessage(error: unknown): string {
  if (error instanceof ApiError) {
    switch (error.status) {
      case 404:
        return 'Kontakt nie został znaleziony.';
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
