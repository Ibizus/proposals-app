import { Component, signal, OnInit } from '@angular/core';
import { Taxes, Column, TaxConfiguration } from '../../utils/types';
import { TableModule } from 'primeng/table';
import { TooltipModule } from 'primeng/tooltip';
import { TaxesService } from '../../services/taxes.service';
import { TranslateModule, TranslateService, TranslateStore } from '@ngx-translate/core';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-taxes',
  standalone: true,
  imports: [TableModule, TooltipModule, TranslateModule],
  templateUrl: './taxes.component.html',
  styleUrl: './taxes.component.scss',
  providers: [TranslateService, TranslateStore],
})
export class TaxesComponent implements OnInit {
  taxesTable!: Taxes[];
  activeConfiguration!: TaxConfiguration;
  cols!: Column[];
  isLoading = signal(true);
  moneySign = '€';

  constructor(
    private taxesService: TaxesService,
    private t: TranslateService,
  ) {
    this.t.use(localStorage.getItem('lang') || 'en');

    const salaryMax$ = this.t.get('SALARY UP TO');
    const socialSecurityTax$ = this.t.get('SOCIAL SECURITY TAX');
    const validFrom$ = this.t.get('VALID FROM');
    const validUntil$ = this.t.get('VALID UNTIL');
    const zero$ = this.t.get('ZERO');

    forkJoin([
      salaryMax$,
      socialSecurityTax$,
      validFrom$,
      validUntil$,
      zero$,
    ]).subscribe(([salaryMax, socialSecurityTax, validFrom, validUntil, zero]) => {
      this.cols = [
        { field: 'salaryMax', header: salaryMax },
        { field: 'socialSecurityTax', header: socialSecurityTax },
        { field: 'zero', header: zero },
        { field: 'validFrom', header: validFrom },
        { field: 'validUntil', header: validUntil },
      ];
    });
  }

  ngOnInit(): void {
    this.getTaxes();
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
}
