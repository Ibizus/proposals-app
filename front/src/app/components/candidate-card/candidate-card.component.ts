import {
  Component,
  Input,
  OnInit,
  OnChanges,
  SimpleChanges,
} from '@angular/core';
import { Candidate } from '../../utils/types'; // Assuming you have a Candidate interface defined
import {
  TranslateModule,
  TranslateService,
  TranslateStore,
} from '@ngx-translate/core';

/**
 * This component represents a candidate card.
 * It displays basic information about a candidate.
 * @param {Candidate} candidate - The candidate object to display.
 */
@Component({
  selector: 'app-candidate-card',
  standalone: true,
  imports: [TranslateModule],
  providers: [TranslateService, TranslateStore],
  template: `
    <div
      class="max-w-4xl bg-white border border-[#CECECE] rounded-md p-4 mx-auto"
    >
      <div class="font-bold text-2xl">
        {{ candidate.name }} {{ candidate.surname }}
      </div>
      <div class="grid grid-cols-2 mt-2">
        <div>
          <div>
            <span class="font-semibold">{{ 'ID NUMBER' | translate }}:</span>
            {{ candidate.idNumber }}
          </div>
          <div>
            <span class="font-semibold">{{ 'PHONE NUMBER' | translate }}:</span>
            {{ candidate.phone }}
          </div>
          <div>
            <span class="font-semibold">{{ 'EMAIL' | translate }}:</span>
            {{ candidate.email }}
          </div>
        </div>
        <div>
          <div>
            <span class="font-semibold">{{ 'WORKER ID' | translate }}:</span>
            {{ candidate.workerId }}
          </div>
          <div>
            <span class="font-semibold"
              >{{ 'MARITAL STATUS' | translate }}:</span
            >
            {{ maritalStatus | translate }}
          </div>
          <div>
            <span class="font-semibold">{{
              'NUMBER OF DEPENDENTS' | translate
            }}</span>
            {{ candidate.dependentsNumber }}
          </div>
        </div>
      </div>
    </div>
  `,
  styles: ``,
})
export class CandidateCardComponent implements OnInit, OnChanges {
  @Input() candidate!: Candidate;
  maritalStatus!: string;

  constructor(private t: TranslateService) {
    this.t.use(localStorage.getItem('lang') || 'en');
  }

  ngOnInit(): void {
    this.updateMaritalStatus();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['candidate']) {
      this.updateMaritalStatus();
    }
  }

  private updateMaritalStatus(): void {
    const maritalNumber = this.candidate.maritalStatus;
    switch (maritalNumber) {
      case 0:
        this.t
          .stream('UNMARRIED')
          .subscribe((text) => (this.maritalStatus = text));
        break;
      case 1:
        this.t
          .stream('MARRIED ONE')
          .subscribe((text) => (this.maritalStatus = text));
        break;
      case 2:
        this.t
          .stream('MARRIED TWO')
          .subscribe((text) => (this.maritalStatus = text));
        break;
      default:
        this.maritalStatus = '';
    }
  }
}
