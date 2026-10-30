/* eslint-disable @typescript-eslint/no-explicit-any -- legacy untyped API payloads */
import { Injectable } from '@angular/core';
import { HttpClient, HttpContext } from '@angular/common/http';
import { BehaviorSubject, map, Observable, of, switchMap } from 'rxjs';
import { environment } from '../../environments/environment';
import { Proposal, TaxBracket, TaxConfiguration } from '../utils/types';
import { TaxesService } from './taxes.service';
import { SalaryService } from './salary.service';

@Injectable({
  providedIn: 'root',
})
export class ProposalService {
  scrValue!: number;
  private proposalSource = new BehaviorSubject(null);
  currentProposal = this.proposalSource.asObservable();

  constructor(
    private http: HttpClient,
    private taxesService: TaxesService,
    private salaryService: SalaryService,
  ) {}

  changeProposal(proposal: any) {
    this.proposalSource.next(proposal);
  }

  getParameters(): Observable<Map<string, number>> {
    return new Observable<Map<string, number>>((observer) => {
      const parameters = new Map<string, number>();
      this.http.get<any[]>(environment.API_ENDPOINT + 'parameters').subscribe(
        (data) => {
          data.forEach((p) => {
            if (p.name === 'iht') {
              parameters.set(p.name, p.value);
            } else if (p.name === 'spp') {
              parameters.set(p.name, p.value);
            } else if (p.name === 'scr') {
              parameters.set(p.name, p.value);
            }
          });
          observer.next(parameters);
          observer.complete();
        },
        (error) => {
          console.error('Error while fetching parameters: ', error);
          observer.error(error);
        },
      );
    });
  }

  salaryCalculations(
    proposal: Proposal,
    irs?: number,
    ss?: number,
  ): Observable<Map<string, Map<string, number>>> {
    const calculations = new Map<string, Map<string, number>>();
    const monthlySalaryCalculations = new Map<string, number>();
    const yearlySalaryCalculations = new Map<string, number>();
    const salary = proposal.salary.base;

    const monthlyNetSalary = 0;
    const yearlyNetSalary = 0;

    const configuration$ = proposal.salary.taxConfigurationId
      ? this.taxesService.getTaxConfiguration(proposal.salary.taxConfigurationId)
      : this.taxesService.getActiveConfiguration();

    return configuration$.pipe(
      switchMap((configuration) =>
        this.taxesService
          .getTaxBracket(salary, proposal.candidate.maritalStatus, proposal.candidate.dependentsNumber, configuration)
          .pipe(
          switchMap((taxBracket) => {
            if (irs != null) {
              taxBracket.irs = irs;
            }
            if (ss != null) {
              taxBracket.ss = ss;
            }
            return this.getParameters().pipe(
              map((parameters) => {
                const iht = parameters.get('iht') ?? 0;
                const spp = parameters.get('spp') ?? 0;
                const scr = parameters.get('scr') ?? 0;
                const payments = configuration.yearlyPayments;

                this.salaryService.setBaseSalary(
                  monthlySalaryCalculations,
                  yearlySalaryCalculations,
                  salary,
                  iht,
                  scr,
                  payments,
                );
                this.salaryService.setIHT(
                  monthlySalaryCalculations,
                  yearlySalaryCalculations,
                  salary,
                  iht,
                  payments,
                );
                this.salaryService.setSharePurchasePlan(
                  monthlySalaryCalculations,
                  yearlySalaryCalculations,
                  salary,
                  spp,
                  payments,
                );
                this.salaryService.setGrossSalary(
                  monthlySalaryCalculations,
                  yearlySalaryCalculations,
                  salary,
                  payments,
                );
                this.salaryService.setNetSalary(
                  proposal,
                  monthlySalaryCalculations,
                  yearlySalaryCalculations,
                  salary,
                  taxBracket,
                  monthlyNetSalary,
                  yearlyNetSalary,
                  payments,
                  configuration,
                  { irs, ss },
                );

                this.scrValue = this.salaryService.calculateSCR(monthlySalaryCalculations, scr);

                calculations.set('monthly', monthlySalaryCalculations);
                calculations.set('yearly', yearlySalaryCalculations);
                return calculations;
              }),
            );
          }),
        ),
      ),
    );
  }

  getScrValue(): number {
    return this.scrValue;
  }

  getProposals(page: number, size: number, filter: string): Observable<any> {
    return this.http.get<any>(
      environment.API_ENDPOINT +
        'proposals/?page=' +
        page +
        '&size=' +
        Math.ceil(size) +
        (filter.length > 0 ? '&filter=' + filter : ''),
    );
  }

  getProposal(id: number, context?: HttpContext): Observable<Proposal> {
    return this.http.get<Proposal>(environment.API_ENDPOINT + 'proposals/' + id, { context });
  }

  deleteProposal(id: number, context?: HttpContext): Observable<Proposal> {
    return this.http.delete<Proposal>(environment.API_ENDPOINT + 'proposals/' + id, { context });
  }

  modifyProposal(proposal: Proposal, context?: HttpContext): Observable<unknown> {
    return this.http.put(environment.API_ENDPOINT + 'proposals/' + proposal.id, proposal, {
      context,
    });
  }

  createProposal(proposal: Proposal, context?: HttpContext): Observable<any> {
    return this.http.post(environment.API_ENDPOINT + 'proposals/', proposal, { context });
  }

  // duplicateProposal(newProposal: Proposal): Observable<any>{
  //   // let newProposal = {...proposal};
  //   // newProposal.id=0;
  //   console.log(`Dentro de duplicateProposal Service, vamos a copiar el objet0 ${JSON.stringify(newProposal)}`);
  //   return this.http.post<Proposal>(environment.API_ENDPOINT + 'proposals/', newProposal);
  // }

  getProposalsByCandidateId(candidateId: number): Observable<any> {
    return this.http.get<Proposal[]>(environment.API_ENDPOINT + 'proposals/' + candidateId);
  }
}
