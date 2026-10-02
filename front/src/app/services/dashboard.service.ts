import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class DashboardService {
  constructor(private http: HttpClient) {}

  getSumary(): Observable<any> {
    return this.http.get<any>(`${environment.API_ENDPOINT}dashboard/summary`);
  }

  getProposalsByStatus(): Observable<any> {
    return this.http.get<any>(
      `${environment.API_ENDPOINT}dashboard/proposals/status`
    );
  }

  getProposalsByDate(): Observable<any> {
    return this.http.get<any>(
      `${environment.API_ENDPOINT}dashboard/proposals/createdat`
    );
  }
}
