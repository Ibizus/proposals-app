import { Component, signal, OnInit } from '@angular/core';
import { Extra } from '../../utils/types';
import { TableModule } from 'primeng/table';
import { PaginatorModule, PaginatorState } from 'primeng/paginator';
import { ButtonModule } from 'primeng/button';
import { FormsModule } from '@angular/forms';
import { TooltipModule } from 'primeng/tooltip';
import { NewExtraButtonComponent } from '../../components/new-extra-button/new-extra-button.component';
import { TranslateModule, TranslateService, TranslateStore } from '@ngx-translate/core';
import { ExtrasService } from '../../services/extras.service';
import { ToastService } from '../../services/toast.service';
import { DialogModule, Dialog } from 'primeng/dialog';
import { sleep } from '../../utils/functions';
import { callerHandlesErrors } from '../../services/error-toast.interceptor';

@Component({
  selector: 'app-extras',
  standalone: true,
  imports: [
    TableModule,
    PaginatorModule,
    ButtonModule,
    FormsModule,
    NewExtraButtonComponent,
    TooltipModule,
    TranslateModule,
    DialogModule,
  ],
  providers: [TranslateService, TranslateStore],
  templateUrl: './extras.component.html',
  styleUrl: './extras.component.scss',
})
export class ExtrasComponent implements OnInit {
  extras = signal<Extra[]>([]);
  clonedExtra = new Map<number, Extra>();
  displayedColumns: string[] = ['id', 'name', 'amount', 'isAPercentage', 'multiplyToMakeItYearly'];
  // Pagination variables with default values:
  first = signal(0);
  rows = signal(10);
  currentPage = 20;
  demandedPage = 0;
  totalPages = 20;
  totalRecords = signal(200);
  visible = signal(false);
  extrasId = -1;
  isLoading = signal(true);

  percentage!: string;
  amount!: string;
  yes!: string;
  no!: string;

  duplicateExtraTooltip!: string;
  modifyExtraTooltip!: string;
  deleteExtraTooltip!: string;
  saveExtraTooltip!: string;
  cancelExtraTooltip!: string;

  constructor(
    private toastService: ToastService,
    private translateService: TranslateService,
    private extraService: ExtrasService,
  ) {
    this.translateService.use(localStorage.getItem('lang') || 'en');

    this.translateService.get(['PERCENTAGE', 'AMOUNT', 'YES', 'NO']).subscribe((translations) => {
      this.percentage = translations['PERCENTAGE'];
      this.amount = translations['AMOUNT'];
      this.yes = translations['YES'];
      this.no = translations['NO'];
    });

    this.translateService
      .get(['DUPLICATE_EXTRA', 'MODIFY_EXTRA', 'DELETE_EXTRA', 'SAVE_EXTRA', 'CANCEL_EXTRA'])
      .subscribe((translations) => {
        this.duplicateExtraTooltip = translations['DUPLICATE_EXTRA'];
        this.modifyExtraTooltip = translations['MODIFY_EXTRA'];
        this.deleteExtraTooltip = translations['DELETE_EXTRA'];
        this.saveExtraTooltip = translations['SAVE_EXTRA'];
        this.cancelExtraTooltip = translations['CANCEL_EXTRA'];
      });
  }

  ngOnInit(): void {
    this.getExtras(this.demandedPage, this.rows());
  }

  paginate(event: PaginatorState) {
    this.first.set((event.page ?? 0) * (event.rows ?? this.rows()));
    this.rows.set(event.rows ?? this.rows());
    this.demandedPage = event.page ?? 0; //Index of the new page
    this.getExtras(this.demandedPage, this.rows());
  }

  getExtras(page: number, size: number) {
    this.isLoading.set(true);
    this.extraService.getExtras(page, size).subscribe({
      next: (data) => {
        this.extras.set(data.extras);
        this.first.set(page * size + 1); // Calculate index of first item shown
        this.rows.set(size); // Update size input from the request
        this.currentPage = data.currentPage + 1; // In back first pago is index 0
        this.totalRecords.set(data.totalItems);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      },
    });
  }

  duplicateExtra(extra: Extra) {
    this.extraService.duplicateExtra(extra).subscribe((data) => {
      this.extras.update((list) => [...list, data]);
      this.ngOnInit();
      sleep(500).then(() => {
        this.toastService.success('Extra duplicated correctly');
      });
    });
  }

  updateExtra(modifiedExtra: Extra, id: number) {
    const extraCopy = { ...modifiedExtra };
    this.extraService.updateExtra(modifiedExtra, id, callerHandlesErrors()).subscribe({
      next: (data) => {
        Object.assign(extraCopy, data);
        this.toastService.success('Extra saved correctly');
      },
      error: () => {
        this.toastService.error('The extra could not be saved');
      },
    });
  }

  deleteExtra(): void {
    if (this.extrasId !== -1) {
      this.extraService.deleteExtra(this.extrasId, callerHandlesErrors()).subscribe({
        next: () => {
          this.ngOnInit();
          sleep(500).then(() => {
            this.toastService.success('Extra deleted succesfully');
          });
        },
        error: () => {
          this.toastService.error('The extra could not be deleted');
        },
      });
    }
    this.extrasId = -1;
    this.visible.set(false);
  }

  // HTML IN ROW EDITING METHODS:
  onRowEditInit(extra: Extra) {
    this.clonedExtra.set(extra.id, { ...extra });
  }

  onRowEditSave(extra: Extra) {
    if (extra.amount > 0) {
      // Conditions to validate row form inputs
      this.updateExtra(extra, extra.id);
      this.clonedExtra.delete(extra.id);
    } else {
      this.toastService.error('Could not save extra');
    }
  }

  onRowEditCancel(extra: Extra, index: number) {
    this.extras.update((list) => {
      const updated = [...list];
      updated[index] = this.clonedExtra.get(extra.id) ?? extra;
      return updated;
    });
    this.clonedExtra.delete(extra.id);
  }

  onSaveExtra() {
    // Listen to the event emitted by NewCandidateButtonComponent
    this.ngOnInit();
  }

  closeDialog(dialog: Dialog, event: Event) {
    this.extrasId = -1;
    dialog.close(event);
  }

  showDialog(rowData: Extra) {
    this.extrasId = rowData.id;
    this.visible.set(true);
  }
}
