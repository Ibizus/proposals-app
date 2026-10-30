import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TaxesService } from './taxes.service';
import { makeTaxConfiguration } from '../../testing/fixtures';
import { environment } from '../../environments/environment';
import { Tax, TaxBracket, TaxConfiguration } from '../utils/types';

const TAX_CONFIG_URL = `${environment.API_ENDPOINT}tax-configurations/`;

const now = new Date();

function taxBracket(overrides: Partial<TaxBracket> = {}): TaxBracket {
  return {
    id: 1,
    threshold: 1000,
    rate: 10,
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
    brackets: [],
    createdAt: now,
    createdBy: '',
    modifiedAt: now,
    modifiedBy: '',
    ...overrides,
  };
}

describe('TaxesService', () => {
  let service: TaxesService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(TaxesService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('returns active configuration taxes sorted by threshold', () => {
    let result: number[] = [];
    service.getTaxes().subscribe((taxes) => (result = taxes.map((t) => t.salaryMax)));

    http.expectOne(`${TAX_CONFIG_URL}active`).flush({
      ...makeTaxConfiguration(),
      taxes: [
        tax({ id: 10, name: 'Social Security', type: 'FIXED', fixedRate: 11, brackets: [] }),
        tax({
          id: 20,
          name: 'IRS',
          type: 'BRACKETED',
          brackets: [
            taxBracket({ id: 1, threshold: 3000, rate: 20 }),
            taxBracket({ id: 2, threshold: 1000, rate: 10 }),
            taxBracket({ id: 3, threshold: 2000, rate: 15 }),
          ],
        }),
      ],
    } as TaxConfiguration);

    expect(result).toEqual([1000, 2000, 3000]);
  });

  it('queries the active configuration for tax bracket calculation', () => {
    let bracket: { irs: number; ss: number; other: number } | undefined;
    service.getTaxBracket(1500, 0, 0).subscribe((b) => (bracket = b));

    http.expectOne(`${TAX_CONFIG_URL}active`).flush({
      ...makeTaxConfiguration(),
      yearlyPayments: 14,
      taxes: [
        tax({ id: 10, name: 'Social Security', type: 'FIXED', fixedRate: 11, brackets: [] }),
        tax({
          id: 20,
          name: 'IRS',
          type: 'BRACKETED',
          brackets: [
            taxBracket({ id: 1, threshold: 1000, rate: 10 }),
            taxBracket({ id: 2, threshold: 2000, rate: 20 }),
          ],
        }),
      ],
    } as TaxConfiguration);

    expect(bracket).toEqual({ irs: (200 / 1500) * 100, ss: 11, other: 0 });
  });

  it('matches income tax and social security by name and reports remaining taxes apart', () => {
    let bracket: { irs: number; ss: number; other: number } | undefined;
    service.getTaxBracket(1000, 0, 0).subscribe((b) => (bracket = b));

    http.expectOne(`${TAX_CONFIG_URL}active`).flush({
      ...makeTaxConfiguration(),
      taxes: [
        tax({ id: 30, name: 'Pension', type: 'FIXED', fixedRate: 2, brackets: [] }),
        tax({
          id: 20,
          name: 'Income Tax',
          type: 'WITHHOLDING',
          brackets: [taxBracket({ id: 1, threshold: 9999999, rate: 15 })],
        }),
        tax({ id: 10, name: 'Social Security', type: 'FIXED', fixedRate: 6.35, brackets: [] }),
      ],
    } as TaxConfiguration);

    expect(bracket).toEqual({ irs: 15, ss: 6.35, other: 2 });
  });

  it('lists all configurations', () => {
    service.getTaxConfigurations().subscribe();

    const req = http.expectOne(TAX_CONFIG_URL);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('activates a configuration', () => {
    service.activateConfiguration(2).subscribe();

    const req = http.expectOne(`${TAX_CONFIG_URL}2/activate`);
    expect(req.request.method).toBe('POST');
    req.flush(makeTaxConfiguration());
  });

  it('propagates errors from the tax configuration lookup', () => {
    let failed = false;
    spyOn(console, 'error');
    service.getTaxBracket(1500, 0, 0).subscribe({ error: () => (failed = true) });

    http
      .expectOne(`${TAX_CONFIG_URL}active`)
      .flush('boom', { status: 500, statusText: 'Server Error' });

    expect(failed).toBeTrue();
  });
});
