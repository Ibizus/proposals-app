import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from './components/navbar/navbar.component';
import { PrimeNGConfig } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { TranslateService, TranslateStore, TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterOutlet,
    NavbarComponent,
    ToastModule,
    TranslateModule,
    // BrowserAnimationsModule
  ],
  providers: [TranslateStore, TranslateService],
  template: `
    <app-navbar />
    <router-outlet />
    <p-toast position="bottom-center" key="bc"></p-toast>
  `,
  styleUrl: './app.component.scss',
})
export class AppComponent implements OnInit {
  title = 'front';

  constructor(
    private primengConfig: PrimeNGConfig,
    private translate: TranslateService,
  ) {
    this.translate.addLangs(['en', 'es', 'de', 'pt']);
  }

  ngOnInit() {
    this.primengConfig.ripple = true;
  }
}
