import { render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { useBuildings } from '@/features/buildings/hooks/useBuildings';
import { useEmployees } from '@/features/employees/hooks/useEmployees';
import { TestAuthProvider } from '@/test/testAuthProvider';

const ORG_A = '11111111-1111-1111-1111-111111111111';
const ORG_B = '22222222-2222-2222-2222-222222222222';

const getBuildings = vi.fn();
const getEmployees = vi.fn();

vi.mock('@/features/buildings/api/buildingsApi', () => ({
  getBuildings: (...args: unknown[]) => getBuildings(...args),
}));

vi.mock('@/features/employees/api/employeesApi', () => ({
  getEmployees: (...args: unknown[]) => getEmployees(...args),
}));

function BuildingsProbe() {
  const { buildings } = useBuildings({ status: 'ACTIVE' });
  return (
    <div data-testid="buildings">{buildings.map((building) => building.code).join(',')}</div>
  );
}

function EmployeesProbe() {
  const { employees } = useEmployees({});
  return (
    <div data-testid="employees">{employees.map((employee) => employee.code).join(',')}</div>
  );
}

function renderWithOrganizationContext(
  probe: 'buildings' | 'employees',
  organizationContextKey: string,
) {
  const Probe = probe === 'buildings' ? BuildingsProbe : EmployeesProbe;

  return render(
    <TestAuthProvider value={{ organizationContextKey }}>
      <Probe />
    </TestAuthProvider>,
  );
}

describe('tenant scope refetch on organization context change', () => {
  beforeEach(() => {
    getBuildings.mockReset();
    getEmployees.mockReset();
  });

  it('useBuildings refetches and replaces stale organization A data after switch to B', async () => {
    getBuildings
      .mockResolvedValueOnce([
        {
          id: '1',
          code: 'ORG-A-BLD',
          name: 'Org A Building',
          status: 'ACTIVE',
        },
      ])
      .mockResolvedValueOnce([
        {
          id: '2',
          code: 'ORG-B-BLD',
          name: 'Org B Building',
          status: 'ACTIVE',
        },
      ]);

    const { rerender } = renderWithOrganizationContext('buildings', ORG_A);

    await waitFor(() => {
      expect(screen.getByTestId('buildings')).toHaveTextContent('ORG-A-BLD');
    });
    expect(getBuildings).toHaveBeenCalledTimes(1);

    rerender(
      <TestAuthProvider value={{ organizationContextKey: ORG_B }}>
        <BuildingsProbe />
      </TestAuthProvider>,
    );

    await waitFor(() => {
      expect(screen.getByTestId('buildings')).toHaveTextContent('ORG-B-BLD');
    });
    expect(screen.getByTestId('buildings')).not.toHaveTextContent('ORG-A-BLD');
    expect(getBuildings).toHaveBeenCalledTimes(2);
  });

  it('useEmployees refetches and replaces stale organization A data after switch to B', async () => {
    getEmployees
      .mockResolvedValueOnce([
        {
          id: '1',
          code: 'ORG-A-EMP',
          firstName: 'Anna',
          lastName: 'Alpha',
          active: true,
        },
      ])
      .mockResolvedValueOnce([
        {
          id: '2',
          code: 'ORG-B-EMP',
          firstName: 'Bartek',
          lastName: 'Beta',
          active: true,
        },
      ]);

    const { rerender } = renderWithOrganizationContext('employees', ORG_A);

    await waitFor(() => {
      expect(screen.getByTestId('employees')).toHaveTextContent('ORG-A-EMP');
    });
    expect(getEmployees).toHaveBeenCalledTimes(1);

    rerender(
      <TestAuthProvider value={{ organizationContextKey: ORG_B }}>
        <EmployeesProbe />
      </TestAuthProvider>,
    );

    await waitFor(() => {
      expect(screen.getByTestId('employees')).toHaveTextContent('ORG-B-EMP');
    });
    expect(screen.getByTestId('employees')).not.toHaveTextContent('ORG-A-EMP');
    expect(getEmployees).toHaveBeenCalledTimes(2);
  });

  it('useBuildings does not fetch when organization context is missing', async () => {
    getBuildings.mockResolvedValue([]);

    render(
      <TestAuthProvider value={{ organizationContextKey: null }}>
        <BuildingsProbe />
      </TestAuthProvider>,
    );

    await waitFor(() => {
      expect(screen.getByTestId('buildings')).toHaveTextContent('');
    });
    expect(getBuildings).not.toHaveBeenCalled();
  });

  it('useBuildings refetches when organization context changes from null to active', async () => {
    getBuildings.mockResolvedValueOnce([
      {
        id: '2',
        code: 'ORG-B-BLD',
        name: 'Org B Building',
        status: 'ACTIVE',
      },
    ]);

    const { rerender } = render(
      <TestAuthProvider value={{ organizationContextKey: null }}>
        <BuildingsProbe />
      </TestAuthProvider>,
    );

    await waitFor(() => {
      expect(getBuildings).not.toHaveBeenCalled();
    });

    rerender(
      <TestAuthProvider value={{ organizationContextKey: ORG_B }}>
        <BuildingsProbe />
      </TestAuthProvider>,
    );

    await waitFor(() => {
      expect(screen.getByTestId('buildings')).toHaveTextContent('ORG-B-BLD');
    });
    expect(getBuildings).toHaveBeenCalledTimes(1);
  });
});
