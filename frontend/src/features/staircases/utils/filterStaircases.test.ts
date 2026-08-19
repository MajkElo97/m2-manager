import { describe, expect, it } from 'vitest';
import type { Building } from '@/features/buildings/types/building';
import type { Staircase } from '@/features/staircases/types/staircase';
import { filterStaircases } from '@/features/staircases/utils/filterStaircases';

const buildingId = '11111111-1111-1111-1111-111111111111';

const building: Building = {
  id: buildingId,
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
};

const staircase: Staircase = {
  id: 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
  buildingId,
  code: 'KL0001',
  designation: '1',
  intercomCode: '#2258',
  keyRequired: true,
  elevator: false,
  floors: 4,
  notes: null,
  createdAt: '2025-01-01T00:00:00Z',
  updatedAt: '2025-01-01T00:00:00Z',
};

describe('filterStaircases', () => {
  it('matches search by building code and staircase code', () => {
    const buildingsById = new Map([[buildingId, building]]);

    expect(
      filterStaircases([staircase], buildingsById, {
        search: 'PUSTA64',
        buildingId: 'ALL',
        elevator: 'ALL',
        keyRequired: 'ALL',
      }),
    ).toHaveLength(1);

    expect(
      filterStaircases([staircase], buildingsById, {
        search: 'KL0001',
        buildingId: 'ALL',
        elevator: 'ALL',
        keyRequired: 'ALL',
      }),
    ).toHaveLength(1);
  });

  it('filters by building id and boolean flags', () => {
    const buildingsById = new Map([[buildingId, building]]);

    expect(
      filterStaircases([staircase], buildingsById, {
        search: '',
        buildingId,
        elevator: 'NO',
        keyRequired: 'YES',
      }),
    ).toHaveLength(1);

    expect(
      filterStaircases([staircase], buildingsById, {
        search: '',
        buildingId,
        elevator: 'YES',
        keyRequired: 'ALL',
      }),
    ).toHaveLength(0);
  });
});
