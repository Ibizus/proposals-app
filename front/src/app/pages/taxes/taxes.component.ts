import { Component, signal, OnInit } from '@angular/core';
import { Taxes, Column, TaxConfiguration } from '../../utils/types';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TooltipModule } from 'primeng/tooltip';
import { FormsModule } from '@angular/forms';
import { TaxesService } from '../../services/taxes.service';
import { TranslateModule, TranslateService, TranslateStore } from '@ngx-translate/core';
import { forkJoin } from 'rxjs';
import { ToastService } from '../../services/toast.service';
import { DialogModule, Dialog } from 'primeng/dialog';
import { DropdownModule } from 'primeng/dropdown';
import { FloatLabelModule } from 'primeng/floatlabel';
import { sleep } from '../../utils/functions';
import { callerHandlesErrors } from '../../services/error-toast.interceptor';

@Component({
  selector: 'app-taxes',
  standalone: true,
  imports: [
    TableModule,
    ButtonModule,
    FormsModule,
    TooltipModule,
    TranslateModule,
    DialogModule,
    DropdownModule,
    FloatLabelModule,
  ],
  templateUrl: './taxes.component.html',
  styleUrl: './taxes.component.scss',
  providers: [TranslateService, TranslateStore],
})
export class TaxesComponent implements OnInit {
  taxesTable!: Taxes[];
  configurations: TaxConfiguration[] = [];
  activeConfiguration!: TaxConfiguration;
  clonedTaxes = new Map<number, Taxes>();
  cols!: Column[];
  isLoading = signal(true);
  moneySign = '€';
  visible = signal(false);
  taxId = -1;
  countryOptions: string[] = [];
  selectedCountry!: string;
  editTaxesTooltip!: string;
  deleteTaxesTooltip!: string;
  saveTaxesTooltip!: string;
  cancelTaxesTooltip!: string;

  constructor(
    private taxesService: TaxesService,
    private toastService: ToastService,
    private t: TranslateService,
  ) {
    this.t.use(localStorage.getItem('lang') || 'en');

    const editTaxesTooltip$ = this.t.get('EDIT TAXES');
    const deleteTaxesTooltip$ = this.t.get('DELETE TAXES');
    const saveTaxesTooltip$ = this.t.get('SAVE TAXES');
    const cancelTaxesTooltip$ = this.t.get('CANCEL TAXES');
    const salaryMax$ = this.t.get('SALARY UP TO');
    const socialSecurityTax$ = this.t.get('SOCIAL SECURITY TAX');
    const validFrom$ = this.t.get('VALID FROM');
    const validUntil$ = this.t.get('VALID UNTIL');
    const zero$ = this.t.get('ZERO');

    forkJoin([
      editTaxesTooltip$,
      deleteTaxesTooltip$,
      saveTaxesTooltip$,
      cancelTaxesTooltip$,
      salaryMax$,
      socialSecurityTax$,
      validFrom$,
      validUntil$,
      zero$,
    ]).subscribe(
      ([
        editTaxesTooltip,
        deleteTaxesTooltip,
        saveTaxesTooltip,
        cancelTaxesTooltip,
        salaryMax,
        socialSecurityTax,
        validFrom,
        validUntil,
        zero,
      ]) => {
        this.editTaxesTooltip = editTaxesTooltip;
        this.deleteTaxesTooltip = deleteTaxesTooltip;
        this.saveTaxesTooltip = saveTaxesTooltip;
        this.cancelTaxesTooltip = cancelTaxesTooltip;
        this.cols = [
          { field: 'salaryMax', header: salaryMax },
          {
            field: 'socialSecurityTax',
            header: socialSecurityTax,
          },
          { field: 'zero', header: zero },
          { field: 'validFrom', header: validFrom },
          { field: 'validUntil', header: validUntil },
        ];
      },
    );
  }

  ngOnInit(): void {
    this.loadConfigurations();
    this.getTaxes();
  }

  loadConfigurations() {
    this.taxesService.getTaxConfigurations().subscribe({
      next: (configs) => {
        this.configurations = configs;
        this.countryOptions = configs.map((c) => c.countryCode.toUpperCase());
        this.activeConfiguration = configs.find((c) => c.active) ?? configs[0];
        this.selectedCountry = this.activeConfiguration?.countryCode.toUpperCase();
      },
    });
  }

  getTaxes() {
    this.isLoading.set(true);
    this.taxesService.getTaxes().subscribe({
      next: (data) => {
        this.taxesTable = data;
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      },
    });
  }

  onCountryChange(event: { value: string }) {
    const code = event.value.toLowerCase();
    const config = this.configurations.find((c) => c.countryCode === code);
    if (config && config.id !== this.activeConfiguration?.id) {
      this.taxesService.activateConfiguration(config.id).subscribe({
        next: () => {
          this.activeConfiguration = config;
          this.getTaxes();
          this.toastService.success('Configuration activated');
        },
        error: () => {
          this.toastService.error('Could not activate configuration');
        },
      });
    }
  }

  updateTaxes(modifiedTaxes: Taxes) {
    const taxesCopy = { ...modifiedTaxes };
    this.taxesService.modifyTaxes(taxesCopy, callerHandlesErrors()).subscribe({
      next: (data) => {
        Object.assign(taxesCopy, data);
        this.toastService.success('Taxes saved correctly');
      },
      error: () => {
        this.toastService.error('The taxes could not be saved');
      },
    });
  }

  deleteTaxes(): void {
    if (this.taxId !== -1) {
      this.taxesService.deleteTaxes(this.taxId, callerHandlesErrors()).subscribe({
        next: () => {
          this.ngOnInit();
          sleep(200).then(() => {
            this.toastService.success('Taxes deleted succesfully');
          });
        },
        error: () => {
          this.toastService.error('The taxes could not be deleted');
        },
      });
    }

    this.taxId = -1;
    this.visible.set(false);
  }

  closeDialog(dialog: Dialog, event: Event) {
    this.taxId = -1;
    dialog.close(event);
  }

  showDialog(rowData: Taxes) {
    this.taxId = rowData.id;
    this.visible.set(true);
  }

  // HTML BUTTON METHODS:
  onRowEditInit(taxes: Taxes) {
    this.clonedTaxes.set(taxes.id, { ...taxes });
  }

  onRowEditSave(taxes: Taxes) {
    if (taxes.salaryMax > 0) {
      this.updateTaxes(taxes);
      this.clonedTaxes.delete(taxes.id);
    } else {
      this.toastService.error('Could not save the Taxes');
    }
  }

  onRowEditCancel(taxes: Taxes, index: number) {
    this.taxesTable[index] = this.clonedTaxes.get(taxes.id) ?? taxes;
    this.clonedTaxes.delete(taxes.id);
  }

  onRowDelete() {
    this.deleteTaxes();
  }
}
