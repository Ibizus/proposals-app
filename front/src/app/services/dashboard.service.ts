import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

interface DashboardSummary {
  totalProposals?: number;
  totalCandidates?: number;
}

type ProposalsByStatus = Record<string, number>;
type ProposalsByDate = Record<string, number>;

@Injectable({
  providedIn: 'root',
})
export class DashboardService {
  constructor(private http: HttpClient) {}

  getSumary(): Observable<DashboardSummary> {
    return this.http.get<DashboardSummary>(`${environment.API_ENDPOINT}dashboard/summary`);
  }

  getProposalsByStatus(): Observable<ProposalsByStatus> {
    return this.http.get<ProposalsByStatus>(
      `${environment.API_ENDPOINT}dashboard/proposals/status`,
    );
  }

  getProposalsByDate(): Observable<ProposalsByDate> {
    return this.http.get<ProposalsByDate>(
      `${environment.API_ENDPOINT}dashboard/proposals/createdat`,
    );
  }
}
