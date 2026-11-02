import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ProposalService } from '../../services/proposal.service';
import { environment } from '../../../environments/environment';
import { TaxesService } from '../../services/taxes.service';
import { Dialog, DialogModule } from 'primeng/dialog';
import { Proposal, Status, Taxes } from '../../utils/types';
import { CandidateCardComponent } from '../../components/candidate-card/candidate-card.component';
import { CurrencyPipe } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { TooltipModule } from 'primeng/tooltip';
import { StepperModule } from 'primeng/stepper';
import { NewMailComponent } from '../../components/new-mail/new-mail.component';
import { MailService } from '../../services/mail.service';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ToastService } from '../../services/toast.service';
import { finalize } from 'rxjs';
@Component({
  selector: 'app-detailed-proposal',
  standalone: true,
  imports: [
    CandidateCardComponent,
    CurrencyPipe,
    TranslateModule,
    NewMailComponent,
    DialogModule,
    ButtonModule,
    TooltipModule,
    StepperModule,
  ],
  templateUrl: './detailed-proposal.component.html',
  styleUrl: './detailed-proposal.component.scss',
})
export class DetailedProposalComponent {
  proposalId: number;
  proposal!: Proposal;
  visible = false;
  sending = false;
  monthlySalaryCalculations = new Map<string, number>();
  yaearlySalaryCalculations = new Map<string, number>();
  calculations = new Map<string, Map<string, number>>();
  monthlyList!: [string, number][];
  yearlyList!: [string, number][];
  spp!: number;
  iht!: number;
  scr!: number;
  taxes!: Taxes[];

  constructor(
    private route: ActivatedRoute,
    private proposalService: ProposalService,
    private taxesService: TaxesService,
    private mailService: MailService,
    private toastService: ToastService,
    private t: TranslateService,
  ) {
    this.t.use(localStorage.getItem('lang') || 'en');
    this.proposalId = 0;
    this.route.params.subscribe((params: Record<string, string | number>) => {
      this.getProposal(+params['id']);
    });

    this.getParameters();
  }

  getProposal(id: number) {
    this.proposalService.getProposal(id).subscribe({
      next: (data) => {
        this.proposalId = id;
        this.proposal = data;
        const statuses = this.proposal.statuses.sort(
          (a: Status, b: Status) =>
            new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime(),
        );
        this.proposal.statuses = statuses;
        this.getTaxes();
      },
    });
  }

  getTaxes() {
    this.taxesService.getTaxes().subscribe({
      next: (data) => {
        this.taxes = data;
        this.calculateAll(this.proposal);
      },
      error: (error) => {
        console.error('Error while fetching  extras:', error);
      },
    });
  }

  getParameters() {
    this.proposalService.getParameters().subscribe({
      next: (parameters) => {
        this.iht = parameters.get('iht') ?? 0;
        this.spp = parameters.get('spp') ?? 0;
        this.scr = parameters.get('scr') ?? 0;
      },
      error: (error) => {
        console.error('Error while fetching parameters: ', error);
      },
    });
  }

  numberToStatus(n: number): string {
    let res = '';
    switch (n) {
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

  notifyCandidate(proposalId: number) {
    if (this.sending) {
      return;
    }
    this.sending = true;
    this.proposalService.getProposal(proposalId).subscribe({
      next: (proposal) => {
        this.mailService
          .notifyByEmail(proposal)
          .pipe(finalize(() => (this.sending = false)))
          .subscribe({
            next: () => {
              this.visible = false;
              this.toastService.success(this.t.instant('EMAIL SENT SUCCESS'));
            },
            error: () => {
              this.visible = false;
              this.toastService.error(this.t.instant('EMAIL SEND ERROR MESSAGE'));
            },
          });
      },
      error: () => {
        this.sending = false;
        this.visible = false;
        this.toastService.error(this.t.instant('EMAIL SEND ERROR MESSAGE'));
      },
    });
  }

  // METHOD RENDERED IN FRONTEND: (need to be refactorized or sent to Service)
  async downloadPdf(): Promise<void> {
    const lang = localStorage.getItem('lang') || 'en';
    fetch(environment.API_ENDPOINT + 'pdf/' + this.proposal.id + '?lang=' + lang)
      .then((response) => {
        if (!response.ok) {
          throw new Error('PDF download failed with status ' + response.status);
        }
        return response.blob();
      })
      .then((blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', 'proposal.pdf');
        document.body.appendChild(link);
        link.click();
      })
      .catch((error) => {
        console.error('Error:', error);
      });
  }

  calculateAll(proposal: Proposal) {
    this.proposalService.salaryCalculations(proposal).subscribe((result) => {
      this.calculations = result;
      this.monthlyList = Array.from(this.calculations.get('monthly') ?? new Map());
      this.yearlyList = Array.from(this.calculations.get('yearly') ?? new Map());
    });
  }

  showDialog(): void {
    this.visible = true;
    setTimeout(() => {
      const element = document.getElementById('focusableDiv');
      if (element) {
        element.focus();
      }
    }, 0);
  }

  closeDialog(dialog: Dialog, event: Event) {
    dialog.close(event);
  }
}
