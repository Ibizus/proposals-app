import { Component, EventEmitter, OnInit, Output, ViewChild } from '@angular/core';
import { NgForm } from '@angular/forms';
import { NgIconComponent, provideIcons } from '@ng-icons/core';
import { matAddCircleOutline } from '@ng-icons/material-icons/outline';
import { InputTextModule } from 'primeng/inputtext';
import { DialogModule } from 'primeng/dialog';
import { DropdownModule } from 'primeng/dropdown';
import { Candidate } from '../../utils/types';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { CandidateService } from '../../services/candidate.service';
import { TranslateModule, TranslateService, TranslateStore } from '@ngx-translate/core';
import { forkJoin } from 'rxjs';
/**
 * This component represents a new candidate button. It displays a dialog
 * to create a new candidate. It also provides a method to reset the form.
 */
@Component({
  selector: 'app-new-candidate-button',
  standalone: true,
  imports: [
    NgIconComponent,
    DialogModule,
    InputTextModule,
    FormsModule,
    DropdownModule,
    ButtonModule,
    TranslateModule,
  ],
  viewProviders: [provideIcons({ matAddCircleOutline }), TranslateService, TranslateStore, NgForm],
  template: `
    <div>
      <div class="flex justify-between">
        <button
          [label]="newCandidateString"
          pButton
          icon="pi pi-plus"
          class="bg-[#0056D2] text-white px-4 p-1 h-14 rounded-full md:absolute md:top-0 md:right-0"
          (click)="newCandidate()"
          [label]="newCandidateString"
        ></button>
      </div>

      <p-dialog
        class=""
        [header]="newCandidateString"
        [(visible)]="visible"
        [modal]="true"
        [style]="{ width: '50vw' }"
        [draggable]="false"
        [resizable]="false"
        (onHide)="resetForm()"
      >
        <span class="p-text-secondary block mb-5">{{
          'MODAL NEW CANDIDATE HELP' | translate
        }}</span>
        <form #form="ngForm" (ngSubmit)="onSaveCandidate()">
          <div class="grid grid-cols-1 lg:grid-cols-2 gap-4 p-4 ">
            <!-- Name -->
            <div class="max-w-sm">
              <label class="block text-sm font-medium mb-2"
                >{{ 'NAME' | translate }}<span class="mandatory">*</span></label
              >
              <input
                type="text"
                [(ngModel)]="candidate.name"
                name="name"
                placeholder=""
                required
                pattern="^[a-zA-ZáéíóúÁÉÍÓÚâêôÂÊÔãõÃÕàÀçÇ '-]*$"
                #name="ngModel"
              />
              @if (
                (name.errors?.['pattern'] && (name.dirty || name.touched)) ||
                (name.invalid && (name.dirty || name.touched))
              ) {
                <small>
                  <p class="text-red-500">
                    {{ 'PLEASE ENTER A VALID NAME' | translate }}
                  </p>
                </small>
              } @else {
                <small>
                  {{ 'NAME FIELD INFORMATION' | translate }}
                </small>
              }
            </div>

            <!-- Surname -->
            <div class="max-w-sm">
              <label class="block text-sm font-medium mb-2"
                >{{ 'SURNAME' | translate }}<span class="mandatory">*</span></label
              >
              <input
                type="text"
                [(ngModel)]="candidate.surname"
                name="surname"
                placeholder=""
                required
                pattern="^[a-zA-ZáéíóúÁÉÍÓÚâêôÂÊÔãõÃÕàÀçÇ '-]*$"
                #surname="ngModel"
              />
              @if (
                (surname.errors?.['pattern'] && (surname.dirty || surname.touched)) ||
                (surname.invalid && (surname.dirty || surname.touched))
              ) {
                <small>
                  <p class="text-red-500">
                    {{ 'PLEASE ENTER A VALID SURNAME' | translate }}
                  </p>
                </small>
              } @else {
                <small>
                  {{ 'SURNAME FIELD INFORMATION' | translate }}
                </small>
              }
            </div>

            <!-- ID Number -->
            <div class="max-w-sm">
              <label class="block text-sm font-medium mb-2"
                >{{ 'NATIONAL ID NUMBER' | translate }}<span class="mandatory">*</span></label
              >
              <input
                type="text"
                [(ngModel)]="candidate.idNumber"
                name="idNumber"
                placeholder=""
                required
                pattern="^[a-zA-Z0-9-]+$"
                #idNumber="ngModel"
              />
              @if (
                (idNumber.errors?.['pattern'] && (idNumber.dirty || idNumber.touched)) ||
                (idNumber.invalid && (idNumber.dirty || idNumber.touched))
              ) {
                <small>
                  <p class="text-red-500">
                    {{ 'NATIONAL ID NUMBER IS REQUIRED' | translate }}
                  </p>
                </small>
              } @else {
                <small>
                  {{ 'ID NUMBER FIELD INFORMATION' | translate }}
                </small>
              }
            </div>

            <!-- Phone number -->
            <div class="max-w-sm">
              <label class="block text-sm font-medium mb-2"
                >{{ 'PHONE NUMBER' | translate }}<span class="mandatory">*</span></label
              >
              <input
                type="text"
                [(ngModel)]="candidate.phone"
                name="phone"
                placeholder=""
                required
                pattern="^[- +()0-9]+$"
                #phone="ngModel"
              />
              @if (
                (phone.errors?.['pattern'] && (phone.dirty || phone.touched)) ||
                (phone.invalid && (phone.dirty || phone.touched))
              ) {
                <small>
                  <p class="text-red-500">
                    {{ 'PLEASE ENTER A VALID PHONE NUMBER' | translate }}
                  </p>
                </small>
              } @else {
                <small>
                  {{ 'PHONE NUMBER FIELD INFORMATION' | translate }}
                </small>
              }
            </div>

            <!-- Email -->
            <div class="max-w-sm">
              <label class="block text-sm font-medium mb-2"
                >{{ 'EMAIL' | translate }}<span class="mandatory">*</span></label
              >
              <input
                type="text"
                [(ngModel)]="candidate.email"
                name="email"
                placeholder=""
                required
                pattern="^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+.[a-zA-Z]{2,}$"
                #email="ngModel"
              />
              @if (
                (email.errors?.['pattern'] && (email.dirty || email.touched)) ||
                (email.invalid && (email.dirty || email.touched))
              ) {
                <small>
                  <p class="text-red-500">
                    {{ 'PLEASE ENTER A VALID EMAIL' | translate }}
                  </p>
                </small>
              } @else {
                <small>
                  {{ 'EMAIL ADDRESS FIELD INFORMATION' | translate }}
                </small>
              }
            </div>
            <!-- Worker ID -->
            <div class="max-w-sm">
              <label class="block text-sm font-medium mb-2">{{
                'CANDIDATE WORKER ID' | translate
              }}</label>
              <input type="text" [(ngModel)]="candidate.workerId" name="workerId" placeholder="" />
              <small>
                {{ 'WORKER ID FIELD INFORMATION' | translate }}
              </small>
            </div>
            <!-- Marital status -->
            <div class="max-w-sm">
              <label class="block text-sm font-medium mb-2">{{
                'MARITAL STATUS' | translate
              }}</label>
              <!-- Dropdown menu: This should be a dropdown with 3 options: Not Married, Married 1 working, Married 2 working -->
              <p-dropdown
                styleClass="dropDown"
                [options]="maritalStatusOptions"
                [(ngModel)]="candidate.maritalStatus"
                placeholder=""
                name="maritalStatus"
                [required]="true"
              >
              </p-dropdown>
              <small>
                {{ 'MARITAL STATUS FIELD INFORMATION' | translate }}
              </small>
            </div>
            <!-- Number of dependents -->
            <div class="max-w-sm">
              <label class="block text-sm font-medium mb-2">{{
                'NUMBER OF DEPENDENTS' | translate
              }}</label>
              <input
                [(ngModel)]="candidate.dependentsNumber"
                name="dependentsNumber"
                placeholder="0"
                min="0"
                type="number"
                onkeypress="return (event.charCode == 8 || event.charCode == 0) ? null : event.charCode >= 48 && event.charCode <= 57"
              />
              <small>
                {{ 'NUMBER OF DEPENDENTS FIELD INFORMATION' | translate }}
              </small>
            </div>

            <button
              pButton
              class="bg-[#0056D2] text-white py-2 px-4 rounded-md mx-auto block mt-6 col-span-full font-bold"
              type="submit"
            >
              <span>{{ 'SAVE CANDIDATE' | translate }}</span>
            </button>
            @if (errorMessage) {
              <small class="text-red-500 mx-auto block col-span-full">
                {{ 'INVALID FORM' | translate }}
              </small>
            }
          </div>
        </form>
      </p-dialog>
    </div>
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

      .p-button:hover {
        background-color: darken(#0056d2, 10%);
        box-shadow: 0 6px 10px rgba(0, 0, 0, 0.2);
        transform: translateY(-2px);
      }

      .p-button:active {
        background-color: #462c9c;
        opacity: 0.9;
        transform: translateY(1px);
      }

      .p-button:focus {
        outline: none;
        box-shadow:
          0 0 0 2px rgba(255, 255, 255, 0.4),
          0 0 0 4px #0056d2;
      }

      label {
        display: block;
        color: #333;
        font-size: 1rem;
        font-weight: medium;
        margin-bottom: 0.5rem;
      }

      input {
        width: 100%;
        padding: 0.8rem 1rem;
        font-size: 1rem;
        border: 2px solid#F0F9FF;
        border-radius: 0.375rem;
        box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.1);
        transition:
          border-color 0.3s,
          box-shadow 0.3s;
      }

      input:hover {
        border-color: #ebfbf3;
      }

      input:focus {
        outline: none;
        border-color: #0056b3;
        box-shadow: 0 0 0 3px rgba(0, 86, 179, 0.5);
      }

      .dropDown {
        width: 100%;
        border-radius: 5px !important;
        border: 2px solid#F0F9FF;
        transition: border-color 0.2s ease-in-out !important;
      }

      .mandatory {
        padding: 1px;
      }
    }
  `,
})
export class NewCandidateButtonComponent implements OnInit {
  @Output() candidateSaved: EventEmitter<void> = new EventEmitter<void>();
  visible = false;
  newCandidateString!: string;
  maritalStatusOptions!: string[];
  @ViewChild('form') form!: NgForm;
  candidate: Candidate = {
    '@id': 0,
    id: 0,
    name: '',
    surname: '',
    idNumber: '',
    phone: '',
    email: '',
    workerId: '',
    maritalStatus: 0,
    dependentsNumber: 0,
    nJoynSet: [''],
    proposals: [0],
  };
  errorMessage = false;

