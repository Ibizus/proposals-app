import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { of, throwError } from 'rxjs';
import { provideTestDefaults } from '../../../testing/test-providers';
import { makeCandidate } from '../../../testing/fixtures';
import { ProposalService } from '../../services/proposal.service';
import { ToastService } from '../../services/toast.service';

import { NewProposalComponent } from './new-proposal.component';

describe('NewProposalComponent', () => {
  let component: NewProposalComponent;
  let fixture: ComponentFixture<NewProposalComponent>;
  let proposalService: ProposalService;
  let toast: ToastService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NewProposalComponent],
      providers: provideTestDefaults(),
    }).compileComponents();

    proposalService = TestBed.inject(ProposalService);
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

  it('reports a failed creation with a specific toast', () => {
    spyOn(proposalService, 'createProposal').and.returnValue(
      throwError(() => new HttpErrorResponse({ status: 400 })),
    );
    const error = spyOn(toast, 'error');

    component.onSubmit();

    expect(error).toHaveBeenCalledOnceWith(
      "The proposal couldn't be created, please check the form for errors or missing fields!",
    );
  });
});
