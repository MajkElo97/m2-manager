import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import type { ComponentProps } from 'react';
import { describe, expect, it, vi } from 'vitest';
import { Topbar } from '@/components/layout/Topbar';
import { TestAuthProvider } from '@/test/testAuthProvider';
import { ThemeProvider } from '@/hooks/ThemeProvider';

function renderTopbar(
  authOverrides?: Parameters<typeof TestAuthProvider>[0]['value'],
  props?: Partial<ComponentProps<typeof Topbar>>,
) {
  return render(
    <ThemeProvider>
      <TestAuthProvider value={authOverrides}>
        <Topbar onMenuClick={vi.fn()} showMenuButton={false} {...props} />
      </TestAuthProvider>
    </ThemeProvider>,
  );
}

describe('Topbar organization context', () => {
  it('renders user name and email', () => {
    renderTopbar({
      context: {
        user: {
          id: 'user-1',
          name: 'Michał Ociepka',
          email: 'm.ocpieka97@gmail.com',
        },
        activeOrganization: {
          id: 'org-1',
          name: 'M2 Group',
          slug: 'm2-group',
        },
        availableOrganizations: [
          { id: 'org-1', name: 'M2 Group', slug: 'm2-group' },
        ],
        canSwitchOrganizations: false,
        mustChangePassword: false,
        superAdmin: false,
      },
    });

    expect(screen.getByText('Michał Ociepka')).toBeInTheDocument();
    expect(screen.getByText('m.ocpieka97@gmail.com')).toBeInTheDocument();
  });

  it('shows organization name without selector for single organization', () => {
    renderTopbar({
      context: {
        user: { id: 'user-1', name: 'Michał Ociepka', email: 'm.ocpieka97@gmail.com' },
        activeOrganization: { id: 'org-1', name: 'M2 Group', slug: 'm2-group' },
        availableOrganizations: [{ id: 'org-1', name: 'M2 Group', slug: 'm2-group' }],
        canSwitchOrganizations: false,
        mustChangePassword: false,
        superAdmin: false,
      },
    });

    expect(screen.getByText('M2 Group')).toBeInTheDocument();
    expect(screen.queryByRole('combobox', { name: 'Wybierz organizację' })).not.toBeInTheDocument();
  });

  it('shows organization selector for multiple organizations', () => {
    renderTopbar({
      context: {
        user: { id: 'user-1', name: 'Michał Ociepka', email: 'm.ocpieka97@gmail.com' },
        activeOrganization: { id: 'org-1', name: 'M2 Group', slug: 'm2-group' },
        availableOrganizations: [
          { id: 'org-1', name: 'M2 Group', slug: 'm2-group' },
          { id: 'org-2', name: 'Kozera Nieruchomości', slug: 'kozera-nieruchomosci' },
        ],
        canSwitchOrganizations: true,
        mustChangePassword: false,
        superAdmin: false,
      },
    });

    const selector = screen.getByRole('combobox', { name: 'Wybierz organizację' });
    expect(selector).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'M2 Group' })).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'Kozera Nieruchomości' })).toBeInTheDocument();
  });

  it('calls switchOrganization when selecting another organization', async () => {
    const user = userEvent.setup();
    const switchOrganization = vi.fn(async () => {});

    renderTopbar({
      context: {
        user: { id: 'user-1', name: 'Michał Ociepka', email: 'm.ocpieka97@gmail.com' },
        activeOrganization: { id: 'org-1', name: 'M2 Group', slug: 'm2-group' },
        availableOrganizations: [
          { id: 'org-1', name: 'M2 Group', slug: 'm2-group' },
          { id: 'org-2', name: 'Kozera Nieruchomości', slug: 'kozera-nieruchomosci' },
        ],
        canSwitchOrganizations: true,
        mustChangePassword: false,
        superAdmin: false,
      },
      switchOrganization,
    });

    await user.selectOptions(
      screen.getByRole('combobox', { name: 'Wybierz organizację' }),
      'org-2',
    );

    expect(switchOrganization).toHaveBeenCalledWith('org-2');
  });

  it('shows SUPER ADMIN badge for super admin users', () => {
    renderTopbar({
      context: {
        user: { id: 'super-1', name: 'Admin M2', email: 'admin@m2manager.local' },
        activeOrganization: { id: 'org-dev', name: 'M2 Manager Dev', slug: 'm2-manager-dev' },
        availableOrganizations: [{ id: 'org-dev', name: 'M2 Manager Dev', slug: 'm2-manager-dev' }],
        canSwitchOrganizations: true,
        mustChangePassword: false,
        superAdmin: true,
      },
    });

    expect(screen.getByText('SUPER ADMIN')).toBeInTheDocument();
    expect(screen.getByText('M2 Manager Dev')).toBeInTheDocument();
  });

  it('does not show SUPER ADMIN badge for regular users', () => {
    renderTopbar({
      context: {
        user: { id: 'user-1', name: 'Michał Ociepka', email: 'multiadmin@m2manager.local' },
        activeOrganization: { id: 'org-1', name: 'M2 Group', slug: 'm2-group' },
        availableOrganizations: [{ id: 'org-1', name: 'M2 Group', slug: 'm2-group' }],
        canSwitchOrganizations: false,
        mustChangePassword: false,
        superAdmin: false,
      },
    });

    expect(screen.queryByText('SUPER ADMIN')).not.toBeInTheDocument();
  });

  it('shows Brak organizacji selector for super admin without active organization', () => {
    renderTopbar({
      context: {
        user: { id: 'super-1', name: 'Admin M2', email: 'admin@m2manager.local' },
        activeOrganization: null,
        availableOrganizations: [
          { id: 'org-dev', name: 'M2 Manager Dev', slug: 'm2-manager-dev' },
          { id: 'org-2', name: 'Secondary Org', slug: 'm2-manager-dev-secondary' },
        ],
        canSwitchOrganizations: true,
        mustChangePassword: false,
        superAdmin: true,
      },
    });

    const selector = screen.getByRole('combobox', { name: 'Wybierz organizację' });
    expect(selector).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'Brak organizacji' })).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'M2 Manager Dev' })).toBeInTheDocument();
    expect(screen.queryByRole('option', { name: 'ADMIN' })).not.toBeInTheDocument();
  });

  it('calls switchOrganization when super admin selects a business organization', async () => {
    const user = userEvent.setup();
    const switchOrganization = vi.fn(async () => {});

    renderTopbar({
      context: {
        user: { id: 'super-1', name: 'Admin M2', email: 'admin@m2manager.local' },
        activeOrganization: null,
        availableOrganizations: [{ id: 'org-dev', name: 'M2 Manager Dev', slug: 'm2-manager-dev' }],
        canSwitchOrganizations: true,
        mustChangePassword: false,
        superAdmin: true,
      },
      switchOrganization,
    });

    await user.selectOptions(screen.getByRole('combobox', { name: 'Wybierz organizację' }), 'org-dev');
    expect(switchOrganization).toHaveBeenCalledWith('org-dev');
  });
});
