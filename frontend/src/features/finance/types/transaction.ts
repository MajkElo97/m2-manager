export type TransactionType = 'INCOME' | 'EXPENSE';

export type PaymentStatus = 'NOT_APPLICABLE' | 'TO_PAY' | 'PAID' | 'OVERDUE';

export type TransactionStatus = 'ACTIVE' | 'CANCELLED';

export interface FinancialTransaction {
  id: string;
  code: string;
  transactionDate: string;
  type: TransactionType;
  netAmount: number;
  vatRate: number | null;
  vatAmount: number;
  grossAmount: number;
  categoryId: string;
  categoryCode: string;
  categoryName: string;
  contractorName: string | null;
  contractorNip: string | null;
  buildingId: string | null;
  buildingCode: string | null;
  buildingName: string | null;
  employeeId: string | null;
  employeeCode: string | null;
  employeeName: string | null;
  vehicleId: string | null;
  vehicleCode: string | null;
  vehicleRegistrationNumber: string | null;
  equipmentId: string | null;
  equipmentCode: string | null;
  equipmentName: string | null;
  chemicalId: string | null;
  chemicalCode: string | null;
  chemicalName: string | null;
  description: string | null;
  documentNumber: string | null;
  dueDate: string | null;
  paymentDate: string | null;
  paymentStatus: PaymentStatus;
  status: TransactionStatus;
  notes: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface TransactionListParams {
  search?: string;
  type?: TransactionType | null;
  categoryId?: string | null;
  buildingId?: string | null;
  employeeId?: string | null;
  vehicleId?: string | null;
  paymentStatus?: PaymentStatus | null;
  status?: TransactionStatus | null;
  dateFrom?: string | null;
  dateTo?: string | null;
}

export interface CreateTransactionPayload {
  code: string;
  transactionDate: string;
  type: TransactionType;
  netAmount: number;
  vatRate: number | null;
  categoryId: string;
  contractorName: string | null;
  contractorNip: string | null;
  buildingId: string | null;
  employeeId: string | null;
  vehicleId: string | null;
  equipmentId: string | null;
  chemicalId: string | null;
  description: string | null;
  documentNumber: string | null;
  dueDate: string | null;
  paymentDate: string | null;
  paymentStatus: PaymentStatus;
  notes: string | null;
}

export type UpdateTransactionPayload = CreateTransactionPayload;
