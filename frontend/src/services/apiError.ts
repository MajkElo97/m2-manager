export interface ApiErrorBody {
  timestamp?: string;
  status: number;
  error?: string;
  message?: string;
  path?: string;
  fieldErrors?: Array<{ field: string; message: string }>;
}

export class ApiError extends Error {
  readonly status: number;
  readonly body: ApiErrorBody | null;

  constructor(status: number, message: string, body: ApiErrorBody | null = null) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.body = body;
  }
}

export async function parseApiError(response: Response): Promise<ApiError> {
  let body: ApiErrorBody | null = null;

  try {
    body = (await response.json()) as ApiErrorBody;
  } catch {
    body = null;
  }

  const message = body?.message ?? getDefaultMessage(response.status);
  return new ApiError(response.status, message, body);
}

function getDefaultMessage(status: number): string {
  switch (status) {
    case 400:
      return 'Nieprawidłowe dane żądania.';
    case 401:
      return 'Sesja wygasła. Zaloguj się ponownie.';
    case 403:
      return 'Brak uprawnień do wykonania tej operacji.';
    case 404:
      return 'Nie znaleziono zasobu.';
    case 409:
      return 'Operacja nie może zostać wykonana.';
    case 500:
      return 'Wystąpił błąd serwera. Spróbuj ponownie później.';
    default:
      return 'Wystąpił nieoczekiwany błąd.';
  }
}

export function getFriendlyAuthError(error: unknown): string {
  if (error instanceof ApiError) {
    if (error.status === 401 || error.status === 400) {
      return 'Nieprawidłowy login lub hasło.';
    }
    return error.message;
  }

  return 'Nie udało się zalogować. Spróbuj ponownie.';
}
