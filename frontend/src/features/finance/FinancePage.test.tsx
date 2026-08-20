import { screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { useEffect } from 'react';
import { Route, Routes } from 'react-router-dom';
import { describe, expect, it, vi, beforeEach, afterEach } from 'vitest';
import { FinancePage } from '@/features/finance/pages/FinancePage';
import type { FinancialCategory } from '@/features/finance/types/category';
import type { FinanceSummary } from '@/features/finance/types/summary';
import type { FinancialTransaction } from '@/features/finance/types/transaction';
import { usePermissions } from '@/features/permissions/PermissionProvider';
import { renderWithProviders } from '@/test/testUtils';

const categories: FinancialCategory[] = [
  {
    id: 'f7000000-0000-4000-8000-000000000001',
    code: 'USLUGI',
    name: 'Usługi',
    type: 'INCOME',
    active: true,
    createdAt: '2025-01-01T00:00:00Z',
    updatedAt: '2025-01-01T00:00:00Z',
  },
  {
    id: 'f7000000-0000-4000-8000-000000000013',
    code: 'CHEMIA',
    name: 'Chemia',
    type: 'EXPENSE',
    active: true,
    createdAt: '2025-01-01T00:00:00Z',
    updatedAt: '2025-01-01T00:00:00Z',
  },
];

const transactions: FinancialTransaction[] = [
  {
    id: 'f7100000-0000-4000-8000-000000000001',
    code: 'DEMO-FN-001',
    transactionDate: '2026-08-01',
    type: 'INCOME',
    netAmount: 1500,
    vatRate: 23,
    vatAmount: 345,
    grossAmount: 1845,
    categoryId: categories[0].id,
    categoryCode: categories[0].code,
    categoryName: categories[0].name,
    contractorName: 'Demo Wspólnota',
    contractorNip: '6443561947',
    buildingId: 'd0000000-0000-4000-8000-000000000001',
    buildingCode: 'BD0001',
    buildingName: 'Demo Budynek',
    employeeId: null,
    employeeCode: null,
    employeeName: null,
    vehicleId: null,
    vehicleCode: null,
    vehicleRegistrationNumber: null,
    equipmentId: null,
    equipmentCode: null,
    equipmentName: null,
    chemicalId: null,
    chemicalCode: null,
    chemicalName: null,
    description: 'Synthetic income',
    documentNumber: 'FV/DEMO/001',
    dueDate: '2026-08-15',
    paymentDate: '2026-08-14',
    paymentStatus: 'PAID',
    status: 'ACTIVE',
    notes: null,
    createdAt: '2025-01-01T00:00:00Z',
    updatedAt: '2025-01-01T00:00:00Z',
  },
  {
    id: 'f7100000-0000-4000-8000-000000000004',
    code: 'DEMO-FN-004',
    transactionDate: '2026-08-10',
    type: 'EXPENSE',
    netAmount: 500,
    vatRate: 23,
    vatAmount: 115,
    grossAmount: 615,
    categoryId: categories[1].id,
    categoryCode: categories[1].code,
    categoryName: categories[1].name,
    contractorName: 'Demo Supplier',
    contractorNip: '1234567890',
    buildingId: null,
    buildingCode: null,
    buildingName: null,
    employeeId: null,
    employeeCode: null,
    employeeName: null,
    vehicleId: null,
    vehicleCode: null,
    vehicleRegistrationNumber: null,
    equipmentId: null,
    equipmentCode: null,
    equipmentName: null,
    chemicalId: null,
    chemicalCode: null,
    chemicalName: null,
    description: 'Synthetic expense',
    documentNumber: 'FZ/DEMO/001',
    dueDate: '2026-08-20',
    paymentDate: '2026-08-18',
    paymentStatus: 'PAID',
    status: 'ACTIVE',
    notes: null,
    createdAt: '2025-01-01T00:00:00Z',
    updatedAt: '2025-01-01T00:00:00Z',
  },
];

const summary: FinanceSummary = {
  incomeNet: 4300,
  incomeGross: 5289,
  expenseNet: 500,
  expenseGross: 615,
  operatingResultNet: 3800,
  receivables: 2800,
  liabilities: 0,
  overdueReceivables: 800,
  overdueLiabilities: 0,
};

function createFinanceFetchMock(options: { transactionsStatus?: number } = {}) {
  return vi.fn(async (input: RequestInfo | URL, init?: RequestInit) => {
    const url = String(input);

    if (url.includes('/api/finance/transactions') && (!init?.method || init.method === 'GET')) {
      if (options.transactionsStatus === 403) {
        return Response.json({ status: 403, message: 'Forbidden' }, { status: 403 });
      }
      return Response.json(transactions);
    }

    if (url.includes('/api/finance/categories') && (!init?.method || init.method === 'GET')) {
      return Response.json(categories);
    }

    if (url.includes('/api/finance/summary') && (!init?.method || init.method === 'GET')) {
      return Response.json(summary);
    }

    if (url.includes('/api/buildings') && (!init?.method || init.method === 'GET')) {
      return Response.json([
        {
          id: 'd0000000-0000-4000-8000-000000000001',
          code: 'BD0001',
          name: 'Demo Budynek',
          address: 'ul. Demo 1',
          city: 'Kraków',
          postalCode: '30-001',
          status: 'ACTIVE',
          serviceStartDate: '2024-01-01',
          notes: null,
          createdAt: '2025-01-01T00:00:00Z',
          updatedAt: '2025-01-01T00:00:00Z',
        },
      ]);
    }

    return new Response(null, { status: 404 });
  });
}

function FinancePageHarness() {
  const { loadPermissions } = usePermissions();

  useEffect(() => {
    void loadPermissions();
  }, [loadPermissions]);

  return (
    <Routes>
      <Route path="/finance" element={<FinancePage />} />
    </Routes>
  );
}

function renderFinancePage(permissions: string[] = ['FINANCES_VIEW']) {
  return renderWithProviders(<FinancePageHarness />, {
    routerProps: { initialEntries: ['/finance'] },
    permissionsAdapter: {
      loadPermissions: async () => permissions,
    },
  });
}

describe('FinancePage', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', createFinanceFetchMock());
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('renders page header and transactions by default', async () => {
    renderFinancePage();

    expect(await screen.findByRole('heading', { name: 'Finanse' })).toBeInTheDocument();
    expect(
      screen.getByText('Przychody, koszty i rozliczenia operacyjne.'),
    ).toBeInTheDocument();
    expect(within(screen.getByRole('table')).getByText('DEMO-FN-001')).toBeInTheDocument();
  });

  it('displays summary cards and currency formatting', async () => {
    renderFinancePage();

    await waitFor(() => {
      expect(screen.getByText('PRZYCHODY')).toBeInTheDocument();
    });

    const summaryCards = screen.getByText('PRZYCHODY').closest('.finance-summary-cards');
    expect(summaryCards).not.toBeNull();
    expect(within(summaryCards as HTMLElement).getByText('4 300,00 zł')).toBeInTheDocument();
    expect(within(summaryCards as HTMLElement).getByText('500,00 zł')).toBeInTheDocument();

    const table = screen.getByRole('table');
    expect(within(table).getByText('01/08/2026')).toBeInTheDocument();
    expect(within(table).getByText('1 500,00 zł')).toBeInTheDocument();
  });

  it('switches to categories tab', async () => {
    const user = userEvent.setup();
    renderFinancePage();

    await waitFor(() => {
      expect(within(screen.getByRole('table')).getByText('DEMO-FN-001')).toBeInTheDocument();
    });

    await user.click(screen.getByRole('tab', { name: 'Kategorie' }));

    await waitFor(() => {
      expect(within(screen.getByRole('table')).getByText('Usługi')).toBeInTheDocument();
    });

    expect(within(screen.getByRole('table')).getByText('Chemia')).toBeInTheDocument();
  });

  it('handles API 403 with forbidden message', async () => {
    vi.stubGlobal('fetch', createFinanceFetchMock({ transactionsStatus: 403 }));

    renderFinancePage();

    expect(await screen.findByText('Nie masz uprawnień do tej operacji.')).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: 'Brak dostępu' })).toBeInTheDocument();
  });
});
