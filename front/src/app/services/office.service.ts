import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Office } from '../utils/types';
import { AutoCompleteCompleteEvent } from 'primeng/autocomplete';
import { Observable, map } from 'rxjs';
import { environment } from '../../environments/environment';

const OFFICEURL = environment.API_ENDPOINT;

@Injectable({
  providedIn: 'root',
})
export class OfficeService {
  constructor(private http: HttpClient) {}

  /**
   * Method that retrieves Offices from API
   * @returns Office[] (data)
   */
  getOffices(event: AutoCompleteCompleteEvent): Observable<Office[]> {
    return this.http
      .get<Office[]>(OFFICEURL + 'offices/', { params: { filter: event.query } })
      .pipe(
        map((data) => {
          data.sort();
          return data;
        }),
      );
  }
}
