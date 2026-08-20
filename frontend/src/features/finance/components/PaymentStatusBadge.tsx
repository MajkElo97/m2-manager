import { Badge } from '@/components/ui/Badge';
import type { PaymentStatus } from '@/features/finance/types/transaction';
import {
  getPaymentStatusLabel,
  paymentStatusVariant,
} from '@/features/finance/utils/financeLabels';

interface PaymentStatusBadgeProps {
  status: PaymentStatus;
}

export function PaymentStatusBadge({ status }: PaymentStatusBadgeProps) {
  return <Badge variant={paymentStatusVariant(status)}>{getPaymentStatusLabel(status)}</Badge>;
}
