import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { HttpTestingController } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { provideTestDefaults } from '../../../testing/test-providers';
import { makeProposal, makeTaxes } from '../../../testing/fixtures';
import { environment } from '../../../environments/environment';

import { DetailedProposalComponent } from './detailed-proposal.component';

const API = environment.API_ENDPOINT;
const TAXES_URL = 'http://localhost:8080/v1/api/taxes/';
const PARAMETERS = [
  { name: 'iht', value: 10 },
  { name: 'spp', value: 5 },
  { name: 'scr', value: 1.5 },
];

describe('DetailedProposalComponent', () => {
  let component: DetailedProposalComponent;
  let fixture: ComponentFixture<DetailedProposalComponent>;
  let http: HttpTestingController;

  beforeEach(async () => {
    spyOn(console, 'log');
    await TestBed.configureTestingModule({
      imports: [DetailedProposalComponent],
      providers: [
        ...provideTestDefaults(),
        { provide: ActivatedRoute, useValue: { params: of({ id: '7' }) } },
      ],
    }).compileComponents();

    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(DetailedProposalComponent);
    component = fixture.componentInstance;

    // The constructor loads the proposal from the route id and the parameters;
    // loading the proposal then chains taxes and the salary calculations.
    http.expectOne(API + 'parameters').flush(PARAMETERS);
    http.expectOne(API + 'proposals/7').flush(
      makeProposal({
        statuses: [
          { id: 2, status: 1, message: 'second', createdAt: new Date('2024-03-01'), createdBy: '', modifiedAt: new Date('2024-03-01'), modifiedBy: '' },
          { id: 1, status: 0, message: 'first', createdAt: new Date('2024-01-01'), createdBy: '', modifiedAt: new Date('2024-01-01'), modifiedBy: '' },
        ],
      })
    );
    http.expectOne(TAXES_URL).flush([makeTaxes()]);
    http.expectOne(TAXES_URL + '?salary=1000&maritalStatus=0').flush(makeTaxes());
    http.expectOne(API + 'parameters').flush(PARAMETERS);

    fixture.detectChanges();
  });

  afterEach(() => http.verify());

  it('loads the proposal named by the route', () => {
    expect(component.proposalId).toBe(7);
    expect(component.proposal.role).toBe('Developer');
  });

  it('sorts statuses oldest first', () => {
    expect(component.proposal.statuses.map((s) => s.message)).toEqual(['first', 'second']);
  });

  it('stores the parameters it fetched', () => {
    expect([component.iht, component.spp, component.scr]).toEqual([10, 5, 1.5]);
  });

  it('exposes the monthly salary breakdown', () => {
    const monthly = new Map(component.monthlyList);
    expect(monthly.get('Gross Salary')).toBe(1000);
    expect(monthly.get('Estimated Net Salary')).toBeCloseTo(690, 6);
  });

  it('renders proposal and candidate details', () => {
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Developer');
    expect(text).toContain('Lisbon');
    expect(text).toContain('Ana Silva');
  });
});
