import { Candidate, Extra, Position, Proposal, Taxes } from '../app/utils/types';

export function makeCandidate(overrides: Partial<Candidate> = {}): Candidate {
  return {
    '@id': 1,
    id: 1,
    name: 'Ana',
    surname: 'Silva',
    idNumber: '12345678A',
    phone: '600000000',
    email: 'ana.silva@example.com',
    workerId: 'W-001',
    maritalStatus: 0,
    dependentsNumber: 0,
    nJoynSet: [],
    proposals: [],
    position: null,
    ...overrides,
  };
}

export function makePosition(overrides: Partial<Position> = {}): Position {
  const at = new Date('2024-01-01T00:00:00Z');
  return {
    id: 1,
    title: 'Backend Developer',
    description: 'Develops backend services',
    njoyn: 'NJ-1',
    knowMore: 'https://example.com/positions/backend',
    candidates: [1, 2],
    createdAt: at,
    createdBy: 'test',
    modifiedAt: at,
    modifiedBy: 'test',
    ...overrides,
  };
}

export function makeExtra(overrides: Partial<Extra> = {}): Extra {
  const at = new Date('2024-01-01T00:00:00Z');
  return {
    id: 1,
    name: 'Meal allowance',
    amount: 100,
    ssTaxrate: 10,
    irsTaxrate: 20,
    isPercentage: false,
    isSSTaxable: true,
    isIRSTaxable: true,
    isDefault: false,
    addToSalary: false,
    multiplyYearly: 11,
    multiplyMonthly: 1,
    createdAt: at,
    createdBy: 'test',
    modifiedAt: at,
    modifiedBy: 'test',
    ...overrides,
  };
}

export function makeTaxes(overrides: Partial<Taxes> = {}): Taxes {
  const at = new Date('2024-01-01T00:00:00Z');
  return {
    id: 1,
    salaryMax: 2000,
    socialSecurityTax: 11,
    taxPercentageCompany: 23.75,
    validFrom: at,
    validUntil: at,
    maritalStatus: 0,
    zero: 20,
    one: 18,
    two: 16,
    three: 14,
    four: 12,
    five: 10,
    createdAt: at,
    createdBy: 'test',
    modifiedAt: at,
    modifiedBy: 'test',
    ...overrides,
  };
}

export function makeProposal(overrides: Partial<Proposal> = {}): Proposal {
  const at = new Date('2024-01-01T00:00:00Z');
  return {
    '@id': 7,
    id: 7,
    njoyn: 'NJ-7',
    description: 'Backend developer',
    role: 'Developer',
    contractLevel: 'B2',
    startDate: '2024-02-01',
    dueDate: '2024-12-31',
    indefinite: true,
    knowMore: '',
    office: { id: 1, name: 'Lisbon', code: 'LIS' },
    salary: { id: 1, base: 1000, extras: [] },
    candidate: makeCandidate(),
    position: null,
    statuses: [],
    createdAt: at,
    createdBy: 'test',
    modifiedAt: at,
    modifiedBy: 'test',
    ...overrides,
  };
}
