import { Component, OnInit, ViewChild, signal } from '@angular/core';
import { TableModule, Table } from 'primeng/table';
import { PaginatorModule, PaginatorState } from 'primeng/paginator';
import { Candidate, Proposal } from '../../utils/types';
import { ButtonModule } from 'primeng/button';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { TooltipModule } from 'primeng/tooltip';
import { NewCandidateButtonComponent } from '../../components/new-candidate-button/new-candidate-button.component';
import { CandidateService } from '../../services/candidate.service';
import { Dialog, DialogModule } from 'primeng/dialog';
import { TranslateModule, TranslateService, TranslateStore } from '@ngx-translate/core';
import { ToastService } from '../../services/toast.service';
import { CommonModule } from '@angular/common';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { forkJoin } from 'rxjs';
import { sleep } from '../../utils/functions';
import { callerHandlesErrors } from '../../services/error-toast.interceptor';

@Component({
  selector: 'app-candidates',
  standalone: true,
  imports: [
    TableModule,
    ButtonModule,
    DialogModule,
    TooltipModule,
    NewCandidateButtonComponent,
    PaginatorModule,
    IconFieldModule,
    FormsModule,
    InputIconModule,
    CommonModule,
    TranslateModule,
  ],
  providers: [TranslateService, TranslateStore],
  templateUrl: './candidates.component.html',
  styleUrl: './candidates.component.scss',
})
export class CandidatesComponent implements OnInit {
  candidates = signal<Candidate[]>([]);
  candidate!: Candidate;
  modifyCandidateString: string = this.t.instant('MODIFY CANDIDATE');
  isLoading = signal(true);

  visibleCandidateModal = signal(false);
  visibleDelete = signal(false);
  visibleCandidateDetails = signal(false);
  candidateId = -1;

  // Names of the colums to be displayed on the Candidates table
  displayedColumns: string[] = ['id', 'name', 'surname', 'phone', 'email', 'actions'];
  selectedCandidate = signal<Candidate | null>(null);
  // Pagination variables with default values:
  first = signal(0);
  rows = signal(10);
  currentPage = 20;
  demandedPage = 0;
  totalPages = 20;
  totalRecords = signal(200);

  @ViewChild('dt2') dataTable!: Table;
  filter = '';
  errorMessage = false;
  maritalStatusOptions: { label: string; value: number }[] | undefined;
  newCandidateString: string | undefined;
  proposals: Proposal[] = [];

  constructor(
    private router: Router,
    private candidateService: CandidateService,
    private toastService: ToastService,
    private t: TranslateService,
  ) {
    // This is how you get translated strings consistantly
    this.t.use(localStorage.getItem('lang') || 'en');
    this.t.get('NEW CANDIDATE').subscribe((newCandidateString) => {
      this.newCandidateString = newCandidateString;
    });

    const maritalStatusSingle$ = this.t.get('SINGLE');
    const maritalStatusMarriedOne$ = this.t.get('MARRIED, ONE INCOME');
    const maritalStatusMarriedTwo$ = this.t.get('MARRIED, TWO INCOME');

    forkJoin([maritalStatusSingle$, maritalStatusMarriedOne$, maritalStatusMarriedTwo$]).subscribe(
      ([maritalStatusSingle, maritalStatusMarriedOne, maritalStatusMarriedTwo]) => {
        this.maritalStatusOptions = [
          { label: maritalStatusSingle, value: 0 },
          { label: maritalStatusMarriedOne, value: 1 },
          { label: maritalStatusMarriedTwo, value: 2 },
        ];
      },
    );
  }

  ngOnInit() {
    // Fetch paginated canidates
    this.getCandidates(this.demandedPage, this.rows(), '');
    // Reset candidate
    this.candidate = {
      '@id': 0,
      id: 0,
      name: '',
      surname: '',
      idNumber: '',
      phone: '',
      email: '',
      workerId: '',
      maritalStatus: 0,
      dependentsNumber: 0,
      nJoynSet: [''],
      proposals: [0],
    };
  }

  paginate(event: PaginatorState) {
    this.first.set((event.page ?? 0) * (event.rows ?? this.rows()));
    this.rows.set(event.rows ?? this.rows());
    this.demandedPage = event.page ?? 0; //Index of the new page
    this.isLoading.set(true);
    this.getCandidates(this.demandedPage, this.rows(), this.filter);
  }

