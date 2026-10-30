/* eslint-disable @typescript-eslint/no-explicit-any -- legacy untyped API payloads */
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Tax, TaxConfiguration, Taxes } from '../utils/types';
import { Observable, map, of } from 'rxjs';
import { environment } from '../../environments/environment';
import { isIncomeTax, isSocialSecurityTax } from '../utils/functions';

const TAX_CONFIG_URL = `${environment.API_ENDPOINT}tax-configurations/`;

@Injectable({
  providedIn: 'root',
})
export class TaxesService {
  constructor(private http: HttpClient) {}

  private effectiveRate(tax: Tax, base: number): number {
    if (tax.type === 'FIXED') {
      return tax.fixedRate ?? 0;
    }
    const sorted = [...tax.brackets].sort((a, b) => a.threshold - b.threshold);
    if (tax.type === 'WITHHOLDING') {
      const bracket = sorted.find((b) => base <= b.threshold) ?? sorted[sorted.length - 1];
      return bracket?.rate ?? 0;
    }
    let remaining = base;
    let previousThreshold = 0;
    let total = 0;
    for (const bracket of sorted) {
      const size = bracket.threshold - previousThreshold;
      const taxable = Math.min(remaining, Math.max(0, size));
      total += (taxable * bracket.rate) / 100;
      remaining -= taxable;
      if (remaining <= 0) break;
      previousThreshold = bracket.threshold;
    }
    return base > 0 ? (total / base) * 100 : 0;
  }

  getTaxes(): Observable<Taxes[]> {
    return this.getActiveConfiguration().pipe(
      map((config) => {
        const incomeTax = config.taxes.find((t) => isIncomeTax(t));
        if (!incomeTax) return [];
        return [...incomeTax.brackets]
          .sort((a, b) => a.threshold - b.threshold)
          .map((bracket): Taxes => ({
            id: bracket.id,
            salaryMax: bracket.threshold,
            socialSecurityTax: config.taxes.find((t) => isSocialSecurityTax(t))?.fixedRate ?? 0,
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
          }));
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
        let irs = 0;
        let ss = 0;
        let other = 0;
        for (const tax of config.taxes.filter((t) => !t.extrasOnly)) {
          const rate = this.effectiveRate(tax, base);
          if (isIncomeTax(tax)) {
            irs = rate;
          } else if (isSocialSecurityTax(tax)) {
            ss = rate;
          } else {
            other += rate;
          }
        }

        const taxBracket = {
          irs: irs,
          ss: ss,
          other: other,
        };
        console.log('Tax bracket calculated inside Taxes Service: ' + JSON.stringify(taxBracket));
        return taxBracket;
      }),
    );
  }
}
