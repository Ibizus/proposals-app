import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpTestingController } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { provideTestDefaults } from '../../../testing/test-providers';
import { makeCandidate, makePosition } from '../../../testing/fixtures';
import { environment } from '../../../environments/environment';
import { CandidateService } from '../../services/candidate.service';

import { CandidatesComponent } from './candidates.component';

describe('CandidatesComponent', () => {
  let component: CandidatesComponent;
  let fixture: ComponentFixture<CandidatesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CandidatesComponent],
      providers: provideTestDefaults(),
    }).compileComponents();

    fixture = TestBed.createComponent(CandidatesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('reloads the current page after a candidate is modified', () => {
    const http = TestBed.inject(HttpTestingController);
    const page = environment.API_ENDPOINT + 'candidates/?page=0&size=10';
    const before = makeCandidate({ id: 4, position: null });
    http.expectOne(page).flush({ candidates: [before], currentPage: 0, totalItems: 1 });
    spyOn(TestBed.inject(CandidateService), 'modifyCandidate').and.returnValue(of({}));

    component.goToModifyCandidate(before);
    component.candidate.position = makePosition({ id: 2, title: 'Frontend' });
    component.onModifyCandidate();
    http.expectOne(page).flush({
      candidates: [{ ...before, position: makePosition({ id: 2, title: 'Frontend' }) }],
      currentPage: 0,
      totalItems: 1,
    });

    expect(component.candidates()[0].position?.title).toBe('Frontend');
  });

  it('reloads the filtered first page after editing from a fresh search', () => {
    const http = TestBed.inject(HttpTestingController);
    const api = environment.API_ENDPOINT + 'candidates/';
    http
      .expectOne(api + '?page=0&size=10')
      .flush({ candidates: [], currentPage: 0, totalItems: 40 });
    component.paginate({ page: 3, rows: 10 });
    http
      .expectOne(api + '?page=3&size=10')
      .flush({ candidates: [], currentPage: 3, totalItems: 40 });
    spyOn(TestBed.inject(CandidateService), 'modifyCandidate').and.returnValue(of({}));

    component.onInputChange({ target: { value: 'ana' } } as unknown as Event);
    http
      .expectOne(api + '?page=0&size=10&filter=ana')
      .flush({ candidates: [makeCandidate()], currentPage: 0, totalItems: 1 });
    component.goToModifyCandidate(makeCandidate());
    component.onModifyCandidate();

    http.expectOne(api + '?page=0&size=10&filter=ana');
  });
});
