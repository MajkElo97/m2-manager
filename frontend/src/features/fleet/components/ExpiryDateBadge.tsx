import { Badge } from '@/components/ui/Badge';
import type { ExpiryKind } from '@/features/fleet/utils/expiryStatus';
import { getExpiryStatus } from '@/features/fleet/utils/expiryStatus';

interface ExpiryDateBadgeProps {
  date: string | null;
  kind: ExpiryKind;
}

export function ExpiryDateBadge({ date, kind }: ExpiryDateBadgeProps) {
  const status = getExpiryStatus(date, kind);

  if (!status) {
    return <span>—</span>;
  }

  return (
    <span title={status.tooltip}>
      <Badge variant={status.variant}>{status.label}</Badge>
    </span>
  );
}
