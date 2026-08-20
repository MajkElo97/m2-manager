export type DateRangePreset =
  | 'CURRENT_MONTH'
  | 'PREVIOUS_MONTH'
  | 'CURRENT_QUARTER'
  | 'CURRENT_YEAR'
  | 'CUSTOM';

export interface DateRange {
  dateFrom: string;
  dateTo: string;
}

function toIsoDate(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function startOfMonth(date: Date): Date {
  return new Date(date.getFullYear(), date.getMonth(), 1);
}

function endOfMonth(date: Date): Date {
  return new Date(date.getFullYear(), date.getMonth() + 1, 0);
}

function startOfQuarter(date: Date): Date {
  const quarter = Math.floor(date.getMonth() / 3);
  return new Date(date.getFullYear(), quarter * 3, 1);
}

function endOfQuarter(date: Date): Date {
  const quarter = Math.floor(date.getMonth() / 3);
  return new Date(date.getFullYear(), quarter * 3 + 3, 0);
}

function startOfYear(date: Date): Date {
  return new Date(date.getFullYear(), 0, 1);
}

function endOfYear(date: Date): Date {
  return new Date(date.getFullYear(), 11, 31);
}

export function getPresetLabel(preset: DateRangePreset): string {
  switch (preset) {
    case 'CURRENT_MONTH':
      return 'Ten miesiąc';
    case 'PREVIOUS_MONTH':
      return 'Poprzedni miesiąc';
    case 'CURRENT_QUARTER':
      return 'Ten kwartał';
    case 'CURRENT_YEAR':
      return 'Ten rok';
    case 'CUSTOM':
      return 'Własny zakres';
  }
}

export const DATE_RANGE_PRESETS: DateRangePreset[] = [
  'CURRENT_MONTH',
  'PREVIOUS_MONTH',
  'CURRENT_QUARTER',
  'CURRENT_YEAR',
  'CUSTOM',
];

export function getDateRangeForPreset(
  preset: DateRangePreset,
  referenceDate: Date = new Date(),
): DateRange {
  switch (preset) {
    case 'CURRENT_MONTH':
      return {
        dateFrom: toIsoDate(startOfMonth(referenceDate)),
        dateTo: toIsoDate(endOfMonth(referenceDate)),
      };
    case 'PREVIOUS_MONTH': {
      const previous = new Date(referenceDate.getFullYear(), referenceDate.getMonth() - 1, 1);
      return {
        dateFrom: toIsoDate(startOfMonth(previous)),
        dateTo: toIsoDate(endOfMonth(previous)),
      };
    }
    case 'CURRENT_QUARTER':
      return {
        dateFrom: toIsoDate(startOfQuarter(referenceDate)),
        dateTo: toIsoDate(endOfQuarter(referenceDate)),
      };
    case 'CURRENT_YEAR':
      return {
        dateFrom: toIsoDate(startOfYear(referenceDate)),
        dateTo: toIsoDate(endOfYear(referenceDate)),
      };
    case 'CUSTOM':
      return getDateRangeForPreset('CURRENT_MONTH', referenceDate);
  }
}

export function getDefaultDateRange(): DateRange {
  return getDateRangeForPreset('CURRENT_MONTH');
}
