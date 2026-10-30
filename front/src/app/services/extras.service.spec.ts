import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ExtrasService } from './extras.service';
import { environment } from '../../environments/environment';
import { makeExtra } from '../../testing/fixtures';

const API = environment.API_ENDPOINT;

describe('ExtrasService', () => {
  let service: ExtrasService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ExtrasService);
    http = TestBed.inject(HttpTestingController);
    spyOn(console, 'log');
  });

  afterEach(() => http.verify());

  it('fetches paginated extra templates', () => {
    let data: unknown;
    service.getExtras(0, 4.1).subscribe((d) => (data = d));

    http.expectOne(API + 'extratemplates/?page=0&size=5').flush({ content: [] });

    expect(data).toEqual({ content: [] });
  });

  it('scopes extra templates to the given tax configuration', () => {
    service.getExtras(0, 100, 2).subscribe();

    http.expectOne(API + 'extratemplates/?page=0&size=100&taxConfigurationId=2').flush({});
  });

  it('duplicates an extra as a new one (id 0) without mutating the original', () => {
    const extra = makeExtra({ id: 42 });
    service.duplicateExtra(extra).subscribe();

    const req = http.expectOne(API + 'extratemplates/');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ ...extra, id: 0 });
    expect(extra.id).toBe(42);
    req.flush({});
  });

  it('updates and deletes extras by id', () => {
    const extra = makeExtra();
    service.updateExtra(extra, 5).subscribe();
    service.deleteExtra(5).subscribe();

    expect(http.match(API + 'extratemplates/5').map((r) => r.request.method)).toEqual([
      'PUT',
      'DELETE',
    ]);
  });
});
