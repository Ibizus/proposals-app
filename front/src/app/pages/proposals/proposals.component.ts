import { Component, OnInit, ViewChild, signal } from '@angular/core';
import { Candidate, EmptyProposalForm, Proposal, Status } from '../../utils/types';
import { NewProposalButtonComponent } from '../../components/new-proposal-button/new-proposal-button.component';
import { Router } from '@angular/router';
import { TableModule, Table } from 'primeng/table';
import { PaginatorModule, PaginatorState } from 'primeng/paginator';
import { ButtonModule } from 'primeng/button';
import { ConfirmPopupModule } from 'primeng/confirmpopup';
import { TagModule } from 'primeng/tag';
import { TooltipModule } from 'primeng/tooltip';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { ProposalService } from '../../services/proposal.service';
import { CandidateService } from '../../services/candidate.service';
import { TranslateModule, TranslateService, TranslateStore } from '@ngx-translate/core';
import { ToastService } from '../../services/toast.service';
import { Dialog, DialogModule } from 'primeng/dialog';
import { sleep } from '../../utils/functions';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-proposals',
  standalone: true,
  imports: [
    NewProposalButtonComponent,
    TableModule,
    PaginatorModule,
    ButtonModule,
    ConfirmPopupModule,
    TagModule,
    IconFieldModule,
    InputIconModule,
    TooltipModule,
    TranslateModule,
    DialogModule,
  ],
  providers: [TranslateService, TranslateStore],
  templateUrl: './proposals.component.html',
  styleUrl: './proposals.component.scss',
})
export class ProposalsComponent implements OnInit {
  displayedColumns: string[] = ['role', 'dueDate', 'status', 'candidate', 'actions'];
  isLoading = signal(true);
  proposals = signal<Proposal[]>([]);
  private router: Router = new Router();
  selectedCandidate = signal<Candidate | null>(null);

  // Pagination variables with default values:
  first = 0;
  rows = 10;
  currentPage = 20;
  demandedPage = 0;
  totalPages = 20;
  totalRecords = 200;

  @ViewChild('dt2') dataTable!: Table;
  filter = '';
  newProposalButton: string = this.t.instant('NEW PROPOSAL');
  visible = false;
  proposalId = -1;

  routerSubscription!: Subscription;

  constructor(
    private proposalService: ProposalService,
    private candidateService: CandidateService,
    private toastService: ToastService,
    private t: TranslateService,
  ) {
    this.t.use(localStorage.getItem('lang') || 'en');

    // This function fetches a candidate from the service and subscribes to it
    // in order to filter the results
    this.candidateService.currentCandidate.subscribe((candidate) => {
      this.selectedCandidate.set(candidate);
      // console.log('Receiving candidate from other table', candidate);
    });
  }

  numberToStatus(p: Proposal): string {
    // Get all the statuses of the proposals and sort them by ascending date
    const statuses = p.statuses.sort(
      (a: Status, b: Status) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime(),
    );
    // Get the most recent status
    const lastStatus = statuses[statuses.length - 1];
    // Get status number of the most recent status
    const status = lastStatus.status;

    let res = '';
    switch (status) {
      case 0:
        res = this.t.instant('DRAFT');
        break;
      case 1:
        res = this.t.instant('CONFIRMED');
        break;
      case 2:
        res = this.t.instant('SENT');
        break;
      case 3:
        res = this.t.instant('DECLINED');
        break;
      case 4:
        res = this.t.instant('CANCELLED');
        break;
      case 5:
        res = this.t.instant('APPROVED');
        break;
      default:
        res = '';
    }
    return res;
  }

  ngOnInit() {
    if (this.selectedCandidate()) {
      this.getProposals(this.demandedPage, this.rows, this.selectedCandidate()?.email ?? '');
      this.candidateService.changeCandidate(null);
    } else {
      this.getProposals(this.demandedPage, this.rows, '');
    }
  }

  paginate(event: PaginatorState) {
    this.first = (event.page ?? 0) * (event.rows ?? this.rows);
    this.rows = event.rows ?? this.rows;
    this.demandedPage = event.page ?? 0; //Index of the new page
    this.isLoading.set(true);
    this.getProposals(this.demandedPage, this.rows, this.filter);
  }

