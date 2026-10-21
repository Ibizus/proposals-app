import { Injectable } from '@angular/core';
import { HttpClient, HttpContext } from '@angular/common/http';
import { BehaviorSubject, map, Observable, switchMap } from 'rxjs';
import { environment } from '../../environments/environment';
import { Extra, Proposal, Taxes } from '../utils/types';
import { TaxesService } from './taxes.service';
import { MailService } from './mail.service';
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
    private mailService: MailService,
    private salaryService: SalaryService
  ) {}

  changeProposal(proposal: any) {
    this.proposalSource.next(proposal);
  }

  getParameters(): Observable<Map<string, number>> {
    return new Observable<Map<string, number>>((observer) => {
      let parameters = new Map<string, number>();
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
        }
      );
    });
  }

  renderPdf(proposalId: number, action: string): Observable<string> {
    return new Observable<string>((observer) => {
      let createdFileName!: string;
      // First get the specified proposal:
      this.http
        .get<Proposal>(environment.API_ENDPOINT + 'proposals/' + proposalId)
        .subscribe({
          next: (proposal) => {
            console.log('proposal found: ' + proposal);

            // Once we´ve got proposal, make calculations:
            this.salaryCalculations(proposal).subscribe((dataMap) => {
              const monthlySalaryCalculations: Map<string, number> =
                dataMap.get('monthly') ?? new Map<string, number>();
              const yearlySalaryCalculations: Map<string, number> =
                dataMap.get('yearly') ?? new Map<string, number>();
              const body: any = this.mailService.createPDFDataBody(
                proposal,
                Array.from(monthlySalaryCalculations),
                Array.from(yearlySalaryCalculations)
              );

              // Once Calculations succeed, post PDF renderization
              this.http
                .post<any>(environment.API_ENDPOINT + 'pdf/' + action, body)
                .subscribe((fileName) => {
                  console.log('Post done');
                  console.log('File name received:', fileName);
                  createdFileName = fileName;
                  observer.next(createdFileName);
                  observer.complete();
                });
            });
          },
          error: (error) => {
            console.error('Error while posting PDF creation: ', error);
          },
        });
    });
  }

  sendTemplate(proposalId: number): Observable<string> {
    return new Observable<string>((observer) => {
      // First get the specified proposal:
      this.http
        .get<Proposal>(environment.API_ENDPOINT + 'proposals/' + proposalId)
        .subscribe({
          next: (proposal) => {
            console.log('proposal found: ' + proposal);

            // Once we´ve got proposal, make calculations:
            this.salaryCalculations(proposal).subscribe((dataMap) => {
              const monthlySalaryCalculations: Map<string, number> =
                dataMap.get('monthly') ?? new Map<string, number>();
              const yearlySalaryCalculations: Map<string, number> =
                dataMap.get('yearly') ?? new Map<string, number>();
              const body: any = this.mailService.createPDFDataBody(
                proposal,
                Array.from(monthlySalaryCalculations),
                Array.from(yearlySalaryCalculations)
              );

              // Once Calculations succeed, send data to back to create email:
              this.http
                .post<any>(environment.API_ENDPOINT + 'send/template', body)
                .subscribe(() => {
                  console.log('Post done to fill template');
                  observer.next();
                  observer.complete();
                });
            });
          },
          error: (error) => {
            console.error('Error while posting template creation: ', error);
          },
        });
    });
  }

  salaryCalculations(
    proposal: Proposal,
    irs?: number,
    ss?: number
  ): Observable<Map<string, Map<string, number>>> {
    const calculations = new Map<string, Map<string, number>>();
    const monthlySalaryCalculations = new Map<string, number>();
    const yearlySalaryCalculations = new Map<string, number>();
    const salary = proposal.salary.base;
  
    let monthlyNetSalary = 0;
    let yearlyNetSalary = 0;
  
    return this.taxesService
      .getTaxBracket(
        salary,
        proposal.candidate.maritalStatus,
        proposal.candidate.dependentsNumber
      )
      .pipe(
        switchMap((taxBracket) => {
          if (irs) {
            taxBracket.irs = irs;
          }
          if (ss) {
            taxBracket.ss = ss;
          }
          return this.getParameters().pipe(
            map((parameters) => {
              const iht = parameters.get('iht') ?? 0;
              const spp = parameters.get('spp') ?? 0;
              const scr = parameters.get('scr') ?? 0;
  
              this.salaryService.setBaseSalary(monthlySalaryCalculations, yearlySalaryCalculations, salary, iht, scr);
              this.salaryService.setIHT(monthlySalaryCalculations, yearlySalaryCalculations, salary, iht);
              this.salaryService.setSharePurchasePlan(monthlySalaryCalculations, yearlySalaryCalculations, salary, spp);
              this.salaryService.setGrossSalary(monthlySalaryCalculations, yearlySalaryCalculations, salary);
              this.salaryService.setNetSalary(
                proposal,
                monthlySalaryCalculations,
                yearlySalaryCalculations,
                salary,
                taxBracket,
                monthlyNetSalary,
                yearlyNetSalary,
              );
              
              this.scrValue = this.salaryService.calculateSCR(monthlySalaryCalculations, scr);

              calculations.set('monthly', monthlySalaryCalculations);
              calculations.set('yearly', yearlySalaryCalculations);
              return calculations;
            })
          );
        })
      );
  }

  getScrValue(): Number {
    return this.scrValue; 
  }

  getProposals(page: number, size: number, filter: string): Observable<any> {
    return this.http.get<any>(
      environment.API_ENDPOINT +
        'proposals/?page=' +
        page +
        '&size=' +
        Math.ceil(size) +
        (filter.length > 0 ? '&filter=' + filter : '')
    );
  }

  getProposal(id: number, context?: HttpContext): Observable<Proposal> {
    return this.http.get<Proposal>(
      environment.API_ENDPOINT + 'proposals/' + id,
      { context }
    );
  }

  deleteProposal(id: number, context?: HttpContext): Observable<Proposal> {
    return this.http.delete<Proposal>(
      environment.API_ENDPOINT + 'proposals/' + id,
      { context }
    );
  }

  modifyProposal(proposal: Proposal, context?: HttpContext): Observable<Object> {
    return this.http.put(
      environment.API_ENDPOINT + 'proposals/' + proposal.id,
      proposal,
      { context }
    );
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

   getProposalsByCandidateId(candidateId: number): Observable<any>{
    return this.http.get<Proposal[]>(environment.API_ENDPOINT + 'proposals/' + candidateId);
  }
}
