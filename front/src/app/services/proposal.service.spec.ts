import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ProposalService } from './proposal.service';
import { environment } from '../../environments/environment';
import { makeProposal, makeTaxConfiguration } from '../../testing/fixtures';
import { Tax, TaxBracket, TaxConfiguration } from '../utils/types';

const API = environment.API_ENDPOINT;
const TAX_CONFIG_URL = `${API}tax-configurations/`;
const PARAMETERS = [
  { name: 'iht', value: 10 },
  { name: 'spp', value: 5 },
  { name: 'scr', value: 1.5 },
  { name: 'unrelated', value: 99 },
];

const now = new Date();

function taxBracket(overrides: Partial<TaxBracket> = {}): TaxBracket {
  return {
    id: 1,
    threshold: 2000,
    rate: 20,
    createdAt: now,
    createdBy: '',
    modifiedAt: now,
    modifiedBy: '',
    ...overrides,
  };
}

function tax(overrides: Partial<Tax> = {}): Tax {
  return {
    id: 1,
    name: 'IRS',
    type: 'BRACKETED',
    fixedRate: null,
    brackets: [taxBracket()],
    createdAt: now,
    createdBy: '',
    modifiedAt: now,
    modifiedBy: '',
    ...overrides,
  };
}

function activeConfiguration(): TaxConfiguration {
  return {
    ...makeTaxConfiguration(),
    taxes: [
      tax({ id: 10, name: 'Social Security', type: 'FIXED', fixedRate: 11, brackets: [] }),
      tax({ id: 20, name: 'IRS', type: 'BRACKETED', brackets: [taxBracket({ threshold: 2000, rate: 20 })] }),
    ],
  };
}

describe('ProposalService', () => {
  let service: ProposalService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ProposalService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  function expectGet(url: string) {
    const req = http.expectOne(url);
    expect(req.request.method).toBe('GET');
    return req;
  }

  it('keeps only the iht, spp and scr parameters', () => {
    let params: Map<string, number> | undefined;
    service.getParameters().subscribe((p) => (params = p));

    expectGet(API + 'parameters').flush(PARAMETERS);

    expect(params).toEqual(
      new Map([
        ['iht', 10],
        ['spp', 5],
        ['scr', 1.5],
      ]),
    );
  });

  it('errors when parameters cannot be fetched', () => {
    let failed = false;
    spyOn(console, 'error');
    service.getParameters().subscribe({ error: () => (failed = true) });

    expectGet(API + 'parameters').flush('', { status: 503, statusText: 'Unavailable' });

    expect(failed).toBeTrue();
  });

  describe('salaryCalculations', () => {
    function run(irs?: number, ss?: number) {
      let result = new Map<string, Map<string, number>>();
      service.salaryCalculations(makeProposal(), irs, ss).subscribe((r) => (result = r));

      // Active configuration for the proposal, then parameters.
      expectGet(`${TAX_CONFIG_URL}active`).flush(activeConfiguration());
      expectGet(API + 'parameters').flush(PARAMETERS);

      return result;
    }

    it('computes monthly and yearly breakdowns from taxes and parameters', () => {
      const monthly = run().get('monthly') as Map<string, number>;

      expect(monthly.get('Gross Salary')).toBe(1000);
      expect(monthly.get('Base Salary')).toBeCloseTo(1350, 6);
      expect(monthly.get('IHT')).toBe(100);
      expect(monthly.get('Share Purchase Plan')).toBe(50);
      // 1000 - (11% SS + 20% IRS)
      expect(monthly.get('Estimated Net Salary')).toBeCloseTo(690, 6);
      expect(service.getScrValue()).toBeCloseTo(18, 6);
    });

    it('lets callers override IRS and SS rates', () => {
      const monthly = run(30, 10).get('monthly') as Map<string, number>;
      expect(monthly.get('Estimated Net Salary')).toBeCloseTo(600, 6);
    });
  });

  it('builds the paginated proposals URL, with and without a filter', () => {
    service.getProposals(2, 9.5, '').subscribe();
    service.getProposals(0, 10, 'dev').subscribe();

    expectGet(API + 'proposals/?page=2&size=10').flush({});
    expectGet(API + 'proposals/?page=0&size=10&filter=dev').flush({});
  });

  it('uses REST verbs for proposal CRUD', () => {
    const proposal = makeProposal();
    service.getProposal(7).subscribe();
    service.modifyProposal(proposal).subscribe();
    service.deleteProposal(7).subscribe();
    service.createProposal(proposal).subscribe();

    expect(http.match(API + 'proposals/7').map((r) => r.request.method)).toEqual([
      'GET',
      'PUT',
      'DELETE',
    ]);
    const create = http.expectOne(API + 'proposals/');
    expect(create.request.method).toBe('POST');
    expect(create.request.body).toEqual(proposal);
  });

  it('shares the current proposal between components', () => {
    const seen: unknown[] = [];
    service.currentProposal.subscribe((p) => seen.push(p));
    const proposal = makeProposal();

    service.changeProposal(proposal);

    expect(seen).toEqual([null, proposal]);
  });
});
