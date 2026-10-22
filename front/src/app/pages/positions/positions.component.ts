import { Component, OnInit, signal } from '@angular/core';
import { TableModule } from 'primeng/table';
import { PaginatorModule, PaginatorState } from 'primeng/paginator';
import { Position } from '../../utils/types';
import { ButtonModule } from 'primeng/button';
import { FormsModule } from '@angular/forms';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { TooltipModule } from 'primeng/tooltip';
import { NewPositionButtonComponent } from '../../components/new-position-button/new-position-button.component';
import { PositionService } from '../../services/position.service';
import { Dialog, DialogModule } from 'primeng/dialog';
import { TranslateModule, TranslateService, TranslateStore } from '@ngx-translate/core';
import { ToastService } from '../../services/toast.service';
import { CommonModule } from '@angular/common';
import { sleep } from '../../utils/functions';
import { callerHandlesErrors } from '../../services/error-toast.interceptor';

@Component({
  selector: 'app-positions',
  standalone: true,
  imports: [
    TableModule,
    ButtonModule,
    DialogModule,
    InputTextareaModule,
    TooltipModule,
    NewPositionButtonComponent,
    PaginatorModule,
    FormsModule,
    CommonModule,
    TranslateModule,
  ],
  providers: [TranslateService, TranslateStore],
  templateUrl: './positions.component.html',
  styleUrl: './positions.component.scss',
})
export class PositionsComponent implements OnInit {
  positions = signal<Position[]>([]);
  position!: Position;
  isLoading = signal(true);

  visiblePositionModal = signal(false);
  visibleDelete = signal(false);
  positionId = -1;

  // Pagination variables with default values:
  first = signal(0);
  rows = signal(10);
  currentPage = 20;
  demandedPage = 0;
  totalPages = 20;
  totalRecords = signal(200);

  constructor(
    private positionService: PositionService,
    private toastService: ToastService,
    private t: TranslateService,
  ) {
    this.t.use(localStorage.getItem('lang') || 'en');
  }

  ngOnInit() {
    this.getPositions(this.demandedPage, this.rows());
    this.resetPosition();
  }

  resetPosition() {
    this.position = {
      id: 0,
      title: '',
      description: '',
      njoyn: '',
      knowMore: '',
    };
  }

  paginate(event: PaginatorState) {
    this.first.set((event.page ?? 0) * (event.rows ?? this.rows()));
    this.rows.set(event.rows ?? this.rows());
    this.demandedPage = event.page ?? 0;
    this.isLoading.set(true);
    this.getPositions(this.demandedPage, this.rows());
  }

  getPositions(page: number, size: number) {
    this.isLoading.set(true);
    this.positionService.getPositions(page, size).subscribe({
      next: (data) => {
        this.positions.set(data.positions);
        this.first.set(page * size + 1);
        this.rows.set(size);
        this.currentPage = data.currentPage + 1;
        this.totalRecords.set(data.totalItems);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      },
    });
  }

  goToModifyPosition(position: Position) {
    this.position = { ...position };
    this.visiblePositionModal.set(true);
  }

  onModifyPosition() {
    const payload = {
      id: this.position.id,
      title: this.position.title,
      description: this.position.description,
      njoyn: this.position.njoyn ?? '',
      knowMore: this.position.knowMore ?? '',
    };
    this.positionService.modifyPosition(payload, callerHandlesErrors()).subscribe({
      next: () => {
        this.visiblePositionModal.set(false);
        this.getPositions(this.demandedPage, this.rows());
        sleep(250).then(() => {
          this.toastService.success('Position saved successfully');
        });
      },
      error: () => {
        this.toastService.error('The position could not be saved');
      },
    });
  }

  deletePosition(): void {
    if (this.positionId !== -1) {
      this.positionService.deletePosition(this.positionId, callerHandlesErrors()).subscribe({
        next: () => {
          this.getPositions(this.demandedPage, this.rows());
          sleep(250).then(() => {
            this.toastService.success('Position deleted successfully');
          });
        },
        error: () => {
          this.toastService.error('The position could not be deleted');
        },
      });
    } else {
      this.toastService.error('positionId is not set');
    }

    this.positionId = -1;
    this.visibleDelete.set(false);
  }

  showDeleteDialog(rowData: Position) {
    this.positionId = rowData.id;
    this.visibleDelete.set(true);
  }

  closeDeleteDialog(dialog: Dialog, event: Event) {
    this.positionId = -1;
    dialog.close(event);
  }

  onSavePosition() {
    // Listen to the event emitted by NewPositionButtonComponent
    this.getPositions(this.demandedPage, this.rows());
  }

  closePositionModal() {
    this.visiblePositionModal.set(false);
    this.resetPosition();
  }
}
