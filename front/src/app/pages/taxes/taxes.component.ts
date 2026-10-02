import { Component } from '@angular/core';
import { Taxes, Column } from '../../utils/types';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { TooltipModule } from 'primeng/tooltip';
import { NgFor } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TaxesService } from '../../services/taxes.service';
import {
  TranslateModule,
  TranslateService,
  TranslateStore,
} from '@ngx-translate/core';
import { forkJoin } from 'rxjs';
import { ToastService } from '../../services/toast.service';
import { DialogModule, Dialog } from 'primeng/dialog';
import { DropdownModule } from 'primeng/dropdown';
import { FloatLabelModule } from 'primeng/floatlabel';
import { sleep } from '../../utils/functions';

@Component({
  selector: 'app-taxes',
  standalone: true,
  imports: [
    NgFor,
    TableModule,
    ButtonModule,
    FormsModule,
    ToastModule,
    TooltipModule,
    TranslateModule,
    DialogModule,
    DropdownModule,
    FloatLabelModule,
  ],
  templateUrl: './taxes.component.html',
  styleUrl: './taxes.component.scss',
  providers: [MessageService, TranslateService, TranslateStore, ToastService],
})
export class TaxesComponent {
  taxesTable!: Taxes[];
  clonedTaxes: { [s: number]: Taxes } = {};
  cols!: Column[];
  selectedRow: any;
  moneySign: string = '€';
  visible: boolean = false;
  taxId: number = -1;
  maritalStatus!: string[];
  numberOfDependents: string[] = ['0', '1', '2', '3', '4', '5+'];
  maritalPlaceholder!: string;
  // numberOfDependentsPlaceholder = 'Number of Dependents';
  editTaxesTooltip!: string;
  deleteTaxesTooltip!: string;
  saveTaxesTooltip!: string;
  cancelTaxesTooltip!: string;

  constructor(
    private taxesService: TaxesService,
    private toastService: ToastService,
    private t: TranslateService
  ) {
    this.t.use(localStorage.getItem('lang') || 'en');

    const unmarried$ = this.t.get('UNMARRIED');
    const marriedOne$ = this.t.get('MARRIED ONE');
    const marriedTwo$ = this.t.get('MARRIED TWO');
    const editTaxesTooltip$ = this.t.get('EDIT TAXES');
    const deleteTaxesTooltip$ = this.t.get('DELETE TAXES');
    const saveTaxesTooltip$ = this.t.get('SAVE TAXES');
    const cancelTaxesTooltip$ = this.t.get('CANCEL TAXES');
    const salaryMax$ = this.t.get('SALARY UP TO');
    const socialSecurityTax$ = this.t.get('SOCIAL SECURITY TAX');
    const validFrom$ = this.t.get('VALID FROM');
    const validUntil$ = this.t.get('VALID UNTIL');
    const zero$ = this.t.get('ZERO');
    const one$ = this.t.get('ONE');
    const two$ = this.t.get('TWO');
    const three$ = this.t.get('THREE');
    const four$ = this.t.get('FOUR');
    const five$ = this.t.get('FIVE');

    forkJoin([
      unmarried$,
      marriedOne$,
      marriedTwo$,
      editTaxesTooltip$,
      deleteTaxesTooltip$,
      saveTaxesTooltip$,
      cancelTaxesTooltip$,
      salaryMax$,
      socialSecurityTax$,
      validFrom$,
      validUntil$,
      zero$,
      one$,
      two$,
      three$,
      four$,
      five$,
    ]).subscribe(
      ([
        unmarried,
        marriedOne,
        marriedTwo,
        editTaxesTooltip,
        deleteTaxesTooltip,
        saveTaxesTooltip,
        cancelTaxesTooltip,
        salaryMax,
        socialSecurityTax,
        validFrom,
        validUntil,
        zero,
        one,
        two,
        three,
        four,
        five,
      ]) => {
        this.maritalStatus = [unmarried, marriedOne, marriedTwo];
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
          { field: 'one', header: one },
          { field: 'two', header: two },
          { field: 'three', header: three },
          { field: 'four', header: four },
          { field: 'five', header: five },
          { field: 'validFrom', header: validFrom },
          { field: 'validUntil', header: validUntil },
          // {
          //   field: 'taxPercentageCompany',
          //   header: taxPercentageCompany,
          // },
        ];
      }
    );
  }

  ngOnInit(): void {
    this.getTaxesForMaritalStatus(0);
  }

  getTaxes() {
    this.taxesService.getTaxes().subscribe((data) => {
      this.taxesTable = data;
    });
  }

  getTaxesForMaritalStatus(maritalStatus: number) {
    this.taxesService
      .getTaxesForMaritalStatus(maritalStatus)
      .subscribe((data) => {
        this.taxesTable = data;
      });
  }

  updateTaxes(modifiedTaxes: Taxes) {
    let taxesCopy = { ...modifiedTaxes };
    this.taxesService
      .modifyTaxes(taxesCopy)
      .subscribe((data) => Object.assign(taxesCopy, data));
  }

  deleteTaxes(): void {
    if (this.taxId !== -1) {
      this.taxesService.deleteTaxes(this.taxId).subscribe({
        next: () => {
          this.ngOnInit();
          sleep(200).then(() => {
            this.toastService.success('Taxes deleted succesfully');
          });
        },
        error: (error) => {
          this.toastService.error(error);
        },
      });
    }

    this.taxId = -1;
    this.visible = false;
  }

  closeDialog(dialog: Dialog, event: any) {
    this.taxId = -1;
    dialog.close(event);
  }

  showDialog(rowData: any) {
    this.taxId = rowData.id;
    this.visible = true;
  }

  onMaritalStatusChange($event: any) {
    const value = $event.value;
    const index = this.maritalStatus.findIndex((status) => status === value);
    this.getTaxesForMaritalStatus(index);
  }

  // HTML BUTTON METHODS:
  onRowEditInit(taxes: Taxes) {
    this.clonedTaxes[taxes.id as number] = { ...taxes };
  }

  onRowEditSave(taxes: Taxes) {
    if (taxes.salaryMax > 0 && taxes.taxPercentageCompany > 0) {
      this.updateTaxes(taxes);
      delete this.clonedTaxes[taxes.id as number];
      sleep(500).then(() => {
        this.toastService.success('Taxes saved correctly');
      });
    } else {
      this.toastService.error('Could not save the Taxes');
    }
  }

  onRowEditCancel(taxes: Taxes, index: number) {
    this.taxesTable[index] = this.clonedTaxes[taxes.id as number];
    delete this.clonedTaxes[taxes.id as number];
  }

  onRowDelete(taxes: Taxes) {
    this.deleteTaxes();
  }
}