import { Component, OnInit, computed, signal } from '@angular/core';
import { TranslateModule, TranslateService, TranslateStore } from '@ngx-translate/core';
import { ChartModule } from 'primeng/chart';
import { SkeletonModule } from 'primeng/skeleton';
import { DashboardService } from '../../services/dashboard.service';

interface DashboardChartDataset {
  label?: string | null;
  data: number[];
  backgroundColor?: string[];
  borderColor?: string | string[];
  borderWidth?: number;
  fill?: boolean;
  tension?: number;
}

interface DashboardChartData {
  labels: string[];
  datasets: DashboardChartDataset[];
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [TranslateModule, ChartModule, SkeletonModule],
  providers: [TranslateService, TranslateStore],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent implements OnInit {
  // Dashboard variables
  summary = signal<{ totalProposals?: number; totalCandidates?: number }>({});
  totalProposals = computed(() => this.summary().totalProposals ?? 0);
  totalCandidates = computed(() => this.summary().totalCandidates ?? 0);
  proposalsByStatusChartData!: DashboardChartData;
  proposalsByDateChartData!: DashboardChartData;
  barOptions: unknown;
  lineOptions: unknown;
  statuses: Record<string, number> = {};
  isLoading = signal(true);

  private pendingLoads = 0;
  private readonly totalLoads = 3;

  private markLoaded(): void {
    this.pendingLoads += 1;
    if (this.pendingLoads === this.totalLoads) {
      this.isLoading.set(false);
    }
  }

  constructor(
    private dashboardService: DashboardService,
    private t: TranslateService,
  ) {
    this.t.use(localStorage.getItem('lang') || 'en');
    const documentStyle = getComputedStyle(document.documentElement);
    const textColorSecondary = documentStyle.getPropertyValue('--text-color-secondary');
    const surfaceBorder = documentStyle.getPropertyValue('--surface-border');

    // Chart options setup. Cehck chartjs documentation for details
    // https://www.chartjs.org/docs/latest/configuration/legend.html
    this.barOptions = {
      responsive: true,
      maintainAspectRatio: false, // Makes the chart fill the container
      plugins: {
        legend: {
          display: false,
        },
      },
      scales: {
        x: {
          ticks: {
            color: textColorSecondary,
          },
          grid: {
            color: surfaceBorder,
            drawBorder: false,
          },
        },
        y: {
          ticks: {
            color: textColorSecondary,
          },
          grid: {
            color: surfaceBorder,
            drawBorder: false,
          },
        },
      },
    };

    this.lineOptions = {
      responsive: true,
      maintainAspectRatio: false, // Makes the chart fill the container
      plugins: {
        legend: {
          display: false,
        },
      },
      scales: {
        x: {
          ticks: {
            color: textColorSecondary,
          },
          grid: {
            color: surfaceBorder,
            drawBorder: false,
          },
        },
        y: {
          ticks: {
            color: textColorSecondary,
          },
          grid: {
            color: surfaceBorder,
            drawBorder: false,
          },
        },
      },
    };
  }

  ngOnInit(): void {
    // Fetch all dashboard data
    // Look at DashboardController.java to see how the data is fetched
    this.dashboardService.getSumary().subscribe({
      next: (data) => {
        this.summary.set(data);
        this.markLoaded();
      },
      error: (error) => {
        console.error('Error while fetching summary:', error);
        this.markLoaded();
      },
    });

    this.dashboardService.getProposalsByStatus().subscribe({
      next: (data) => {
        this.statuses = data;
        this.proposalsByStatusChartData = {
          labels: Object.keys(data),
          datasets: [
            {
              label: null,
              data: Object.values(data),
              backgroundColor: [
                'rgba(255, 159, 64, 0.2)',
                'rgba(75, 192, 192, 0.2)',
                'rgba(54, 162, 235, 0.2)',
                'rgba(153, 102, 255, 0.2)',
                'rgba(14, 233, 12, 0.2)',
                'rgba(244, 12, 23, 0.2)',
              ],
              borderColor: [
                'rgb(255, 159, 64)',
                'rgb(75, 192, 192)',
                'rgb(54, 162, 235)',
                'rgb(153, 102, 255)',
                'rgb(14, 233, 12)',
                'rgb(244, 12, 23)',
              ],
              borderWidth: 1,
            },
          ],
        };
        this.markLoaded();
      },
      error: (error) => {
        console.error('Error while fetching proposals by status:', error);
        this.markLoaded();
      },
    });

    this.dashboardService.getProposalsByDate().subscribe({
      next: (data) => {
        this.proposalsByDateChartData = {
          labels: Object.keys(data),
          datasets: [
            {
              label: 'Proposals by Date',
              data: Object.values(data),
              fill: false,
              borderColor: 'rgba(153, 102, 255, 0.9)',
              tension: 0.4,
            },
          ],
        };
        this.markLoaded();
      },
      error: (error) => {
        console.error('Error while fetching proposals by date:', error);
        this.markLoaded();
      },
    });
  }
}
