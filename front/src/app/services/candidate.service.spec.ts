import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { CandidateService } from './candidate.service';
import { environment } from '../../environments/environment';
import { makeCandidate } from '../../testing/fixtures';

const API = environment.API_ENDPOINT;

describe('CandidateService', () => {
  let service: CandidateService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(CandidateService);
    http = TestBed.inject(HttpTestingController);
    spyOn(console, 'log');
  });

  afterEach(() => http.verify());

  function expectGet(url: string) {
    const req = http.expectOne(url);
    expect(req.request.method).toBe('GET');
    return req;
  }

  it('builds the paginated candidates URL, rounding size up', () => {
    service.getCandidates(1, 10.2, '').subscribe();
    expectGet(API + 'candidates/?page=1&size=11').flush({});
  });

  it('appends the filter when one is given', () => {
    service.getCandidates(0, 10, 'ana').subscribe();
    expectGet(API + 'candidates/?page=0&size=10&filter=ana').flush({});
  });

  it('fetches a candidate by id', () => {
    let candidate: unknown;
    service.getCandidateById(1).subscribe((c) => (candidate = c));

    expectGet(API + 'candidates/1').flush(makeCandidate());

    expect(candidate).toEqual(makeCandidate());
  });

  it('creates, modifies and deletes candidates', () => {
    const candidate = makeCandidate({ id: 3 });
    service.createCandidate(candidate).subscribe();
    service.modifyCandidate(candidate).subscribe();
    service.deleteCandidate(3).subscribe();

    const create = http.expectOne(API + 'candidates/');
    expect(create.request.method).toBe('POST');
    expect(create.request.body).toEqual(candidate);
    create.flush({});
    expect(http.match(API + 'candidates/3').map((r) => r.request.method)).toEqual([
      'PUT',
      'DELETE',
    ]);
  });

  it('fetches proposals for a candidate', () => {
    service.getProposalsByCandidateId(4).subscribe();
    expectGet(API + 'proposals?candidate=4').flush([]);
  });

  it('shares the current candidate between components', () => {
    const seen: unknown[] = [];
    service.currentCandidate.subscribe((c) => seen.push(c));
    const candidate = makeCandidate();

    service.changeCandidate(candidate);

    expect(seen).toEqual([null, candidate]);
  });
});
