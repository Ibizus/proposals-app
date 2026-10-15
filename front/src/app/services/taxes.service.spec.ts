import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TaxesService } from './taxes.service';
import { makeTaxes } from '../../testing/fixtures';

const TAXES_URL = 'http://localhost:8080/v1/api/taxes/';

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

  it('returns taxes sorted by salaryMax', () => {
    let result: number[] = [];
    service.getTaxes().subscribe((taxes) => (result = taxes.map((t) => t.salaryMax)));

    http
      .expectOne(TAXES_URL)
      .flush([makeTaxes({ salaryMax: 3000 }), makeTaxes({ salaryMax: 1000 }), makeTaxes({ salaryMax: 2000 })]);

    expect(result).toEqual([1000, 2000, 3000]);
  });

  it('queries taxes by salary and marital status', () => {
    service.getTaxesForSalaryAndMaritalStatus(1500, 1).subscribe();
    const req = http.expectOne(TAXES_URL + '?salary=1500&maritalStatus=1');
    expect(req.request.method).toBe('GET');
    req.flush(makeTaxes());
  });

  const bracketCases: [number, number][] = [
    [0, 20],
    [1, 18],
    [2, 16],
    [3, 14],
    [4, 12],
    [5, 10],
    [8, 10],
  ];
  bracketCases.forEach(([dependents, expectedIrs]) => {
    it(`picks IRS ${expectedIrs}% for ${dependents} dependents`, () => {
      let bracket: any;
      service.getTaxBracket(1500, 0, dependents).subscribe((b) => (bracket = b));

      http.expectOne(TAXES_URL + '?salary=1500&maritalStatus=0').flush(makeTaxes());

      expect(bracket).toEqual({ irs: expectedIrs, ss: 11 });
    });
  });

  it('propagates errors from the tax bracket lookup', () => {
    let failed = false;
    spyOn(console, 'error');
    service.getTaxBracket(1500, 0, 0).subscribe({ error: () => (failed = true) });

    http
      .expectOne(TAXES_URL + '?salary=1500&maritalStatus=0')
      .flush('boom', { status: 500, statusText: 'Server Error' });

    expect(failed).toBeTrue();
  });

  it('updates and deletes a tax row by id', () => {
    const tax = makeTaxes({ id: 9 });
    service.modifyTaxes(tax).subscribe();
    service.deleteTaxes(9).subscribe();

    const reqs = http.match(TAXES_URL + '9');
    expect(reqs.map((r) => r.request.method)).toEqual(['PUT', 'DELETE']);
    expect(reqs[0].request.body).toEqual(tax);
    reqs.forEach((r) => r.flush({}));
  });
});
