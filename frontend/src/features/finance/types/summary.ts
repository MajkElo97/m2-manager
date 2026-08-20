export interface FinanceSummary {
  incomeNet: number;
  incomeGross: number;
  expenseNet: number;
  expenseGross: number;
  operatingResultNet: number;
  receivables: number;
  liabilities: number;
  overdueReceivables: number;
  overdueLiabilities: number;
}

export interface FinanceSummaryParams {
  dateFrom: string;
  dateTo: string;
}
