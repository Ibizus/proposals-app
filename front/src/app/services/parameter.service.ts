import { HttpClient, HttpContext } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Parameter } from '../utils/types';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class ParameterService {
  constructor(private http: HttpClient) {}

  //HTTP METHODS:
  getParameters(): Observable<Parameter[]> {
    return this.http.get<Parameter[]>(environment.API_ENDPOINT + 'parameters');
  }

  modifyParameter(parameter: Parameter, context?: HttpContext): Observable<unknown> {
    return this.http.put(environment.API_ENDPOINT + 'parameters/' + parameter.id, parameter, {
      context,
    });
  }

  deleteParameter(parameter: Parameter, context?: HttpContext): Observable<Parameter> {
    return this.http.delete<Parameter>(environment.API_ENDPOINT + 'parameters/' + parameter.id, {
      context,
    });
  }
}
