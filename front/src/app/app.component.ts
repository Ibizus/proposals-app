import { Component, OnInit } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { NavbarComponent } from './components/navbar/navbar.component';
import { PrimeNGConfig } from 'primeng/api';
import { TabMenuModule } from 'primeng/tabmenu';
import { MenuItem } from 'primeng/api';
import {
  TranslateService,
  TranslateStore,
  TranslateModule,
} from '@ngx-translate/core';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    HttpClientModule,
    RouterOutlet,
    NavbarComponent,
    TabMenuModule,
    TranslateModule,
    // BrowserAnimationsModule
  ],
  providers: [TranslateStore, TranslateService],
  template: `
    <app-navbar />
    <router-outlet />
  `,
  styleUrl: './app.component.scss',
})
export class AppComponent implements OnInit {
  title = 'front';

  items!: MenuItem[];

  activeItem!: MenuItem;

  isLoginFormVisible: boolean = false;

  constructor(
    private router: Router,
    private primengConfig: PrimeNGConfig,
    private translate: TranslateService
  ) {
    this.translate.addLangs(['en', 'es', 'de', 'pt']);
    const userLang = navigator.language.split('-')[0];
    // console.log(userLang);

    this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.isLoginFormVisible = event.url === '/';
      }
    });
  }

  ngOnInit() {
    this.primengConfig.ripple = true;

    this.items = [
      {
        label: 'Proposals',
        icon: 'pi pi-fw pi-file',
        routerLink: ['/dashboard'],
      },
      {
        label: 'Candidates',
        icon: 'pi pi-fw pi-user',
        routerLink: ['/candidates'],
      },
      { label: 'Extras', icon: 'pi pi-fw pi-plus', routerLink: ['/extras'] },
      { label: 'Taxes', icon: 'pi pi-fw pi-euro', routerLink: ['/taxes'] },
      //{ label: 'Settings', icon: 'pi pi-fw pi-cog' }
    ];

    this.activeItem = this.items[0];
  }

  goBack(): void {
    if (location.pathname !== '/') {
      this.router.navigate(['dashboard']);
    }
  }

  goCandidates(): void {
    if (location.pathname !== '/') {
      this.router.navigate(['candidates']);
    }
  }

  goExtras(): void {
    if (location.pathname !== '/') {
      this.router.navigate(['extras']);
    }
  }

  goLogin(): void {
    if (location.pathname !== '/') {
      this.router.navigate(['']);
    }
  }

  goTaxes(): void {
    if (location.pathname !== '/') {
      this.router.navigate(['taxes']);
    }
  }
}
