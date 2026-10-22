import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpTestingController } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { provideTestDefaults } from '../../../testing/test-providers';
import { makeCandidate } from '../../../testing/fixtures';
import { environment } from '../../../environments/environment';
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

  it('filters candidate suggestions to the chosen position', () => {
    spyOn(candidateService, 'getFilteredCandidates').and.returnValue(
      of({
        candidates: [
          makeCandidate({
            id: 1,
            position: { id: 1, title: 'Backend', description: '', njoyn: '', knowMore: '' },
          }),
          makeCandidate({
            id: 2,
            position: { id: 2, title: 'Frontend', description: '', njoyn: '', knowMore: '' },
          }),
        ],
      }),
    );
    component.selectedPosition = {
      id: 1,
      title: 'Backend',
      description: '',
      njoyn: '',
      knowMore: '',
    };

    component.searchCandidates({ query: 'a' });

    expect(component.filteredCandidates().map((candidate) => candidate.id)).toEqual([1]);
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
