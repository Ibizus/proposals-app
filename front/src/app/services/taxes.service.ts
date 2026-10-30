/* eslint-disable @typescript-eslint/no-explicit-any -- legacy untyped API payloads */
import { Injectable } from '@angular/core';
import { HttpClient, HttpContext } from '@angular/common/http';
import { TaxConfiguration, Taxes } from '../utils/types';
import { Observable, map, of } from 'rxjs';
import { environment } from '../../environments/environment';

const TAX_CONFIG_URL = `${environment.API_ENDPOINT}tax-configurations/`;

@Injectable({
  providedIn: 'root',
})
export class TaxesService {
  constructor(private http: HttpClient) {}

  private isIncomeTax(tax: any): boolean {
    return tax.type === 'BRACKETED' || tax.type === 'WITHHOLDING';
  }

  getTaxes(): Observable<Taxes[]> {
    return this.getActiveConfiguration().pipe(
      map((config) => {
        const incomeTax = config.taxes.find((t) => this.isIncomeTax(t));
        if (!incomeTax) return [];
        return [...incomeTax.brackets]
          .sort((a, b) => a.threshold - b.threshold)
          .map(
            (bracket): Taxes => ({
              id: bracket.id,
              salaryMax: bracket.threshold,
              socialSecurityTax:
                config.taxes.find((t) => t.type === 'FIXED')?.fixedRate ?? 0,
              taxPercentageCompany: 0,
              validFrom: new Date(),
              validUntil: new Date(),
              maritalStatus: 0,
              zero: bracket.rate,
              one: bracket.rate,
              two: bracket.rate,
              three: bracket.rate,
              four: bracket.rate,
              five: bracket.rate,
              createdAt: new Date(),
              createdBy: '',
              modifiedAt: new Date(),
              modifiedBy: '',
            }),
          );
      }),
    );
  }

  getTaxConfigurations(): Observable<TaxConfiguration[]> {
    return this.http.get<TaxConfiguration[]>(TAX_CONFIG_URL).pipe(
      map((data) => {
        data.sort((a, b) => a.countryCode.localeCompare(b.countryCode));
        return data;
      }),
    );
  }

  getActiveConfiguration(): Observable<TaxConfiguration> {
    return this.http.get<TaxConfiguration>(`${TAX_CONFIG_URL}active`);
  }

  getTaxConfiguration(id: number): Observable<TaxConfiguration> {
    return this.http.get<TaxConfiguration>(`${TAX_CONFIG_URL}${id}`);
  }

  activateConfiguration(id: number): Observable<TaxConfiguration> {
    return this.http.post<TaxConfiguration>(`${TAX_CONFIG_URL}${id}/activate`, {});
  }

  getTaxBracket(
    base: number,
    maritalStatus: number,
    numberOfDependents: number,
    configuration?: TaxConfiguration,
  ): Observable<any> {
    const config$ = configuration ? of(configuration) : this.getActiveConfiguration();
    return config$.pipe(
      map((config) => {
        const ssTax = config.taxes.find((t) => t.type === 'FIXED');
        const incomeTax = config.taxes.find((t) => this.isIncomeTax(t));

        let irs = 0;
        if (incomeTax) {
          const sorted = [...incomeTax.brackets].sort((a, b) => a.threshold - b.threshold);
          if (incomeTax.type === 'WITHHOLDING') {
            const bracket = sorted.find((b) => base <= b.threshold) ?? sorted[sorted.length - 1];
            irs = bracket?.rate ?? 0;
          } else {
            let remaining = base;
            let previousThreshold = 0;
            let tax = 0;
            for (const bracket of sorted) {
              const size = bracket.threshold - previousThreshold;
              const taxable = Math.min(remaining, Math.max(0, size));
              tax += (taxable * bracket.rate) / 100;
              remaining -= taxable;
              if (remaining <= 0) break;
              previousThreshold = bracket.threshold;
            }
            irs = base > 0 ? (tax / base) * 100 : 0;
          }
        }

        const taxBracket = {
          irs: irs,
          ss: ssTax?.fixedRate ?? 0,
        };
        console.log('Tax bracket calculated inside Taxes Service: ' + JSON.stringify(taxBracket));
        return taxBracket;
      }),
    );
  }

}