  newProposal() {
    this.proposalService.changeProposal({});
    this.router.navigate(['/new-proposal']);
  }

  // Search bar event
  onInputChange(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value;
    this.filter = filterValue;
    this.getProposals(0, this.rows, this.filter);
  }

  getProposals(page: number, size: number, filter: string) {
    this.proposalService.getProposals(page, size, filter).subscribe({
      next: (data) => {
        this.proposals.set(data.proposals);
        this.first = page * size + 1; // Calculate index of first item shown
        this.rows = size; // Update size input from the request
        this.currentPage = data.currentPage + 1; // In back first pago is index 0
        this.totalRecords = data.totalItems;
        this.isLoading.set(false);
      },
    });
  }

  goToProposalDetail(proposalId: number): void {
    this.router.navigate(['/proposal', proposalId]);
  }

  getProposalById(proposalId: number) {
    this.proposalService.getProposal(proposalId).subscribe({
      next: (proposal) => {
        console.log(proposal);
        return proposal;
      },
      error: () => {
        this.toastService.warn('Warning');
      },
    });
  }

  goToModifyProposal(proposalId: number) {
    // Get the proposal of Id === proposalId
    this.proposalService.getProposal(proposalId).subscribe({
      next: (proposal) => {
        // If it finds the proposal, change it in the service and navigate to the new proposal page
        this.proposalService.changeProposal(proposal);
        this.router.navigate(['/new-proposal']);
      },
      error: () => undefined,
    });
  }

  deleteProposal(): void {
    if (this.proposalId !== -1) {
      this.proposalService.deleteProposal(this.proposalId).subscribe({
        next: () => {
          this.ngOnInit();
          sleep(200).then(() => {
            this.toastService.success('The proposal has been deleted successfully');
          });
        },
        error: () => undefined,
      });
    } else {
      this.toastService.error('proposalId is not set');
    }

    this.proposalId = -1;
    this.visible = false;
  }

  getSeverity(status: string) {
    switch (status) {
      case this.t.instant('DRAFT'):
        return 'success';
      case this.t.instant('CONFIRMED'):
        return 'info';
      case this.t.instant('SENT'):
        return 'warning';
      case this.t.instant('DECLINED'):
        return 'warning';
      case this.t.instant('CANCELLED'):
        return 'danger';
      case this.t.instant('APPROVED'):
        return 'success';
      default:
        return '';
    }
  }

  closeDialog(dialog: Dialog, event: Event) {
    this.proposalId = -1;
    dialog.close(event);
  }

  showDialog(rowData: Proposal) {
    this.proposalId = rowData.id;
    this.visible = true;
  }

  duplicateProposal(existingProposal: Proposal) {
    const newProposal: Proposal = EmptyProposalForm() as unknown as Proposal;

    Object.assign(newProposal, {
      id: 0,
      role: existingProposal.role,
      description: existingProposal.description,
      njoyn: newProposal.njoyn,
      starDate: new Date(existingProposal.startDate),
      dueDate: new Date(existingProposal.dueDate),
      contractLevel: existingProposal.contractLevel,
      knowMore: existingProposal.knowMore,
      indefinite: existingProposal.indefinite,
      office: {
        ...existingProposal.office,
        proposals: [],
      },
      salary: {
        id: 0,
        base: existingProposal.salary.base,
        extras: existingProposal.salary.extras,
      },
      candidate: {
        ...existingProposal.candidate,
      },
      statuses: [
        {
          id: 0,
          status: 0,
          message: '',
          createdAt: new Date().getTime,
          createdBy: '',
          modifiedAt: new Date().getTime,
          modifiedBy: 'string',
        },
      ],
      createdAt: new Date().getTime,
      createdBy: '',
      modifiedAt: new Date().getTime,
      modifiedBy: '',
    });

    this.proposalService.createProposal(newProposal).subscribe({
      next: () => {
        this.ngOnInit();
        this.toastService.success('Extra duplicated correctly');
      },
      error: () => undefined,
    });
  }
}
