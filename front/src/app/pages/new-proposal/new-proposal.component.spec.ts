import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpTestingController } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { provideTestDefaults } from '../../../testing/test-providers';
import { makeCandidate, makePosition } from '../../../testing/fixtures';
import { environment } from '../../../environments/environment';
import { Candidate } from '../../utils/types';
import { CandidateService } from '../../services/candidate.service';
import { ProposalService } from '../../services/proposal.service';
import { ToastService } from '../../services/toast.service';

import { NewProposalComponent } from './new-proposal.component';

describe('NewProposalComponent', () => {
  let component: NewProposalComponent;
  let fixture: ComponentFixture<NewProposalComponent>;
  let proposalService: ProposalService;
  let candidateService: CandidateService;
  let toast: ToastService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NewProposalComponent],
      providers: provideTestDefaults(),
    }).compileComponents();

    proposalService = TestBed.inject(ProposalService);
    candidateService = TestBed.inject(CandidateService);
    toast = TestBed.inject(ToastService);
    fixture = TestBed.createComponent(NewProposalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('derives the salary lists and SCR from the latest calculation', () => {
    const monthly = new Map([['Base Salary', 1000]]);
    const yearly = new Map([['Base Salary', 14000]]);
    spyOn(proposalService, 'salaryCalculations').and.returnValue(
      of(
        new Map([
          ['monthly', monthly],
          ['yearly', yearly],
        ]),
      ),
    );
    spyOn(proposalService, 'getScrValue').and.returnValue(3);

    component.calculateAll();

    expect(component.monthlyList()).toEqual([['Base Salary', 1000]]);
    expect(component.yearlyList()).toEqual([['Base Salary', 14000]]);
    expect(component.scrValue()).toBe(3);
  });

  it('tracks the candidate picked in the autocomplete', () => {
    spyOn(proposalService, 'salaryCalculations').and.returnValue(of(new Map()));
    const candidate = makeCandidate();

    component.onSelectedCandidate({ value: candidate } as never);

    expect(component.selectedCandidate()).toEqual(candidate);
  });

  it('suggests every candidate of the chosen position, not only the first server page', () => {
    const filtered = spyOn(candidateService, 'getFilteredCandidates');
    const byId = new Map<number, Candidate>([
      [21, makeCandidate({ id: 21, name: 'Marta', surname: 'Gomes' })],
      [22, makeCandidate({ id: 22, name: 'Rui', surname: 'Costa' })],
    ]);
    const fetch = spyOn(candidateService, 'getCandidateById').and.callFake((id: number) =>
      of(byId.get(id) as Candidate),
    );
    component.selectedPosition = makePosition({ id: 5, candidates: [21, 22] });

    component.searchCandidates({ query: '' });
    expect(component.filteredCandidates().map((candidate) => candidate.id)).toEqual([21, 22]);

    component.searchCandidates({ query: 'COST' });
    expect(component.filteredCandidates().map((candidate) => candidate.id)).toEqual([22]);
    expect(filtered).not.toHaveBeenCalled();
    expect(fetch).toHaveBeenCalledTimes(2);
  });

  it("fetches a position's candidates once and again only when the position changes", () => {
    const http = TestBed.inject(HttpTestingController);
    const url = (id: number) => environment.API_ENDPOINT + 'candidates/' + id;
    component.selectedPosition = makePosition({ id: 5, candidates: [21, 22] });

    component.searchCandidates({ query: 'm' });
    component.searchCandidates({ query: 'ma' });
    http.expectOne(url(21)).flush(makeCandidate({ id: 21, name: 'Marta' }));
    http.expectOne(url(22)).flush('gone', { status: 404, statusText: 'Not Found' });
    expect(component.filteredCandidates().map((candidate) => candidate.id)).toEqual([21]);

    component.searchCandidates({ query: 'mar' });
    http.expectNone(url(21));
    expect(component.filteredCandidates().map((candidate) => candidate.id)).toEqual([21]);

    component.selectedPosition = makePosition({ id: 6, candidates: [30] });
    component.searchCandidates({ query: '' });
    http.expectOne(url(30)).flush(makeCandidate({ id: 30, name: 'Rui' }));
    expect(component.filteredCandidates().map((candidate) => candidate.id)).toEqual([30]);
  });

  it('suggests no candidates for a position without candidates', () => {
    const byId = spyOn(candidateService, 'getCandidateById');
    component.selectedPosition = makePosition({ candidates: [] });

    component.searchCandidates({ query: 'a' });

    expect(component.filteredCandidates()).toEqual([]);
    expect(byId).not.toHaveBeenCalled();
  });

  it("defaults the position to the picked candidate's position", () => {
    spyOn(proposalService, 'salaryCalculations').and.returnValue(of(new Map()));
    const listed = makePosition({ id: 3, title: 'Frontend' });
    component.positions.set([makePosition(), listed]);

    component.onSelectedCandidate({
      value: makeCandidate({ position: makePosition({ id: 3, candidates: undefined }) }),
    } as never);

    expect(component.selectedPosition).toBe(listed);
  });

  it('keeps a position the user already chose when picking a candidate', () => {
    spyOn(proposalService, 'salaryCalculations').and.returnValue(of(new Map()));
    const chosen = makePosition({ id: 1 });
    component.selectedPosition = chosen;

    component.onSelectedCandidate({
      value: makeCandidate({ position: makePosition({ id: 3 }) }),
    } as never);

    expect(component.selectedPosition).toBe(chosen);
  });

  it("submits a proposal started from the Candidates page with the candidate's position", () => {
    candidateService.changeCandidate(makeCandidate({ position: makePosition({ id: 3 }) }));
    const preselectedFixture = TestBed.createComponent(NewProposalComponent);
    preselectedFixture.detectChanges();
    const preselected = preselectedFixture.componentInstance;
    const http = TestBed.inject(HttpTestingController);
    http
      .match(environment.API_ENDPOINT + 'positions/')
      .forEach((req) => req.flush([makePosition(), makePosition({ id: 3, title: 'Frontend' })]));

    expect(preselected.selectedPosition?.title).toBe('Frontend');

    preselected.onSubmit();
    const post = http.expectOne(
      (req) => req.method === 'POST' && req.url === environment.API_ENDPOINT + 'proposals/',
    );
    expect(post.request.body.position.id).toBe(3);
  });

  it('clears the candidate and records the chosen position', () => {
    component.selectedPosition = {
      id: 1,
      title: 'Backend',
      description: '',
      njoyn: '',
      knowMore: '',
    };

    component.onSelectedPosition();

    expect(component.selectedCandidate()).toBeNull();
    expect(component.proposalForm.position).toEqual(component.selectedPosition);
  });

  it('reports a failed creation with a specific toast', () => {
    const error = spyOn(toast, 'error');

    component.onSubmit();
    TestBed.inject(HttpTestingController)
      .expectOne(
        (req) => req.method === 'POST' && req.url === environment.API_ENDPOINT + 'proposals/',
      )
      .flush('nope', { status: 400, statusText: 'Bad Request' });

    expect(error).toHaveBeenCalledOnceWith(
      "The proposal couldn't be created, please check the form for errors or missing fields!",
    );
  });
});