  // Search bar event
  onInputChange(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value;
    this.filter = filterValue;
    this.getCandidates(0, this.rows(), this.filter);
  }

  goToCandidateDetail(candidate: Candidate): void {
    // Show toast
    this.toastService.success('Going to candidate: ' + candidate.name + ' ' + candidate.surname);
    // Show modal with Candidate details
    this.candidateService.getCandidateById(candidate.id, callerHandlesErrors()).subscribe({
      next: (candidate) => {
        this.showCandidateModal(candidate);
      },
      error: () => {
        this.toastService.error('Error fetching candidate details');
      },
    });
  }

  goToModifyCandidate(candidate: Candidate) {
    this.visibleCandidateModal.set(true);
    this.candidate = candidate;
  }

  getCandidates(page: number, size: number, filter: string) {
    this.candidateService.getCandidates(page, size, filter).subscribe({
      next: (data) => {
        this.candidates.set(data.candidates);
        this.first.set(page * size + 1); // Calculate index of first item shown
        this.rows.set(size); // Update size input from the request
        this.currentPage = data.currentPage + 1; // In back first page is index 0
        this.totalRecords.set(data.totalItems);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      },
    });
  }

  getProposalsByCandidate(candidate: Candidate) {
    console.log('Fetching proposals for candidate:', candidate.id);
    this.candidateService.getProposalsByCandidateId(candidate.id).subscribe({
      next: (response) => {
        this.proposals = response;
      },
      error: (error) => {
        console.error('Failed to fetch proposals:', error);
      },
    });
  }

  deleteCandidate(): void {
    if (this.candidateId !== -1) {
      this.candidateService.deleteCandidate(this.candidateId, callerHandlesErrors()).subscribe({
        next: () => {
          this.ngOnInit();
          sleep(250).then(() => {
            this.toastService.success('Candidate deleted succesfully');
          });
        },
        error: () => {
          this.toastService.error('The candidate could not be deleted');
        },
      });
    } else {
      this.toastService.error('candidateId is not set');
    }

    this.candidateId = -1;
    this.visibleDelete.set(false);
  }

  newProposalCandidate(id: number) {
    this.candidateService.newProposalCandidate(id, callerHandlesErrors()).subscribe({
      next: (candidate) => {
        this.candidateService.changeCandidate(candidate);
        this.router.navigate(['/new-proposal']);
      },
      error: () => {
        this.toastService.error('The candidate could not be loaded');
      },
    });
  }

  goToCandidateProposals(id: number) {
    this.candidateService.newProposalCandidate(id, callerHandlesErrors()).subscribe({
      next: (candidate) => {
        this.candidateService.changeCandidate(candidate);
        this.router.navigate(['/proposals']);
      },
      error: () => {
        this.toastService.error('The candidate could not be loaded');
      },
    });
  }

  onSaveCandidate() {
    // Listen to the event emitted by NewCandidateButtonComponent
    this.ngOnInit();
  }

  onModifyCandidate() {
    this.candidateService.modifyCandidate(this.candidate).subscribe({
      next: () => {
        this.visibleCandidateModal.set(false);
        this.candidate = {
          '@id': 0,
          id: 0,
          name: '',
          surname: '',
          idNumber: '',
          phone: '',
          email: '',
          workerId: '',
          maritalStatus: 0,
          dependentsNumber: 0,
          nJoynSet: [''],
          proposals: [0],
        };
      },
      error: () => undefined,
    });
  }

  goToProposalDetail(proposal: Proposal) {
    this.router.navigate(['proposal/', proposal.id]);
  }

  showDeleteDialog(rowData: Candidate) {
    this.candidateId = rowData.id;
    this.visibleDelete.set(true);
  }

  closeDeleteDialog(dialog: Dialog, event: Event) {
    this.candidateId = -1;
    dialog.close(event);
  }

  showCandidateModal(candidate: Candidate) {
    this.selectedCandidate.set(candidate);
    this.visibleCandidateDetails.set(true);
    this.getProposalsByCandidate(candidate);
  }

  closeCandidateModal() {
    this.visibleCandidateDetails.set(false);
    this.selectedCandidate.set(null);
  }

  resetForm() {
    throw new Error('Method not implemented.');
  }
}
