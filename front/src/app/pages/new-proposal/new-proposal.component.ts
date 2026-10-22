/* eslint-disable @typescript-eslint/no-explicit-any -- legacy proposal-form state is untyped */
import { Component, OnInit, OnDestroy, computed, signal } from '@angular/core';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { NavigationStart, Router } from '@angular/router';
import {
  Candidate,
  Extra,
  Office,
  Position,
  Taxes,
  Status,
  EmptyProposalForm,
} from '../../utils/types';
import { matCloseOutline } from '@ng-icons/material-icons/outline';
import { NgIconComponent, provideIcons } from '@ng-icons/core';
import { MultiSelectModule } from 'primeng/multiselect';
import { AutoCompleteModule, AutoCompleteSelectEvent } from 'primeng/autocomplete';
import { CalendarModule } from 'primeng/calendar';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { DropdownModule } from 'primeng/dropdown';
import { ToggleButtonModule } from 'primeng/togglebutton';
import { TableModule } from 'primeng/table';
import { forkJoin, Subscription } from 'rxjs';
import { CurrencyPipe } from '@angular/common';
import { CandidateCardComponent } from '../../components/candidate-card/candidate-card.component';
import { CandidateService } from '../../services/candidate.service';
import { PositionService } from '../../services/position.service';
import { ProposalService } from '../../services/proposal.service';
import { AccordionModule } from 'primeng/accordion';
import { TranslateModule, TranslateService, TranslateStore } from '@ngx-translate/core';
import { TaxesService } from '../../services/taxes.service';
import { OfficeService } from '../../services/office.service';
import { ExtrasService } from '../../services/extras.service';
import { ToastService } from '../../services/toast.service';
import { sleep } from '../../utils/functions';
import { callerHandlesErrors } from '../../services/error-toast.interceptor';
@Component({
  selector: 'app-new-proposal',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    NgIconComponent,
    MultiSelectModule,
    FormsModule,
    AutoCompleteModule,
    CalendarModule,
    InputTextModule,
    InputTextareaModule,
    DropdownModule,
    ToggleButtonModule,
    TableModule,
    CurrencyPipe,
    CandidateCardComponent,
    AccordionModule,
    TranslateModule,
  ],
  viewProviders: [provideIcons({ matCloseOutline })],
  providers: [TranslateService, TranslateStore],
  templateUrl: './new-proposal.component.html',
  styleUrl: './new-proposal.component.scss',
})
export class NewProposalComponent implements OnInit, OnDestroy {
  proposalForm: any = EmptyProposalForm();
  role: string | undefined;
  njoyn: string | undefined;
  date: Date | undefined;
  minDate: Date = new Date();
  offices: Office[] = [];
  extras = signal<Extra[]>([]);
  parameters: any[] = [];
  selectedExtras: any[] = [];
  searchTextOffices = '';
  searchTextCandidates = '';
  filteredOffices = signal<Office[]>([]);
  filteredCandidates = signal<Candidate[]>([]);
  positions = signal<Position[]>([]);
  selectedPosition: Position | null = null;
  candidate!: Candidate | null;
  selectedCandidate = signal<Candidate | null>(null);
  taxes!: Taxes[];
  contractLevels = ['L1', 'L2', 'L3', 'L4', 'L5', 'L6', 'L7'];
  yearlyGross!: number;
  monthlyNet = 0;
  yearlyNet = 0;
  monthlyCompanyCost = 0;
  yearlyCompanyCost = 0;
  // Salary calculations variables:
  iht!: number;
  spp!: number;
  scr!: number;
  ss!: number;
  irs!: number;
  monthlySalaryCalculations = signal(new Map<string, number>());
  yearlySalaryCalculations = signal(new Map<string, number>());
  monthlyList = computed(() => Array.from(this.monthlySalaryCalculations()));
  yearlyList = computed(() => Array.from(this.yearlySalaryCalculations()));
  temporalProposal!: any;
  candidateHeaderTemplate = '';
  proposalHeaderTemplate = '';
  salaryHeaderTemplate = '';
  candidateHeader = this.candidateHeaderTemplate;
  proposalHeader = this.proposalHeaderTemplate;
  salaryHeader = this.salaryHeaderTemplate;
  scrValue = signal<number | undefined>(undefined);

  subscription!: Subscription;

  contractTypeOnLabel = '';
  contractTypeOffLabel = '';

  routerSubscription!: Subscription;

