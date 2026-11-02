import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { DropdownModule } from 'primeng/dropdown';
import { InputTextModule } from 'primeng/inputtext';
import { CheckboxModule } from 'primeng/checkbox';
import { TooltipModule } from 'primeng/tooltip';
import { TranslateModule, TranslateService, TranslateStore } from '@ngx-translate/core';
import { TaxesService } from '../../services/taxes.service';
import { ToastService } from '../../services/toast.service';
import { Tax, TaxConfiguration } from '../../utils/types';
import { sleep } from '../../utils/functions';

interface EditableTaxBracket {
  threshold: number;
  rate: number;
}

interface EditableTax {
  name: string;
  type: 'FIXED' | 'BRACKETED';
  fixedRate: number | null;
  extrasOnly: boolean;
  brackets: EditableTaxBracket[];
}

const emptyTax = (): EditableTax => ({
  name: '',
  type: 'FIXED',
  fixedRate: null,
  extrasOnly: false,
  brackets: [],
});

const emptyConfiguration = () => ({
  countryCode: '',
  label: '',
  yearlyPayments: 12,
  active: false as const,
  taxes: [emptyTax()],
});

@Component({
  selector: 'app-taxes',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TableModule,
    ButtonModule,
    DialogModule,
    DropdownModule,
    InputTextModule,
    CheckboxModule,
    TooltipModule,
    TranslateModule,
  ],
  templateUrl: './taxes.component.html',
  styleUrl: './taxes.component.scss',
  providers: [TranslateService, TranslateStore],
})
export class TaxesComponent implements OnInit {
  configurations = signal<TaxConfiguration[]>([]);
  selectedConfiguration = signal<TaxConfiguration | null>(null);
  isLoading = signal(true);
  visibleCreate = signal(false);
  submitting = signal(false);

  newConfiguration = emptyConfiguration();
  taxTypes = [
    { label: 'FIXED RATE', value: 'FIXED' },
    { label: 'BRACKETED RATE', value: 'BRACKETED' },
  ];

  constructor(
    private taxesService: TaxesService,
    private toastService: ToastService,
    private t: TranslateService,
  ) {
    this.t.use(localStorage.getItem('lang') || 'en');
  }

  ngOnInit(): void {
    this.loadConfigurations();
  }

  loadConfigurations(): void {
    this.isLoading.set(true);
    this.taxesService.getTaxConfigurations().subscribe({
      next: (data) => {
        this.configurations.set(data);
        this.selectedConfiguration.set(data.find((c) => c.active) ?? data[0] ?? null);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.toastService.error('The tax configurations could not be loaded');
      },
    });
  }

  onActivate(id: number): void {
    this.taxesService.activateConfiguration(id).subscribe({
      next: () => {
        this.loadConfigurations();
        sleep(250).then(() => this.toastService.success('Tax configuration activated'));
      },
      error: () => {
        this.toastService.error('The tax configuration could not be activated');
      },
    });
  }

  openCreateDialog(): void {
    this.newConfiguration = emptyConfiguration();
    this.visibleCreate.set(true);
  }

  closeCreateDialog(): void {
    this.visibleCreate.set(false);
  }

  addTax(): void {
    this.newConfiguration.taxes.push(emptyTax());
  }

  removeTax(index: number): void {
    this.newConfiguration.taxes.splice(index, 1);
    if (this.newConfiguration.taxes.length === 0) {
      this.addTax();
    }
  }

  addBracket(tax: EditableTax): void {
    tax.brackets.push({ threshold: 0, rate: 0 });
  }

  removeBracket(tax: EditableTax, index: number): void {
    tax.brackets.splice(index, 1);
  }

  onCreate(): void {
    if (!this.isConfigurationValid()) {
      this.toastService.error('Please fill in all required fields');
      return;
    }

    this.submitting.set(true);
    const payload: TaxConfiguration = {
      id: 0,
      countryCode: this.newConfiguration.countryCode.trim(),
      label: this.newConfiguration.label.trim(),
      yearlyPayments: this.newConfiguration.yearlyPayments,
      active: false,
      taxes: this.newConfiguration.taxes.map((tax) => this.mapTax(tax)),
      createdAt: new Date(),
      createdBy: '',
      modifiedAt: new Date(),
      modifiedBy: '',
    };

    this.taxesService.createTaxConfiguration(payload).subscribe({
      next: () => {
        this.submitting.set(false);
        this.closeCreateDialog();
        this.loadConfigurations();
        sleep(250).then(() => this.toastService.success('Tax configuration created'));
      },
      error: () => {
        this.submitting.set(false);
        this.toastService.error('The tax configuration could not be created');
      },
    });
  }

  private mapTax(tax: EditableTax): Tax {
    return {
      id: 0,
      name: tax.name.trim(),
      type: tax.type,
      fixedRate: tax.type === 'FIXED' ? tax.fixedRate ?? 0 : null,
      extrasOnly: tax.extrasOnly,
      brackets:
        tax.type === 'BRACKETED'
          ? tax.brackets.map((b) => ({
              id: 0,
              threshold: b.threshold,
              rate: b.rate,
              createdAt: new Date(),
              createdBy: '',
              modifiedAt: new Date(),
              modifiedBy: '',
            }))
          : [],
      createdAt: new Date(),
      createdBy: '',
      modifiedAt: new Date(),
      modifiedBy: '',
    };
  }

  private isConfigurationValid(): boolean {
    const c = this.newConfiguration;
    if (!c.countryCode.trim() || !c.label.trim() || c.yearlyPayments < 1) {
      return false;
    }
    return c.taxes.every((tax) => {
      if (!tax.name.trim()) return false;
      if (tax.type === 'FIXED' && (tax.fixedRate === null || tax.fixedRate < 0)) return false;
      if (tax.type === 'BRACKETED' && tax.brackets.length === 0) return false;
      return true;
    });
  }
}
