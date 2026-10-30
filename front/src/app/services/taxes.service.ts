/* eslint-disable @typescript-eslint/no-explicit-any -- legacy untyped API payloads */
import { Injectable } from '@angular/core';
import { HttpClient, HttpContext } from '@angular/common/http';
import { TaxConfiguration, Taxes } from '../utils/types';
import { Observable, map } from 'rxjs';
import { environment } from '../../environments/environment';

const TAX_CONFIG_URL = `${environment.API_ENDPOINT}tax-configurations/`;

@Injectable({
  providedIn: 'root',
})
export class TaxesService {
  constructor(private http: HttpClient) {}

  getTaxes(): Observable<Taxes[]> {
    return this.getActiveConfiguration().pipe(
      map((config) => {
        const incomeTax = config.taxes.find((t) => t.type === 'BRACKETED');
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

  activateConfiguration(id: number): Observable<TaxConfiguration> {
    return this.http.post<TaxConfiguration>(`${TAX_CONFIG_URL}${id}/activate`, {});
  }

  getTaxesForSalaryAndMaritalStatus(salary: number, maritalStatus: number): Observable<Taxes> {
    return this.getTaxes().pipe(
      map((taxes) => {
        const sorted = [...taxes].sort((a, b) => a.salaryMax - b.salaryMax);
        return (
          sorted.find((t) => salary <= t.salaryMax) ?? sorted[sorted.length - 1]
        );
      }),
    );
  }

  getTaxesForMaritalStatus(maritalStatus: number): Observable<Taxes[]> {
    return this.getTaxes();
  }

  getTaxBracket(base: number, maritalStatus: number, numberOfDependents: number): Observable<any> {
    return this.getActiveConfiguration().pipe(
      map((config) => {
        const ssTax = config.taxes.find((t) => t.type === 'FIXED');
        const incomeTax = config.taxes.find((t) => t.type === 'BRACKETED');

        let irs = 0;
        if (incomeTax) {
          const sorted = [...incomeTax.brackets].sort((a, b) => a.threshold - b.threshold);
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
          irs = (tax / base) * 100;
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

  modifyTaxes(tax: Taxes, context?: HttpContext): Observable<unknown> {
    return this.http.put<Taxes>(TAX_CONFIG_URL + tax.id, tax, { context });
  }

  deleteTaxes(id: number, context?: HttpContext): Observable<Taxes> {
    return this.http.delete<Taxes>(TAX_CONFIG_URL + id, { context });
  }
}