  selected = false;
  statuses!: any[];
  latestStatus!: Status;
  latestStatusNumber!: number;
  latestStatusMessage!: string;

  userNotSelected!: boolean;

  constructor(
    private router: Router,
    private toastService: ToastService,
    private candidateService: CandidateService,
    private positionService: PositionService,
    private proposalService: ProposalService,
    private taxesService: TaxesService,
    private officeService: OfficeService,
    private extrasService: ExtrasService,
    private t: TranslateService,
  ) {
    // Get all translated strings
    this.t.use(localStorage.getItem('lang') || 'en');
    const candidateHeaderTemplate$ = this.t.get('CANDIDATE DETAILS');
    const proposalHeaderTemplate$ = this.t.get('PROPOSAL DETAILS');
    const salaryHeaderTemplate$ = this.t.get('SALARY DETAILS');
    const contractTypeOnLabel$ = this.t.get('INDEFINITE');
    const contractTypeOffLabel$ = this.t.get('FIXEDTERM');

    forkJoin([
      candidateHeaderTemplate$,
      proposalHeaderTemplate$,
      salaryHeaderTemplate$,
      contractTypeOnLabel$,
      contractTypeOffLabel$,
    ]).subscribe(
      ([
        candidateHeaderTemplate,
        proposalHeaderTemplate,
        salaryHeaderTemplate,
        contractTypeOnLabel,
        contractTypeOffLabel,
      ]) => {
        this.candidateHeaderTemplate = candidateHeaderTemplate;
        this.proposalHeaderTemplate = proposalHeaderTemplate;
        this.salaryHeaderTemplate = salaryHeaderTemplate;
        this.contractTypeOnLabel = contractTypeOnLabel;
        this.contractTypeOffLabel = contractTypeOffLabel;
        this.candidateHeader = candidateHeaderTemplate;
        this.proposalHeader = proposalHeaderTemplate;
        this.salaryHeader = salaryHeaderTemplate;
      },
    );

    // All the possible statuses
    this.statuses = [
      { label: 'Draft', value: 0 },
      { label: 'Confirmed', value: 1 },
      { label: 'Sent', value: 2 },
      { label: 'Declined', value: 3 },
      { label: 'Cancelled', value: 4 },
      { label: 'Approved', value: 5 },
    ];

    // Fetch every position for the position picker
    this.positionService.getAllPositions().subscribe({
      next: (positions) => {
        this.positions.set(positions);
        if (this.selectedPosition) {
          const match = positions.find((position) => position.id === this.selectedPosition?.id);
          if (match) {
            this.selectedPosition = match;
            this.proposalForm.position = match;
          }
        }
      },
      error: () => this.positions.set([]),
    });

    // Fetch extras into this.extras mapped by the method mapJsonToExtra
    this.extrasService.getExtras(0, 100).subscribe({
      next: (data) => {
        const extras: Extra[] = data.extras.map(this.mapJsonToExtra);

        // If it's a new proposal, add the default extras to it
        if (this.selectedExtras.length === 0) {
          extras.forEach((ex) => {
            if (ex.isDefault) {
              this.selectedExtras.push(ex);
            }
          });
        }

        // Find matching extras and update the selectedExtras.
        this.selectedExtras.forEach((ex) => {
          const matchingExtraIndex = extras.findIndex((extra) => extra.name === ex.name);
          if (matchingExtraIndex !== -1) {
            extras[matchingExtraIndex] = ex;
          }
        });
        this.extras.set(extras);
      },
      error: (error) => {
        console.error('Error while fetching extras:', error);
      },
    });

    // Fetch parameters
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

    // Fetch taxes
    this.taxesService.getTaxes().subscribe({
      next: (data: any) => {
        this.taxes = data;
        this.calculateAll();
      },
      error: (error: any) => {
        console.error('Error while fetching  extras:', error);
      },
    });

    // Get current candidate
    this.candidateService.currentCandidate.subscribe((candidate) => {
      console.log(candidate);
      this.selectedCandidate.set(candidate);
    });

    // Get current proposal from service and set it in temporalProposal
    this.proposalService.currentProposal.subscribe((proposal: any) => {
      if (Object.keys(proposal).length !== 0) {
        this.temporalProposal = proposal;
        const statuses = proposal.statuses.sort(
          (a: any, b: any) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime(),
        );
        this.latestStatus = statuses[statuses.length - 1];
        this.latestStatusNumber = this.latestStatus.status;
        this.latestStatusMessage = this.latestStatus.message;
      } else {
        this.temporalProposal = null;
        this.proposalForm = null;
      }
    });

    // TODO: The proposal Form needs to be filled properly before submitting
    // The proposal form should be filled with the current proposal from the service
    if (this.temporalProposal) {
      // This function sets the id to the field attribute extra.templateId and adds 3 more fields to be properly shown on the
      // dropdown menu of the multiselect component
      const mapInputToOutput = (input: any): any => {
        return {
          amount: input.amount,
          createdAt: null, // Set createdAt to null
          id: input.templateId,
          isIRSTaxable: input.isIRSTaxable,
          isPercentage: input.isPercentage,
          isSSTaxable: input.isSSTaxable,
          modifiedAt: null, // Set modifiedAt to null
          modifiedBy: null, // Set modifiedBy to null
          multiplyMonthly: input.multiplyMonthly,
          multiplyYearly: input.multiplyYearly,
          name: input.name,
        };
      };

      this.proposalForm = this.temporalProposal;
      this.proposalForm.startDate = new Date(this.temporalProposal.startDate);
      this.proposalForm.dueDate = new Date(this.temporalProposal.dueDate);

      this.selectedCandidate.set(this.temporalProposal.candidate);
      this.selectedPosition = this.temporalProposal.position ?? null;

      const extras: any[] = this.proposalForm.salary.extras.map((e: any) => mapInputToOutput(e));
      this.selectedExtras = extras;
    } else {
      this.proposalForm = EmptyProposalForm();
    }

    if (this.selectedCandidate()) {
      this.proposalForm.candidate = this.selectedCandidate();
    }
  }

