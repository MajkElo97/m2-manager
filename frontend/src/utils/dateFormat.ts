/**
 * Formats an ISO date string (yyyy-MM-dd) for display as DD/MM/YYYY.
 */
export function formatDateDisplay(iso: string | null): string {
  if (!iso) {
    return '—';
  }

  const parts = iso.split('-');
  if (parts.length !== 3) {
    return iso;
  }

  const [year, month, day] = parts;
  return `${day}/${month}/${year}`;
}

/**
 * Parses an ISO date string (yyyy-MM-dd) for use in native date inputs.
 */
export function parseIsoDate(iso: string | null | undefined): string {
  if (!iso) {
    return '';
  }

  const parts = iso.split('-');
  if (parts.length !== 3) {
    return '';
  }

  const [year, month, day] = parts;
  if (!year || !month || !day) {
    return '';
  }

  return `${year}-${month.padStart(2, '0')}-${day.padStart(2, '0')}`;
}

/**
 * Converts a native date input value (yyyy-MM-dd) to ISO date for the API.
 */
export function toIsoDate(inputValue: string): string | null {
  if (!inputValue) {
    return null;
  }

  const parts = inputValue.split('-');
  if (parts.length !== 3) {
    return null;
  }

  const [year, month, day] = parts;
  return `${year}-${month.padStart(2, '0')}-${day.padStart(2, '0')}`;
}
