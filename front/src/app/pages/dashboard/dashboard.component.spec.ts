import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpTestingController } from '@angular/common/http/testing';
import { provideTestDefaults } from '../../../testing/test-providers';
import { environment } from '../../../environments/environment';

import { DashboardComponent } from './dashboard.component';

const API = environment.API_ENDPOINT;

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: provideTestDefaults(),
    }).compileComponents();

    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => http.verify());

  function flushCharts(): void {
    http.expectOne(API + 'dashboard/proposals/status').flush({ DRAFT: 3, SENT: 9 });
    http.expectOne(API + 'dashboard/proposals/createdat').flush({ '2024-01': 4, '2024-02': 8 });
    fixture.detectChanges();
  }

  function flushAll(): void {
    http.expectOne(API + 'dashboard/summary').flush({ totalProposals: 12, totalCandidates: 5 });
    flushCharts();
  }

  it('renders the summary totals', () => {
    flushAll();
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('12');
    expect(text).toContain('5');
  });

  it('does not show zero totals when the summary request fails', () => {
    spyOn(console, 'error');
    http
      .expectOne(API + 'dashboard/summary')
      .flush('nope', { status: 500, statusText: 'Server Error' });
    flushCharts();

    expect(component.isLoading()).toBeFalse();
    expect(component.totalProposals()).toBeUndefined();
    expect(component.totalCandidates()).toBeUndefined();
    const totals = (fixture.nativeElement as HTMLElement).querySelectorAll('span.font-bold');
    expect(Array.from(totals).map((el) => el.textContent?.trim())).toEqual(['', '']);
  });

  it('builds the proposals-by-status chart from the API response', () => {
    flushAll();
    expect(component.proposalsByStatusChartData.labels).toEqual(['DRAFT', 'SENT']);
    expect(component.proposalsByStatusChartData.datasets[0].data).toEqual([3, 9]);
  });

  it('builds the proposals-by-date chart from the API response', () => {
    flushAll();
    expect(component.proposalsByDateChartData.labels).toEqual(['2024-01', '2024-02']);
    expect(component.proposalsByDateChartData.datasets[0].data).toEqual([4, 8]);
  });
});