  constructor(
    private t: TranslateService,
    private candidateService: CandidateService,
  ) {
    this.t.use(localStorage.getItem('lang') || 'en');

    this.t.get('NEW CANDIDATE').subscribe((newCandidateString) => {
      this.newCandidateString = newCandidateString;
    });

    const maritalStatusSingle$ = this.t.get('SINGLE');
    const maritalStatusMarriedOne$ = this.t.get('MARRIED, ONE INCOME');
    const maritalStatusMarriedTwo$ = this.t.get('MARRIED, TWO INCOME');

    forkJoin([maritalStatusSingle$, maritalStatusMarriedOne$, maritalStatusMarriedTwo$]).subscribe(
      ([maritalStatusSingle, maritalStatusMarriedOne, maritalStatusMarriedTwo]) => {
        this.maritalStatusOptions = [
          maritalStatusSingle,
          maritalStatusMarriedOne,
          maritalStatusMarriedTwo,
        ];
      },
    );
  }

  ngOnInit() {
    this.resetForm();
  }

  newCandidate() {
    this.visible = true;
    this.resetForm();
  }

  onSaveCandidate() {
    if (this.form.valid) {
      this.candidateService.createCandidate(this.candidate).subscribe({
        next: () => {
          this.visible = false;
          this.candidateSaved.emit();
        },
        error: (error) => {
          console.log('Error saving candidate: 0', error);
        },
      });
    } else {
      console.log('Invalid form');
      this.errorMessage = true;
    }
  }

  resetForm() {
    this.form?.reset();
    this.errorMessage = false;
    this.candidate = {
      '@id': 0,
      id: 0,
      name: '',
      surname: '',
      idNumber: '',
      phone: '',
      email: '',
      workerId: '',
      maritalStatus: 0,
      dependentsNumber: 0,
      nJoynSet: [''],
      proposals: [0],
    };
  }
}
