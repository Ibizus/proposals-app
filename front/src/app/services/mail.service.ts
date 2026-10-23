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

  createPDFDataBody(
    proposal: Proposal,
    monthly: [string, number][],
    yearly: [string, number][],
  ): any {
    return {
      proposal: proposal,
      monthly: monthly,
      yearly: yearly,
      language: localStorage.getItem('lang') || 'en',
    };
  }

  notifyByEmail(proposal: Proposal): Observable<any> {
    return new Observable<any>((observer) => {
      this.http
        .post<any>(
          environment.API_ENDPOINT +
            'send/candidate/?language=' +
            (localStorage.getItem('lang') || 'en'),
          proposal,
        )
        .subscribe({
          next: () => {
            console.log('Posting email to backend');
          },
          error: () => {
            console.log('Could not POST mail');
          },
        });
      observer.next();
      observer.complete();
    });
  }

  newEmail(email: string, subject: string, message: string, proposalId: number): Observable<any> {
    return new Observable<any>((observer) => {
      this.http
        .post<any>(
          environment.API_ENDPOINT +
            'send/to/?to=' +
            email +
            '&subject=' +
            subject +
            '&proposalId=' +
            proposalId,
          message,
        )
        .subscribe({
          next: () => {
            console.log('Posting email to backend');
          },
          error: () => {
            console.log('Could not POST mail');
          },
        });
      observer.next();
      observer.complete();
    });
  }
}
