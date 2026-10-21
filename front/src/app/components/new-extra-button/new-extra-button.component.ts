import { Component, EventEmitter, Output } from '@angular/core';
import { NgIconComponent, provideIcons } from '@ng-icons/core';
import { matAddCircleOutline } from '@ng-icons/material-icons/outline';
import { DialogModule } from 'primeng/dialog';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { CheckboxModule } from 'primeng/checkbox';
import {
  TranslateModule,
  TranslateService,
  TranslateStore,
} from '@ngx-translate/core';
import { ExtrasService } from '../../services/extras.service';

@Component({
  selector: 'app-new-extra-button',
  standalone: true,
  imports: [
    CheckboxModule,
    NgIconComponent,
    DialogModule,
    FormsModule,
    ButtonModule,
    TranslateModule,
  ],
  viewProviders: [
    provideIcons({ matAddCircleOutline }),
    TranslateService,
    TranslateStore,
  ],
  template: ` <div>
    <button
      pButton
      [label]="newExtraLabel"
      icon="pi pi-plus"
      class="newProposal bg-[#0056D2] text-white mb-3 px-4 p-1 w-44 h-14 rounded-full  absolute top-0 right-0"
      (click)="newExtra()"
    ></button>
    <p-dialog
      [header]="newExtraLabel"
      [(visible)]="visible"
      [modal]="true"
      [style]="{ width: '75vw' }"
      [draggable]="false"
      [resizable]="false"
    >
      <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <!-- Name -->
        <div class="flex flex-col">
          <label class="text-gray-700 font-semibold">{{
            'NAME' | translate
          }}</label>
          <input [(ngModel)]="extra.name" class="input-field" required />
        </div>
        <!-- Amount -->
        <div class="flex flex-col">
          <label class="text-gray-700 font-semibold">{{
            'AMOUNT' | translate
          }}</label>
          <input [(ngModel)]="extra.amount" class="input-field" required />
        </div>
        <!-- isPercentage -->
        <div class="flex flex-col">
          <label class="text-gray-700 font-semibold">{{
            'IS IT A PERCENTAGE' | translate
          }}</label>
          <p-checkbox [(ngModel)]="extra.isPercentage" [binary]="true" />
        </div>
        <!-- isSSTaxable -->
        <div class="flex flex-col">
          <label class="text-gray-700 font-semibold">{{
            'IS IT TAXABLE BY SOCIAL SECURITY' | translate
          }}</label>
          <p-checkbox [(ngModel)]="extra.isSSTaxable" [binary]="true" />
        </div>
        <!-- ssTaxrates -->
        <div class="flex flex-col">
          <label class="text-gray-700 font-semibold">{{
            'SS Taxrate' | translate
          }}</label>
          <input [(ngModel)]="extra.ssTaxrate" class="input-field" required />
        </div>
        <!-- irsTaxrates -->
        <div class="flex flex-col">
          <label class="text-gray-700 font-semibold">{{
            'IRS Taxrate' | translate
          }}</label>
          <input [(ngModel)]="extra.irsTaxrate" class="input-field" required />
        </div>
        <!-- isIRSTaxable -->
        <div class="flex flex-col">
          <label class="text-gray-700 font-semibold">{{
            'IS IT TAXABLE BY IRS' | translate
          }}</label>
          <p-checkbox [(ngModel)]="extra.isIRSTaxable" [binary]="true" />
        </div>
        <!-- Multiply Monthly -->
        <div class="flex flex-col">
          <label class="text-gray-700 font-semibold">{{
            'MONTHLY MULTIPLIER' | translate
          }}</label>
          <input
            [(ngModel)]="extra.multiplyMonthly"
            class="input-field"
            type="number"
          />
        </div>
        <!-- Yearly multiplier -->
        <div class="flex flex-col">
          <label class="text-gray-700 font-semibold">{{
            'YEARLY MULTIPLIER' | translate
          }}</label>
          <input
            [(ngModel)]="extra.multiplyYearly"
            class="input-field"
            type="number"
          />
        </div>
        <!-- isAddedToSalary -->
        <div class="flex flex-col">
          <label class="text-gray-700 font-semibold">{{
            'IS ADDED TO SALARY' | translate
          }}</label>
          <p-checkbox [(ngModel)]="extra.addToSalary" [binary]="true" />
        </div>
        <button
          class="bg-[#0056D2] text-white py-2 px-4 rounded-md mx-auto block mt-8 col-span-full"
          (click)="onSaveExtra()"
          type="button"
        >
          <label>{{ 'SAVE EXTRA' | translate }}</label>
        </button>
      </div>
    </p-dialog>
  </div>`,
  styles: `
    :host ::ng-deep {
      .p-checkbox-box	 {
        border: 1px solid grey;
      }

      // .p-checkbox-box:focus {
      //   border: none;
      //   background-color: #0056D2;
      // }

      // .p-checkbox-icon {
      //   background-color: #0056D2; /* Set the same background color for the checkmark icon */
      // }

      .input-field {
        border: none !important;
        border-bottom: 1px solid #000 !important;
        border-radius: 0 !important;
        box-shadow: none !important;
        color: #333333;
        background: #EFEFEF;
      }

      .input-field:focus {
        border: none !important;
        border-bottom: 2px solid #0056D2 !important;
        border-radius: 0 !important;
        box-shadow: none !important;
        transition: 100ms ease-out;
      }

      .input-field input:checked {
        background: #0056D2;
      }

      .p-inputtext {
        border: none !important;
        border-bottom: 1px solid #000 !important;
        border-radius: 0 !important;
        box-shadow: none !important;
        color: #333333;
        background: #EFEFEF;
      }

      .p-inputtext:focus {
        border: none !important;
        border-bottom: 2px solid #0056D2 !important;
        border-radius: 0 !important;
        box-shadow: none !important;
        transition: 100ms ease-out;
      }

      textarea:focus, input:focus {
        outline: none;
      }

      .p-button {
        background-color: #0056D2;
        color: white;
        border: none;
        border-radius: 15px;
        padding: 10px 20px;
        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
        transition: background-color 0.3s ease, box-shadow 0.3s ease, transform 0.2s ease;
      }

      .p-button:hover {
        background-color: darken(#0056D2, 10%);
        box-shadow: 0 6px 10px rgba(0, 0, 0, 0.2);
        transform: translateY(-2px); 
        
      }

      .p-button:active {
        background-color: #462C9C;
        opacity: 0.9;
        transform: translateY(1px);
      }

      .p-button:focus {
        outline: none; 
        box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.4), 0 0 0 4px #0056D2; 
      }
  }
  `,
})
export class NewExtraButtonComponent {
  @Output() extraSaved: EventEmitter<void> = new EventEmitter<void>();
  visible: boolean = false;
  extra!: any;

  newExtraLabel!: string;

  constructor(
    private t: TranslateService,
    private extrasService: ExtrasService
  ) {
    this.extra = {
      name: '',
      amount: 0.0,
      isPercentage: false,
      isSSTaxable: false,
      isIRSTaxable: false,
      multiplyYearly: 0,
      multiplyMonthly: 0,
    };

    this.t.use(localStorage.getItem('lang') || 'en');

    this.t
      .get('NEW EXTRA')
      .subscribe((newExtraLabel) => (this.newExtraLabel = newExtraLabel));
  }

  ngOnInit() {}

  newExtra() {
    this.visible = true;
  }

  onSaveExtra() {
    this.extrasService.createExtra(this.extra).subscribe({
      next: (response) => {
        this.visible = false;
        this.extraSaved.emit();
        this.extra = {
          name: '',
          amount: 0.0,
          isPercentage: false,
          isSSTaxable: false,
          isIRSTaxable: false,
          multiplyYearly: 0,
          multiplyMonthly: 0,
        };
      },
      error: (error) => {},
    });
  }
}
