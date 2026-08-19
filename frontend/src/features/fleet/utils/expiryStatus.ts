import { formatDateDisplay } from '@/utils/dateFormat';

export type ExpiryVariant = 'neutral' | 'success' | 'warning' | 'danger';

export type ExpiryKind = 'insurance' | 'inspection';

export interface ExpiryStatus {
  variant: ExpiryVariant;
  label: string;
  tooltip: string;
}

const WARNING_THRESHOLD_DAYS = 30;

function daysUntil(isoDate: string): number {
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  const target = new Date(`${isoDate}T00:00:00`);
  const diffMs = target.getTime() - today.getTime();
  return Math.ceil(diffMs / (1000 * 60 * 60 * 24));
}

function formatDaysCount(days: number): string {
  if (days === 1) {
    return '1 dzień';
  }

  return `${days} dni`;
}

function getKindPrefix(kind: ExpiryKind): string {
  return kind === 'insurance' ? 'OC' : 'Przegląd';
}

export function getExpiryStatus(isoDate: string | null, kind: ExpiryKind): ExpiryStatus | null {
  if (!isoDate) {
    return null;
  }

  const days = daysUntil(isoDate);
  const prefix = getKindPrefix(kind);
  const label = formatDateDisplay(isoDate);

  if (days < 0) {
    return {
      variant: 'danger',
      label,
      tooltip: `${prefix} wygasł`,
    };
  }

  if (days <= WARNING_THRESHOLD_DAYS) {
    return {
      variant: 'warning',
      label,
      tooltip: `${prefix} wygasa za ${formatDaysCount(days)}`,
    };
  }

  return {
    variant: 'success',
    label,
    tooltip: `${prefix} ważne do ${label}`,
  };
}
