/**
 * Formats a monetary value for display in Polish locale (e.g. "1 500,00 zł").
 */
export function formatCurrency(value: number | null | undefined): string {
  if (value == null || Number.isNaN(value)) {
    return '—';
  }

  const [integerPart, decimalPart = '00'] = Math.abs(value).toFixed(2).split('.');
  const sign = value < 0 ? '-' : '';
  const groupedInteger = integerPart.replace(/\B(?=(\d{3})+(?!\d))/g, ' ');

  return `${sign}${groupedInteger},${decimalPart}\u00A0zł`;
}
