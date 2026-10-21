import { Injectable } from '@angular/core';
import { HttpClient, HttpContext } from '@angular/common/http';
import { Extra } from '../utils/types';
import { Observable, map } from 'rxjs';
import { environment } from '../../environments/environment';

const EXTRASURL = environment.API_ENDPOINT;

@Injectable({
  providedIn: 'root',
})
export class ExtrasService {
  constructor(private http: HttpClient) {}

  /**
   * Method that retrieves Extras from API
   * @returns Extras[] (data)
   */
  getExtras(page: number, size: number): Observable<any> {
    return this.http
      .get<any>(
        EXTRASURL + 'extratemplates/?page=' + page + '&size=' + Math.ceil(size)
      )
      .pipe(
        map((data) => {
          console.log(data);
          return data;
        })
      );
  }

  duplicateExtra(extra: Extra): Observable<any> {
    let newExtra = { ...extra };
    newExtra.id = 0;
    return this.http.post<Extra>(EXTRASURL + 'extratemplates/', newExtra);
  }

  updateExtra(modifiedExtra: Extra, id: number): Observable<any> {
    return this.http.put<Extra>(
      EXTRASURL + 'extratemplates/' + id,
      modifiedExtra
    );
  }

  deleteExtra(extraId: any, context?: HttpContext): Observable<any> {
    return this.http.delete<Extra>(EXTRASURL + 'extratemplates/' + extraId, { context });
  }

  createExtra(extra: Extra): Observable<Object> {
    return this.http.post(environment.API_ENDPOINT + 'extratemplates/', extra);
  }
}
