import { ApiError } from '@/services/apiError';

const DUPLICATE_BUILDING_CODE_MESSAGE = 'Building code already exists in organization';

export function getBuildingErrorMessage(error: unknown): string {
  if (error instanceof ApiError) {
    switch (error.status) {
      case 409:
        if (error.message === DUPLICATE_BUILDING_CODE_MESSAGE) {
          return 'Budynku o tym kodzie już istnieje.';
        }
        return error.message;
      case 404:
        return 'Budynek nie został znaleziony.';
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
