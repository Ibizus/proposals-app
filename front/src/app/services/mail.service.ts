/* eslint-disable @typescript-eslint/no-explicit-any -- legacy untyped API payloads */
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Proposal } from '../utils/types';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class MailService {
  constructor(private http: HttpClient) {}

  notifyByEmail(proposal: Proposal): Observable<any> {
    const language = localStorage.getItem('lang') || 'en';
    return this.http.post<any>(
      `${environment.API_ENDPOINT}send/candidate/?language=${language}`,
      proposal,
    );
  }

  newEmail(email: string, subject: string, message: string, proposalId: number): Observable<any> {
    const language = localStorage.getItem('lang') || 'en';
    return this.http.post<any>(
      `${environment.API_ENDPOINT}send/to/?to=${encodeURIComponent(email)}&subject=${encodeURIComponent(subject)}&proposalId=${proposalId}&language=${language}`,
      message,
    );
  }
}
