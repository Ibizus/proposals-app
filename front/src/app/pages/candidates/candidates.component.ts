import { Component, OnInit, ViewChild } from '@angular/core';
import { TableModule, Table } from 'primeng/table';
import { PaginatorModule } from 'primeng/paginator';
import { Candidate, Proposal } from '../../utils/types';
import { MessageService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ToastModule } from 'primeng/toast';
import { TooltipModule } from 'primeng/tooltip';
import { NewCandidateButtonComponent } from '../../components/new-candidate-button/new-candidate-button.component';
import { CandidateService } from '../../services/candidate.service';
import { Dialog, DialogModule } from 'primeng/dialog';
import {
  TranslateModule,
  TranslateService,
  TranslateStore,
} from '@ngx-translate/core';
import { ToastService } from '../../services/toast.service';
import { CommonModule } from '@angular/common';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { forkJoin } from 'rxjs';
import { sleep } from '../../utils/functions';

@Component({
  selector: 'app-candidates',
  standalone: true,
  imports: [
    TableModule,
    ButtonModule,
    DialogModule,
    ToastModule,
    TooltipModule,
    NewCandidateButtonComponent,
    PaginatorModule,
    IconFieldModule,
    FormsModule,
    InputIconModule,
    CommonModule,
    TranslateModule,
    IconFieldModule,
    InputIconModule,
  ],
  providers: [MessageService, TranslateService, TranslateStore, ToastService],
  templateUrl: './candidates.component.html',
  styleUrl: './candidates.component.scss',
})
export class CandidatesComponent implements OnInit {
  candidates: Candidate[] = [];
  candidate!: Candidate;
  modifyCandidateString: string = this.t.instant('MODIFY CANDIDATE');
  isLoading: boolean = true;

  visibleCandidateModal: boolean = false;
  visibleDelete: boolean = false;
  visibleCandidateDetails: boolean = false;
  candidateId: number = -1;

  // Names of the colums to be displayed on the Candidates table
  displayedColumns: string[] = [
    'id',
    'name',
    'surname',
    'phone',
    'email',
    'actions',
  ];
  selectedRow: any;
  selectedCandidate!: Candidate | null;
  // Pagination variables with default values:
  first: number = 0;
  rows: number = 10;
  currentPage: number = 20;
  demandedPage: number = 0;
  totalPages: number = 20;
  totalRecords: number = 200;

  @ViewChild('dt2') dataTable!: Table;
  filter: string = '';
  errorMessage: boolean = false;
  maritalStatusOptions: any[] | undefined;
  newCandidateString: string | undefined;
  proposals: Proposal[] = [];

  constructor(
    private router: Router,
    private candidateService: CandidateService,
    private toastService: ToastService,
    private t: TranslateService
  ) {
    // This is how you get translated strings consistantly
    this.t.use(localStorage.getItem('lang') || 'en');
    this.t.get('NEW CANDIDATE').subscribe((newCandidateString) => {
      this.newCandidateString = newCandidateString;
    });

    const maritalStatusSingle$ = this.t.get('SINGLE');
    const maritalStatusMarriedOne$ = this.t.get('MARRIED, ONE INCOME');
    const maritalStatusMarriedTwo$ = this.t.get('MARRIED, TWO INCOME');

    forkJoin([
      maritalStatusSingle$,
      maritalStatusMarriedOne$,
      maritalStatusMarriedTwo$,
    ]).subscribe(
      ([
        maritalStatusSingle,
        maritalStatusMarriedOne,
        maritalStatusMarriedTwo,
      ]) => {
        this.maritalStatusOptions = [
          { label: maritalStatusSingle, value: 0 },
          { label: maritalStatusMarriedOne, value: 1 },
          { label: maritalStatusMarriedTwo, value: 2 },
        ];
      }
    );
  }

  ngOnInit() {
    // Fetch paginated canidates
    this.getCandidates(this.demandedPage, this.rows, '');
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

  paginate(event: any) {
    this.first = event.page * event.rows;
    this.rows = event.rows;
    this.demandedPage = event.page; //Index of the new page
    this.isLoading = true;
    this.getCandidates(this.demandedPage, this.rows, this.filter);
  }

  // Search bar event
  onInputChange(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value;
    this.filter = filterValue;
    this.getCandidates(0, this.rows, this.filter);
  }

  goToCandidateDetail(candidate: Candidate): void {
    // Show toast
    this.toastService.success(
      'Going to candidate: ' + candidate.name + ' ' + candidate.surname
    );
    // Show modal with Candidate details
    this.candidateService.getCandidateById(candidate.id).subscribe({
      next: (candidate) => {
        this.showCandidateModal(candidate);
      },
      error: (error) => {
        this.toastService.error('Error fetching candidate details');
      },
    });
  }

  goToModifyCandidate(candidate: any) {
    this.visibleCandidateModal = true;
    this.candidate = candidate;
  }

  getCandidates(page: number, size: number, filter: string) {
    this.candidateService.getCandidates(page, size, filter).subscribe({
      next: (data) => {
        this.candidates = data.candidates;
        this.first = page * size + 1; // Calculate index of first item shown
        this.rows = size; // Update size input from the request
        this.currentPage = data.currentPage + 1; // In back first page is index 0
        this.totalRecords = data.totalItems;
        this.isLoading = false;
      },
      error: () => {},
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
      this.candidateService.deleteCandidate(this.candidateId).subscribe({
        next: (candidate) => {
          this.ngOnInit();
          sleep(250).then(() => {
            this.toastService.success('Candidate deleted succesfully');
          });
        },
        error: (error) => {
          this.toastService.error(error);
          // console.error(
          //   `Error deleting candidate with ID ${candidateId}:`,
          //   error
          // );
        },
      });
    } else {
      this.toastService.error('candidateId is not set');
    }

    this.candidateId = -1;
    this.visibleDelete = false;
  }

  newProposalCandidate(id: number) {
    this.candidateService.newProposalCandidate(id).subscribe({
      next: (candidate) => {
        this.candidateService.changeCandidate(candidate);
        this.router.navigate(['/new-proposal']);
      },
      error: (error) => {
        this.toastService.error(error);
      },
    });
  }

  goToCandidateProposals(id: number) {
    this.candidateService.newProposalCandidate(id).subscribe({
      next: (candidate) => {
        this.candidateService.changeCandidate(candidate);
        this.router.navigate(['/proposals']);
      },
      error: (error) => {
        this.toastService.error(error);
      },
    });
  }

  onSaveCandidate() {
    // Listen to the event emitted by NewCandidateButtonComponent
    this.ngOnInit();
  }

  onModifyCandidate() {
    this.candidateService.modifyCandidate(this.candidate).subscribe({
      next: (response) => {
        this.visibleCandidateModal = false;
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
      error: (error) => {},
    });
  }

  goToProposalDetail(proposal: any) {
    this.router.navigate(['proposal/', proposal.id]);
  }

  showDeleteDialog(rowData: any) {
    this.candidateId = rowData.id;
    this.visibleDelete = true;
  }

  closeDeleteDialog(dialog: Dialog, event: any) {
    this.candidateId = -1;
    dialog.close(event);
  }

  showCandidateModal(candidate: Candidate) {
    this.selectedCandidate = candidate;
    this.visibleCandidateDetails = true;
    this.getProposalsByCandidate(this.selectedCandidate);
  }

  closeCandidateModal() {
    this.visibleCandidateDetails = false;
    this.selectedCandidate = null;
  }

  resetForm() {
    throw new Error('Method not implemented.');
  }
}

