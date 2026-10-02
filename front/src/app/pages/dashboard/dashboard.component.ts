import { Component, OnInit } from '@angular/core';
import {
  TranslateModule,
  TranslateService,
  TranslateStore,
} from '@ngx-translate/core';
import { ChartModule } from 'primeng/chart';
import { DashboardService } from '../../services/dashboard.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [TranslateModule, ChartModule],
  providers: [TranslateService, TranslateStore],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent implements OnInit {
  // Dashboard variables
  summary: any = {};
  proposalsByStatusChartData: any;
  proposalsByDateChartData: any;
  barOptions: any;
  lineOptions: any;
  statuses: any;

  constructor(
    private dashboardService: DashboardService,
    private t: TranslateService
  ) {
    this.t.use(localStorage.getItem('lang') || 'en');
    const documentStyle = getComputedStyle(document.documentElement);
    const textColorSecondary = documentStyle.getPropertyValue(
      '--text-color-secondary'
    );
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
        this.summary = data;
      },
      error: (error) => {
        console.error('Error while fetching summary:', error);
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
      },
      error: (error) => {
        console.error('Error while fetching proposals by status:', error);
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
      },
      error: (error) => {
        console.error('Error while fetching proposals by date:', error);
      },
    });
  }
}
