import { Injectable } from '@angular/core';
import { HttpClient, HttpContext } from '@angular/common/http';
import { Taxes } from '../utils/types';
import { Observable, map } from 'rxjs';

const TAXESURL = 'http://localhost:8080/v1/api/taxes/';

@Injectable({
  providedIn: 'root',
})
export class TaxesService {
  constructor(private http: HttpClient) {}

  getTaxes(): Observable<Taxes[]> {
    return this.http.get<Taxes[]>(TAXESURL).pipe(
      map((data) => {
        data.sort((a, b) => a.salaryMax - b.salaryMax);
        return data;
      })
    );
  }

  getTaxesForSalaryAndMaritalStatus(
    salary: number,
    maritalStatus: number
  ): Observable<Taxes> {
    return this.http
      .get<Taxes>(
        TAXESURL + '?salary=' + salary + '&maritalStatus=' + maritalStatus
      )
      .pipe(
        map((data) => {
          return data;
        })
      );
  }

  getTaxesForMaritalStatus(maritalStatus: number): Observable<Taxes[]> {
    return this.http
      .get<Taxes[]>(TAXESURL + '?maritalStatus=' + maritalStatus)
      .pipe(
        map((data) => {
          data.sort((a, b) => a.salaryMax - b.salaryMax);
          return data;
        })
      );
  }

  getTaxBracket(
    base: number,
    maritalStatus: number,
    numberOfDependents: number
  ): Observable<any> {
    return new Observable<any>((observer) => {
      this.getTaxesForSalaryAndMaritalStatus(base, maritalStatus).subscribe({
        next: (data) => {
          const tax = data;
          const taxBracket = {
            irs: tax.zero,
            ss: tax.socialSecurityTax,
          };

          if (numberOfDependents <= 0) {
            taxBracket.irs = tax.zero;
          } else if (numberOfDependents === 1) {
            taxBracket.irs = tax.one;
          } else if (numberOfDependents === 2) {
            taxBracket.irs = tax.two;
          } else if (numberOfDependents === 3) {
            taxBracket.irs = tax.three;
          } else if (numberOfDependents === 4) {
            taxBracket.irs = tax.four;
          } else {
            taxBracket.irs = tax.five;
          }

          console.log(
            'Tax bracket calculated inside Taxes Service: ' +
              JSON.stringify(taxBracket)
          );
          observer.next(taxBracket);
          observer.complete();
        },
        error: (error) => {
          console.error('Error while fetching extras:', error);
          observer.error(error);
        },
      });
    });
  }

  modifyTaxes(tax: Taxes, context?: HttpContext): Observable<Object> {
    return this.http.put<Taxes>(TAXESURL + tax.id, tax, { context });
  }

  deleteTaxes(id: number, context?: HttpContext): Observable<Taxes> {
    return this.http.delete<Taxes>(TAXESURL + id, { context });
  }
}
