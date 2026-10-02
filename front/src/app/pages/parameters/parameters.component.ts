import { Component, OnInit, ViewChild } from '@angular/core';
import { Table, TableModule } from 'primeng/table';
import { Parameter } from '../../utils/types';
import { ButtonModule } from 'primeng/button';
import { TooltipModule } from 'primeng/tooltip';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { ToastService } from '../../services/toast.service';
import { IconFieldModule } from 'primeng/iconfield';
import { Dialog, DialogModule } from 'primeng/dialog';
import { InputIconModule } from 'primeng/inputicon';
import { FormsModule } from '@angular/forms';
import { ParameterService } from '../../services/parameter.service';
import {
  TranslateModule,
  TranslateService,
  TranslateStore,
} from '@ngx-translate/core';

@Component({
  selector: 'app-parameters',
  standalone: true,
  imports: [
    TableModule,
    IconFieldModule,
    TooltipModule,
    DialogModule,
    ButtonModule,
    ToastModule,
    TranslateModule,
    InputIconModule,
    FormsModule,
  ],
  templateUrl: './parameters.component.html',
  styleUrl: './parameters.component.scss',
  providers: [
    MessageService,
    TranslateService,
    TranslateStore,
    ToastService,
    ParameterService,
  ],
})
export class ParametersComponent implements OnInit {
  parameters: Parameter[] = [];
  paramId: number = -1;
  currentParameter!: Parameter;
  visible: boolean = false;
  isLoading: boolean = true;
  @ViewChild('dt2') dataTable!: Table;
  displayedColumns: string[] = [
    'id',
    'name',
    'value',
    'validFrom',
    'validUntil',
  ];
  modifyParameterTooltip!: string;
  deleteParameterTooltip!: string;
  saveParameterTooltip!: string;
  cancelParameterTooltip!: string;

  constructor(
    private toastService: ToastService,
    private t: TranslateService,
    private parameterService: ParameterService
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
        this.parameters = [...data];
        this.isLoading = false;
        console.log('Datos cargados', data);
      },
      error: (error) => {
        console.error('Error while fetching parameters: ', error);
        this.isLoading = false;
      },
    });
  }

  deleteParameter(parameter: Parameter): void {
    this.parameterService.deleteParameter(parameter).subscribe({
      next: () => {
        this.parameters = this.parameters.filter((p) => p.id !== parameter.id);
        setTimeout(() => {
          this.toastService.success('Parameter deleted successfully');
          this.paramId = -1;
          this.visible = false;
          console.log('Se completo con exito');
        }, 500);
      },
      error: (error) => {
        this.toastService.error(error);
      },
    });
  }

  modifyParameter(modifiedParameter: Parameter) {
    let parameterCopy = { ...modifiedParameter };
    this.parameterService.modifyParameter(modifiedParameter).subscribe({
      next: (data) => {
        Object.assign(parameterCopy, data);
        this.toastService.success('Parameter modified successfully');
      },
      error: (error) => {
        this.toastService.error(error);
      },
    });
  }

  showDialog(parameter: Parameter): any {
    this.currentParameter = parameter;
    this.visible = true;
  }

  closeDialog(dialog: Dialog, event: any) {
    this.currentParameter.id = -1;
    dialog.close(event);
  }
}
