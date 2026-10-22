import { Component, EventEmitter, Output, ViewChild } from '@angular/core';
import { NgForm } from '@angular/forms';
import { NgIconComponent, provideIcons } from '@ng-icons/core';
import { matAddCircleOutline } from '@ng-icons/material-icons/outline';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { Position } from '../../utils/types';
import { PositionService } from '../../services/position.service';
import { TranslateModule, TranslateService, TranslateStore } from '@ngx-translate/core';

/**
 * A button that opens a dialog to create a new position.
 */
@Component({
  selector: 'app-new-position-button',
  standalone: true,
  imports: [
    NgIconComponent,
    DialogModule,
    InputTextModule,
    InputTextareaModule,
    FormsModule,
    ButtonModule,
    TranslateModule,
  ],
  viewProviders: [provideIcons({ matAddCircleOutline }), TranslateService, TranslateStore, NgForm],
  template: `
    <button
      pButton
      [label]="newPositionLabel"
      icon="pi pi-plus"
      class="bg-[#0056D2] text-white px-4 p-1 h-14 rounded-full md:absolute md:top-0 md:right-0"
      (click)="newPosition()"
    ></button>

    <p-dialog
      [header]="newPositionLabel"
      [(visible)]="visible"
      [modal]="true"
      [style]="{ width: '50vw' }"
      [draggable]="false"
      [resizable]="false"
      (onHide)="resetForm()"
    >
      <span class="p-text-secondary block mb-5">{{ 'MODAL NEW POSITION HELP' | translate }}</span>
      <form #form="ngForm" (ngSubmit)="onSavePosition()">
        <div class="grid grid-cols-1 gap-4 p-4">
          <!-- Title -->
          <div class="max-w-sm">
            <label class="block text-sm font-medium mb-2"
              >{{ 'TITLE' | translate }}<span class="mandatory">*</span></label
            >
            <input
              type="text"
              [(ngModel)]="position.title"
              name="title"
              placeholder=""
              required
              #title="ngModel"
            />
            @if (title.invalid && (title.dirty || title.touched)) {
              <small>
                <p class="text-red-500">{{ 'PLEASE ENTER A VALID TITLE' | translate }}</p>
              </small>
            } @else {
              <small>{{ 'TITLE FIELD INFORMATION' | translate }}</small>
            }
          </div>

          <!-- Description -->
          <div class="max-w-sm">
            <label class="block text-sm font-medium mb-2"
              >{{ 'DESCRIPTION' | translate }}<span class="mandatory">*</span></label
            >
            <textarea
              rows="4"
              pInputTextarea
              [(ngModel)]="position.description"
              name="description"
              required
              #description="ngModel"
            ></textarea>
            @if (description.invalid && (description.dirty || description.touched)) {
              <small>
                <p class="text-red-500">{{ 'PLEASE ENTER A VALID DESCRIPTION' | translate }}</p>
              </small>
            } @else {
              <small>{{ 'DESCRIPTION FIELD INFORMATION' | translate }}</small>
            }
          </div>

          <!-- nJoyn -->
          <div class="max-w-sm">
            <label class="block text-sm font-medium mb-2">{{ 'NJOYN' | translate }}</label>
            <input type="text" [(ngModel)]="position.njoyn" name="njoyn" placeholder="" />
            <small>{{ 'NJOYN FIELD INFORMATION' | translate }}</small>
          </div>

          <!-- Know more -->
          <div class="max-w-sm">
            <label class="block text-sm font-medium mb-2">{{ 'KNOW MORE' | translate }}</label>
            <input type="text" [(ngModel)]="position.knowMore" name="knowMore" placeholder="" />
            <small>{{ 'KNOW MORE FIELD INFORMATION' | translate }}</small>
          </div>

          <button
            pButton
            class="bg-[#0056D2] text-white py-2 px-4 rounded-md mx-auto block mt-6 col-span-full font-bold"
            type="submit"
          >
            <span>{{ 'SAVE POSITION' | translate }}</span>
          </button>
          @if (errorMessage) {
            <small class="text-red-500 mx-auto block col-span-full">
              {{ 'INVALID FORM' | translate }}
            </small>
          }
        </div>
      </form>
    </p-dialog>
  `,
  styles: `
    :host ::ng-deep {
      .p-button {
        background-color: #0056d2;
        color: white;
        border: none;
        border-radius: 20px;
        padding: 10px 20px;
        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
        transition:
          background-color 0.3s ease,
          box-shadow 0.3s ease,
          transform 0.2s ease;
      }

      label {
        display: block;
        color: #333;
        font-size: 1rem;
        font-weight: medium;
        margin-bottom: 0.5rem;
      }

      input,
      textarea {
        width: 100%;
        padding: 0.8rem 1rem;
        font-size: 1rem;
        border: 2px solid #f0f9ff;
        border-radius: 0.375rem;
        box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.1);
        transition:
          border-color 0.3s,
          box-shadow 0.3s;
      }

      input:hover,
      textarea:hover {
        border-color: #ebfbf3;
      }

      input:focus,
      textarea:focus {
        outline: none;
        border-color: #0056b3;
        box-shadow: 0 0 0 3px rgba(0, 86, 179, 0.5);
      }

      .mandatory {
        padding: 1px;
      }
    }
  `,
})
export class NewPositionButtonComponent {
  @Output() positionSaved: EventEmitter<void> = new EventEmitter<void>();
  @ViewChild('form') form!: NgForm;
  visible = false;
  newPositionLabel!: string;
  errorMessage = false;

  position: Position = {
    id: 0,
    title: '',
    description: '',
    njoyn: '',
    knowMore: '',
  };

  constructor(
    private t: TranslateService,
    private positionService: PositionService,
  ) {
    this.t.use(localStorage.getItem('lang') || 'en');
    this.t
      .get('NEW POSITION')
      .subscribe((newPositionLabel) => (this.newPositionLabel = newPositionLabel));
  }

  newPosition() {
    this.visible = true;
    this.resetForm();
  }

  onSavePosition() {
    if (this.form.valid) {
      this.positionService.createPosition(this.position).subscribe({
        next: () => {
          this.visible = false;
          this.positionSaved.emit();
          this.resetForm();
        },
        error: () => {
          this.errorMessage = true;
        },
      });
    } else {
      this.errorMessage = true;
    }
  }

  resetForm() {
    this.form?.reset();
    this.errorMessage = false;
    this.position = {
      id: 0,
      title: '',
      description: '',
      njoyn: '',
      knowMore: '',
    };
  }
}
