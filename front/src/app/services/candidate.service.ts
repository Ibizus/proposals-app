/* eslint-disable @typescript-eslint/no-explicit-any -- legacy untyped API payloads */
import { HttpClient, HttpContext } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, map } from 'rxjs';
import { environment } from '../../environments/environment';
import { Candidate, Proposal } from '../utils/types';

@Injectable({
  providedIn: 'root',
})
export class CandidateService {
  private candidateSource = new BehaviorSubject(null);
  currentCandidate = this.candidateSource.asObservable();

  constructor(private http: HttpClient) {}

  changeCandidate(candidate: any) {
    this.candidateSource.next(candidate);
  }

  getCandidates(page: number, size: number, filter: string): Observable<any> {
    return this.http.get<any>(
      environment.API_ENDPOINT +
        'candidates/?page=' +
        page +
        '&size=' +
        Math.ceil(size) +
        (filter.length > 0 ? '&filter=' + filter : ''),
    );
  }

  getFilteredCandidates(filter: string) {
    console.log(filter);
    return this.http.get<any>(environment.API_ENDPOINT + 'candidates/?filter=' + filter);
  }

  deleteCandidate(id: number, context?: HttpContext): Observable<Candidate> {
    return this.http.delete<Candidate>(environment.API_ENDPOINT + 'candidates/' + id, { context });
  }

  newProposalCandidate(id: number, context?: HttpContext): Observable<Candidate> {
    return this.http
      .get<Candidate>(environment.API_ENDPOINT + 'candidates/' + id, { context })
      .pipe(
        map((data: Candidate) => {
          console.log(data);
          return data;
        }),
      );
  }

  modifyCandidate(candidate: Candidate): Observable<unknown> {
    return this.http.put(environment.API_ENDPOINT + 'candidates/' + candidate.id, candidate);
  }

  getCandidateById(candidateId: number, context?: HttpContext): Observable<Candidate> {
    return this.http.get<Candidate>(environment.API_ENDPOINT + 'candidates/' + candidateId, {
      context,
    });
  }

  createCandidate(candidate: Candidate): Observable<unknown> {
    return this.http.post(environment.API_ENDPOINT + 'candidates/', candidate);
  }

  getProposalsByCandidateId(candidateId: number): Observable<any> {
    console.log('Dentro de getPRoposalsByCandidateById Service');
    return this.http.get<Proposal[]>(
      environment.API_ENDPOINT + 'proposals?candidate=' + candidateId,
    );
  }
}
