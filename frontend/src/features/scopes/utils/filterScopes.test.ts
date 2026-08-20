import { describe, expect, it } from 'vitest';
import type { Activity } from '@/features/activities/types/activity';
import type { Building } from '@/features/buildings/types/building';
import type { Scope } from '@/features/scopes/types/scope';
import { filterScopes } from '@/features/scopes/utils/filterScopes';

const buildingAId = '11111111-1111-1111-1111-111111111111';
const buildingBId = '22222222-2222-2222-2222-222222222222';
const activityAId = 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa';
const activityBId = 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb';

const buildingsById = new Map<string, Building>([
  [
    buildingAId,
    {
      id: buildingAId,
      code: 'PUSTA64',
      name: 'Pusta 64',
      address: 'ul. Pusta 64',
      city: 'Warszawa',
      nip: null,
      phone: null,
      email: null,
      managerCode: null,
      supervisorCode: null,
      employeeCode: null,
      contractSignedAt: null,
      serviceStartDate: null,
      noticePeriodMonths: 3,
      status: 'ACTIVE',
      notes: null,
      createdAt: '2025-01-01T00:00:00Z',
      updatedAt: '2025-01-01T00:00:00Z',
    },
  ],
  [
    buildingBId,
    {
      id: buildingBId,
      code: 'KASPRZAKA6',
      name: 'Kasprzaka 6',
      address: 'ul. Kasprzaka 6',
      city: 'Dąbrowa Górnicza',
      nip: null,
      phone: null,
      email: null,
      managerCode: null,
      supervisorCode: null,
      employeeCode: null,
      contractSignedAt: null,
      serviceStartDate: null,
      noticePeriodMonths: 3,
      status: 'ACTIVE',
      notes: null,
      createdAt: '2025-01-01T00:00:00Z',
      updatedAt: '2025-01-01T00:00:00Z',
    },
  ],
]);

const activitiesById = new Map<string, Activity>([
  [
    activityAId,
    {
      id: activityAId,
      code: 'CZ0001',
      name: 'Tereny zewnętrzne',
      category: 'Sprzątanie',
      planningType: 'CYCLIC',
      defaultPeriod: null,
      durationMinutes: 30,
      priority: 'NORMAL',
      active: true,
      system: true,
      manageable: false,
      createdAt: '2025-01-01T00:00:00Z',
      updatedAt: '2025-01-01T00:00:00Z',
    },
  ],
  [
    activityBId,
    {
      id: activityBId,
      code: 'CZ0018',
      name: 'Odśnieżanie',
      category: 'Zimowe',
      planningType: 'ON_DEMAND',
      defaultPeriod: 'ZIMA',
      durationMinutes: 120,
      priority: 'HIGH',
      active: true,
      system: true,
      manageable: false,
      createdAt: '2025-01-01T00:00:00Z',
      updatedAt: '2025-01-01T00:00:00Z',
    },
  ],
]);

const scopes: Scope[] = [
  {
    id: 'scope-1',
    code: 'ZP0001',
    buildingId: buildingAId,
    activityId: activityAId,
    planningType: 'WEEKLY',
    frequency: 1,
    weekdays: 'Wtorek',
    notes: null,
    status: 'ACTIVE',
    createdAt: '2025-01-01T00:00:00Z',
    updatedAt: '2025-01-01T00:00:00Z',
  },
  {
    id: 'scope-2',
    code: 'ZP0006',
    buildingId: buildingAId,
    activityId: activityBId,
    planningType: 'EVENT',
    frequency: null,
    weekdays: null,
    notes: 'Wg potrzeb',
    status: 'ACTIVE',
    createdAt: '2025-01-01T00:00:00Z',
    updatedAt: '2025-01-01T00:00:00Z',
  },
  {
    id: 'scope-3',
    code: 'ZP0008',
    buildingId: buildingBId,
    activityId: activityAId,
    planningType: 'WEEKLY',
    frequency: 1,
    weekdays: 'Czwartek',
    notes: null,
    status: 'INACTIVE',
    createdAt: '2025-01-01T00:00:00Z',
    updatedAt: '2025-01-01T00:00:00Z',
  },
];

describe('filterScopes', () => {
  it('returns all scopes when filters are ALL and search is empty', () => {
    const result = filterScopes(scopes, buildingsById, activitiesById, {
      search: '',
      buildingId: 'ALL',
      activityId: 'ALL',
      planningType: 'ALL',
      status: 'ALL',
    });

    expect(result).toHaveLength(3);
  });

  it('filters by building', () => {
    const result = filterScopes(scopes, buildingsById, activitiesById, {
      search: '',
      buildingId: buildingBId,
      activityId: 'ALL',
      planningType: 'ALL',
      status: 'ALL',
    });

    expect(result).toHaveLength(1);
    expect(result[0]?.code).toBe('ZP0008');
  });

  it('filters by activity', () => {
    const result = filterScopes(scopes, buildingsById, activitiesById, {
      search: '',
      buildingId: 'ALL',
      activityId: activityBId,
      planningType: 'ALL',
      status: 'ALL',
    });

    expect(result).toHaveLength(1);
    expect(result[0]?.code).toBe('ZP0006');
  });

  it('filters by planning type and status', () => {
    const result = filterScopes(scopes, buildingsById, activitiesById, {
      search: '',
      buildingId: 'ALL',
      activityId: 'ALL',
      planningType: 'WEEKLY',
      status: 'ACTIVE',
    });

    expect(result).toHaveLength(1);
    expect(result[0]?.code).toBe('ZP0001');
  });

  it('filters by search across building and activity fields', () => {
    const result = filterScopes(scopes, buildingsById, activitiesById, {
      search: 'KASPRZAKA6',
      buildingId: 'ALL',
      activityId: 'ALL',
      planningType: 'ALL',
      status: 'ALL',
    });

    expect(result).toHaveLength(1);
    expect(result[0]?.code).toBe('ZP0008');
  });
});
