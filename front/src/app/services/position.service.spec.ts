import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { PositionService } from './position.service';
import { environment } from '../../environments/environment';
import { makePosition } from '../../testing/fixtures';

const API = environment.API_ENDPOINT + 'positions/';

describe('PositionService', () => {
  let service: PositionService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(PositionService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('fetches a paginated page of positions', () => {
    let data: unknown;
    service.getPositions(1, 4.5).subscribe((d) => (data = d));

    http.expectOne(API + '?page=1&size=5').flush({ positions: [], totalItems: 0 });

    expect(data).toEqual({ positions: [], totalItems: 0 });
  });

  it('fetches every position for the pickers', () => {
    let data: unknown;
    service.getAllPositions().subscribe((d) => (data = d));

    http.expectOne(API).flush([makePosition()]);

    expect(data).toEqual([makePosition()]);
  });

  it('uses REST verbs for position CRUD', () => {
    const position = makePosition();
    service.getPosition(7).subscribe();
    service.createPosition(position).subscribe();
    service.modifyPosition(position).subscribe();
    service.deletePosition(7).subscribe();

    expect(http.match(API + '7').map((r) => r.request.method)).toEqual(['GET', 'DELETE']);

    const create = http.expectOne(API);
    expect(create.request.method).toBe('POST');
    expect(create.request.body).toEqual(position);

    const modify = http.expectOne(API + '1');
    expect(modify.request.method).toBe('PUT');
    expect(modify.request.body).toEqual(position);
  });
});