  ngOnInit() {
    this.updateHeaders();
    this.routerSubscription = this.router.events.subscribe((event) => {
      if (event instanceof NavigationStart) {
        // Navigation started, reset proposalForm
        this.clearFormFields();
      }
    });
  }

  ngOnDestroy() {
    this.routerSubscription.unsubscribe();
  }

  clearFormFields() {
    this.candidateService.changeCandidate(null);
    this.selectedCandidate.set(null);
    this.selectedPosition = null;
    this.candidate = null;
    this.proposalForm = EmptyProposalForm();
  }

  updateHeaders() {
    const candidateName = this.proposalForm.candidate?.name ?? '';
    const proposalRole = this.proposalForm.role;

    this.candidateHeader = this.candidateHeaderTemplate + ': ' + candidateName;
    this.proposalHeader = this.proposalHeaderTemplate + ': ' + proposalRole;
    this.salaryHeader =
      this.salaryHeaderTemplate +
      ': ' +
      this.formatCurrency(this.yearlySalaryCalculations().get('Estimated Net Salary'));
  }

  mapJsonToExtra(json: any): Extra {
    return {
      id: json.id,
      name: json.name,
      amount: json.amount,
      irsTaxrate: json.irsTaxrate,
      ssTaxrate: json.ssTaxrate,
      isPercentage: json.isPercentage,
      isSSTaxable: json.isSSTaxable,
      isIRSTaxable: json.isIRSTaxable,
      isDefault: json.isDefault,
      addToSalary: json.addToSalary,
      multiplyYearly: json.multiplyYearly,
      multiplyMonthly: json.multiplyMonthly,
      createdAt: json.createdAt,
      createdBy: json.createdBy,
      modifiedAt: json.createdAt,
      modifiedBy: json.modifiedBy,
    };
  }

  formatDate(date: Date) {
    // Get year, month, and day from the date object
    const year = date.getFullYear();
    // Add 1 to month since JavaScript months are zero-based
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    // Format the date in YYYY-MM-DD format
    return `${year}-${month}-${day}`;
  }

  calculateAll() {
    this.proposalForm.salary.extras = this.selectedExtras;
    let irs, ss;
    if (this.ss) {
      ss = this.ss;
    }
    if (this.irs) {
      irs = this.irs;
    }
    this.proposalService.salaryCalculations(this.proposalForm, irs, ss).subscribe((result) => {
      this.monthlySalaryCalculations.set(result.get('monthly') ?? new Map());
      this.yearlySalaryCalculations.set(result.get('yearly') ?? new Map());
      this.scrValue.set(Number(this.proposalService.getScrValue()));
    });
  }

