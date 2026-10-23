import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ProposalService } from '../../services/proposal.service';
import { environment } from '../../../environments/environment';
import { TaxesService } from '../../services/taxes.service';
import { Dialog, DialogModule } from 'primeng/dialog';
import { Proposal, Taxes } from '../../utils/types';
import { CandidateCardComponent } from '../../components/candidate-card/candidate-card.component';
import { CurrencyPipe } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import { TooltipModule } from 'primeng/tooltip';
import { StepperModule } from 'primeng/stepper';
import { NewMailComponent } from '../../components/new-mail/new-mail.component';
import { MailService } from '../../services/mail.service';
import {
  TranslateModule,
  TranslateService,
  TranslateStore,
} from '@ngx-translate/core';
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
  providers: [TranslateService, TranslateStore],
  templateUrl: './detailed-proposal.component.html',
  styleUrl: './detailed-proposal.component.scss',
})
export class DetailedProposalComponent {
  proposals!: Proposal[];
  proposalId: number;
  proposal!: Proposal;
  visible: boolean = false;
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
    private router: Router,
    private proposalService: ProposalService,
    private taxesService: TaxesService,
    private mailService: MailService,
    private t: TranslateService
  ) {
    this.t.use(localStorage.getItem('lang') || 'en');
    this.proposalId = 0;
    this.route.params.subscribe((params: { [x: string]: string | number }) => {
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
          (a: any, b: any) =>
            new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
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

  onClickDelete(): void {
    this.proposalService.deleteProposal(this.proposalId).subscribe({
      next: () => {
        // Remove the deleted proposal from the local array
        this.proposals.splice(this.proposalId, 1);
        // Update the proposals in localStorage
        localStorage.setItem('proposals', JSON.stringify(this.proposals));
        // Redirect to the proposals route
        this.router.navigate(['/proposals']);
      },
    });
  }

  // Listen to the event emitted by NewEmailComponent:
  onSendEmailClicked(data: {
    email: string;
    subject: string;
    message: string;
    proposalId: number;
  }) {
    // console.log(
    //   'Event received with all the information needed, proposalId: ' +
    //     data.proposalId +
    //     ' - ' +
    //     data.email +
    //     ' - ' +
    //     data.subject +
    //     ' - ' +
    //     data.message +
    //     ' - '
    // );

    this.mailService
      .newEmail(data.email, data.subject, data.message, data.proposalId)
      .subscribe({
        next: () => {
          console.log('Posting email to backend inside new-email event listener');
        },
        error: (error) => {
          console.log(error);
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
    this.proposalService.getProposal(proposalId).subscribe({
      next: (proposal) => {
        this.mailService.notifyByEmail(proposal).subscribe({
          next: () => {},
          error: (error) => {
            console.log(error);
          },
        });
      },
      error: (error) => {
        console.log('Error finding proposal: ', error);
      },
    });
  }

  // Creates the html template in backend:
  sendTemplate(proposalId: number) {
    this.proposalService.sendTemplate(proposalId).subscribe({
      next: () => {
        console.log(
          'Sending info to front Service in order send html template'
        );
        this.visible = false;
      },
      error: (error) => {
        console.error('Error fetching the "Send Template method":', error);
      },
    });
  }

  // METHOD RENDERED IN FRONTEND: (need to be refactorized or sent to Service)
  async downloadPdf(): Promise<void> {
    const lang = localStorage.getItem('lang') || 'en';
    fetch(environment.API_ENDPOINT + 'pdf/' + this.proposal.id + '?lang=' + lang)
      .then((response) => response.blob())
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
      this.monthlyList = Array.from(
        this.calculations.get('monthly') ?? new Map()
      );
      this.yearlyList = Array.from(
        this.calculations.get('yearly') ?? new Map()
      );
    });
  }

  showDialog(): any {
    this.visible = true;
    setTimeout(() => {
      const element = document.getElementById('focusableDiv');
      if (element) {
        element.focus();
      }
    }, 0);
  }

  closeDialog(dialog: Dialog, event: any) {
    dialog.close(event);
  }
}
