import { Component, OnInit, ViewChild, signal } from '@angular/core';
import { Table, TableModule } from 'primeng/table';
import { Parameter } from '../../utils/types';
import { ButtonModule } from 'primeng/button';
import { TooltipModule } from 'primeng/tooltip';
import { ToastService } from '../../services/toast.service';
import { IconFieldModule } from 'primeng/iconfield';
import { Dialog, DialogModule } from 'primeng/dialog';
import { InputIconModule } from 'primeng/inputicon';
import { FormsModule } from '@angular/forms';
import { ParameterService } from '../../services/parameter.service';
import { TranslateModule, TranslateService, TranslateStore } from '@ngx-translate/core';

@Component({
  selector: 'app-parameters',
  standalone: true,
  imports: [
    TableModule,
    IconFieldModule,
    TooltipModule,
    DialogModule,
    ButtonModule,
    TranslateModule,
    InputIconModule,
    FormsModule,
  ],
  templateUrl: './parameters.component.html',
  styleUrl: './parameters.component.scss',
  providers: [TranslateService, TranslateStore, ParameterService],
})
export class ParametersComponent implements OnInit {
  parameters = signal<Parameter[]>([]);
  paramId = -1;
  currentParameter!: Parameter;
  visible = false;
  isLoading = signal(true);
  @ViewChild('dt2') dataTable!: Table;
  displayedColumns: string[] = ['id', 'name', 'value', 'validFrom', 'validUntil'];
  modifyParameterTooltip!: string;
  deleteParameterTooltip!: string;
  saveParameterTooltip!: string;
  cancelParameterTooltip!: string;

  constructor(
    private toastService: ToastService,
    private t: TranslateService,
    private parameterService: ParameterService,
  ) {
    this.t.use(localStorage.getItem('lang') || 'en');

    this.t.get('EDIT PARAMETERS').subscribe((modifyParameterTooltip) => {
      this.modifyParameterTooltip = modifyParameterTooltip;
    });

    this.t.get('DELETE PARAMETERS').subscribe((deleteParameterTooltip) => {
      this.deleteParameterTooltip = deleteParameterTooltip;
    });

    this.t.get('SAVE PARAMETERS').subscribe((saveParameterTooltip) => {
      this.saveParameterTooltip = saveParameterTooltip;
    });

    this.t.get('CANCEL PARAMETERS').subscribe((cancelParameterTooltip) => {
      this.cancelParameterTooltip = cancelParameterTooltip;
    });
  }

  ngOnInit(): void {
    this.getParameters();
  }

  filterParameters(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value;
    if (this.dataTable) {
      this.dataTable.filterGlobal(filterValue, 'contains');
    }
  }

  getParameters() {
    this.parameterService.getParameters().subscribe({
      next: (data) => {
        this.parameters.set([...data]);
        this.isLoading.set(false);
        console.log('Parameters loaded', data);
      },
      error: (error) => {
        console.error('Error while fetching parameters: ', error);
        this.isLoading.set(false);
      },
    });
  }

  deleteParameter(parameter: Parameter): void {
    this.parameterService.deleteParameter(parameter).subscribe({
      next: () => {
        this.parameters.update((list) => list.filter((p) => p.id !== parameter.id));
        setTimeout(() => {
          this.toastService.success('Parameter deleted successfully');
          this.paramId = -1;
          this.visible = false;
          console.log('Completed successfully');
        }, 500);
      },
      error: () => undefined,
    });
  }

  modifyParameter(modifiedParameter: Parameter) {
    const parameterCopy = { ...modifiedParameter };
    this.parameterService.modifyParameter(modifiedParameter).subscribe({
      next: (data) => {
        Object.assign(parameterCopy, data);
        this.toastService.success('Parameter modified successfully');
      },
      error: () => undefined,
    });
  }

  showDialog(parameter: Parameter): void {
    this.currentParameter = parameter;
    this.visible = true;
  }

  closeDialog(dialog: Dialog, event: Event) {
    this.currentParameter.id = -1;
    dialog.close(event);
  }
}