  getTaxBracket(base: number): Taxes {
    let taxBracket!: Taxes;
    let min = 0;
    let max = 0;
    for (const tax of this.taxes) {
      max = tax.salaryMax;
      if (base >= min && base < max) {
        taxBracket = tax;
      }
      min = max;
    }
    return taxBracket;
  }

  onSubmit() {
    // Map office proposals to ids
    this.proposalForm.office.proposals = this.proposalForm.office.proposals.map((p: any) => p.id);
    // Add selectedExtras list to salary.extras
    this.proposalForm.salary.extras = this.selectedExtras;
    // Format startDate
    this.proposalForm.startDate = this.formatDate(this.proposalForm.startDate);

    // Format dueDate if not indefinite
    if (!this.proposalForm.indefinite) {
      this.proposalForm.dueDate = this.formatDate(this.proposalForm.dueDate);
    }

    // Add selectedCandidate if not null
    if (this.selectedCandidate() != null) {
      this.proposalForm.candidate = this.selectedCandidate();
    }

    // Send the chosen position with the proposal
    this.proposalForm.position = this.selectedPosition;

    // If temporalProposal is not null, it means that we are editing an existing proposal,
    // and if it's null, it means that we are creating a new one.
    if (this.temporalProposal) {
      if (this.latestStatusNumber !== this.latestStatus.status) {
        const status: Status = {
          id: 0,
          status: this.latestStatusNumber,
          message: this.latestStatusMessage,
          createdAt: new Date(),
          createdBy: '',
          modifiedAt: new Date(),
          modifiedBy: '',
        };
        const statusList: Status[] = this.proposalForm.statuses;
        statusList.push(status);
        console.log(statusList);
      }
      this.proposalService.modifyProposal(this.proposalForm, callerHandlesErrors()).subscribe({
        next: () => {
          this.toastService.success(
            'The proposal for ' +
              this.selectedCandidate()?.name +
              ' has been modified successfully!',
          );
          sleep(1000).then(() => {
            this.router.navigate(['/proposals']);
          });
        },
        error: () => {
          this.toastService.error(
            "The proposal couldn't be modified, please check the form for errors or missing fields!",
          );
        },
      });
    } else {
      const status: Status = {
        id: 0,
        status: 0,
        message: 'Draft',
        createdAt: new Date(),
        createdBy: '',
        modifiedAt: new Date(),
        modifiedBy: '',
      };
      this.proposalForm.statuses[0] = status;
      this.proposalService.createProposal(this.proposalForm, callerHandlesErrors()).subscribe({
        next: () => {
          this.toastService.success(
            'The proposal for ' +
              this.selectedCandidate()?.name +
              ' has been created successfully!',
          );
          sleep(1000).then(() => {
            this.router.navigate(['/proposals']);
          });
        },
        error: () => {
          this.toastService.error(
            "The proposal couldn't be created, please check the form for errors or missing fields!",
          );
        },
      });
    }
    this.proposalForm = EmptyProposalForm();
  }

  searchCandidates(event: any) {
    this.candidateService.getFilteredCandidates(event.query).subscribe((candidates) => {
      const all = candidates.candidates as Candidate[];
      const filtered = this.selectedPosition
        ? all.filter((candidate) => candidate.position?.id === this.selectedPosition?.id)
        : all;
      this.filteredCandidates.set(filtered);
    });
  }

  onSelectedPosition() {
    this.selectedCandidate.set(null);
    this.proposalForm.candidate = null;
    this.proposalForm.position = this.selectedPosition;
    this.updateHeaders();
  }

  searchOffices(event: any) {
    this.officeService.getOffices(event).subscribe((offices) => {
      this.filteredOffices.set(offices);
    });
  }

  onSelectedOffice($event: AutoCompleteSelectEvent) {
    this.proposalForm.value.office = $event.value;
  }

  onSelectedCandidate($event: AutoCompleteSelectEvent) {
    this.selectedCandidate.set($event.value);
    this.calculateAll();
    this.updateHeaders();
  }

  onExtrasChange($event: any) {
    this.proposalForm.get('extras')?.setValue($event.value);
  }

  formatCurrency(amount: any, locale = 'es-ES') {
    // Check if amount is a valid number
    if (isNaN(amount)) {
      return 'Invalid amount';
    }

    // Format the number as currency using Intl.NumberFormat
    return new Intl.NumberFormat(locale, {
      style: 'currency',
      currency: 'EUR',
    }).format(amount);
  }
}
