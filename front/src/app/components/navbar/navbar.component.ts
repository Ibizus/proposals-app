import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, NavigationEnd } from '@angular/router';
import { AvatarModule } from 'primeng/avatar';
import { AvatarGroupModule } from 'primeng/avatargroup';
import { TabMenuModule } from 'primeng/tabmenu';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MenuItem } from 'primeng/api';
import { TranslateModule, TranslateService, TranslateStore } from '@ngx-translate/core';
import { TieredMenuModule } from 'primeng/tieredmenu';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [
    CommonModule,
    AvatarModule,
    AvatarGroupModule,
    TabMenuModule,
    RouterLink,
    RouterLinkActive,
    TieredMenuModule,
    TranslateModule,
  ],
  providers: [TranslateService, TranslateStore],
  template: `
    @if (!isLoginFormVisible) {
      <nav
        class="w-full bg-white h-16 inline-flex items-center absolute top-0 justify-between my-auto"
      >
        <header
          id="ui-navbar-top"
          class="navbar ui-navbar-top pl-4 md:pl-10 my-auto translate-y-[3px]"
        >
          <span id="siteIcon" class="navbar-brand mr-auto mr-lg-0">
            <div id="DeltaSiteLogo">
              <a id="ctl00_onetidProjectPropertyTitleGraphic" href="/#/dashboard">
                <img
                  id="ctl00_onetidHeadbnnr2"
                  class="ms-siteicon-img"
                  name="onetidHeadbnnr0"
                  src="/assets/logo.png"
                  alt="Logo"
                />
              </a>
            </div>
            <a
              id="ctl00_PlaceHolderSiteName_ProjectPropertyTitle"
              class="justify-self-center font-bold"
              href="/#/dashboard"
            >
              <!-- Proposal Admin -->
              {{ 'PROPOSAL ADMIN NAVBAR' | translate }}
            </a>
          </span>
        </header>
        <div class="mx-auto hidden xl:block">
          <p-tabMenu [model]="items" class="bg-white"></p-tabMenu>
        </div>
        <div class="flex items-center gap-2 md:gap-8 shrink-0 pr-3 md:pr-10">
          <i class="pi pi-language cursor-pointer" (click)="langMenu.toggle($event)"></i>
          <button class="p-2" (click)="menu.toggle($event)">
            <p-avatar icon="pi pi-user" size="large" shape="circle"></p-avatar>
          </button>

          <button class="xl:hidden p-2" (click)="navMenu.toggle($event)">
            <i class="pi pi-bars text-xl"></i>
          </button>
        </div>
      </nav>
      <p-tieredMenu #menu [model]="profileItems" [popup]="true" class="left-0"></p-tieredMenu>
      <p-tieredMenu #langMenu [model]="localesList" [popup]="true"></p-tieredMenu>
      <p-tieredMenu #navMenu [model]="items" [popup]="true"></p-tieredMenu>
    }
  `,
  styles: `
    :host ::ng-deep {
      .pi {
        cursor: pointer;
        color: #0056d2; /* Color for icons */
      }

      button {
        border: none;
        color: white;
        border-radius: 5px;
        transition: background-color 0.3s;
      }

      button:hover {
        color: #0056d2;
      }

      // .ui-navbar-top {
      //   box-shadow: 0 2px 4px rgba(0,0,0,0.1);
      // }
    }

    *,
    *::before,
    *::after {
      box-sizing: border-box;
    }

    @media (max-width: 991.98px) {
      .ui-navbar-top #siteIcon {
        -ms-flex-positive: 0;
        flex-grow: 0;
      }
    }

    @media screen and (min-width: 320px) {
      .ui-navbar-top.navbar .navbar-brand {
        font-size: calc(18px + 2 * ((100vw - 320px) / 1280));
      }
    }

    .ui-navbar-top.navbar[_ngcontent-ng-c498251216] .navbar-brand[_ngcontent-ng-c498251216] {
      font-family: 'Source Sans Pro', Arial, 'Helvetica Neue', sans-serif;
      // box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }

    .ui-navbar-top.navbar .navbar-brand {
      font-family: 'Source Sans Pro', Arial, 'Helvetica Neue', sans-serif;
      // box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }

    #siteIcon #ctl00_PlaceHolderSiteName_ProjectPropertyTitle {
      margin-left: 0.75rem;
    }

    .font-bold {
      font-weight: 700;
    }

    #siteIcon {
      display: -ms-inline-flexbox !important;
      display: inline-flex !important;
      -ms-flex-align: center !important;
      align-items: center !important;
      height: auto !important;
      min-width: auto !important;
      line-height: 1 !important;
      float: none !important;
      margin-right: 0 !important;
    }

    .ui-navbar-top #siteIcon #ctl00_PlaceHolderSiteName_ProjectPropertyTitle {
      display: block;
      font-size: 15px;
      line-height: 16px;
      border-left: 1px solid #d2d2d2;
      padding: 0px 0px 0px 20px;
      margin: 0px 0px 0px 20px;
      color: #151515;
      font-weight: 600;
    }

    #DeltaSiteLogo {
      flex-shrink: 0;
    }

    .ms-siteicon-img {
      width: min(177px, 24vw);
      max-width: none;
      height: auto;
    }

    @media (max-width: 991.98px) {
      .ui-navbar-top #siteIcon #ctl00_PlaceHolderSiteName_ProjectPropertyTitle {
        padding-left: 8px;
        margin-left: 8px;
      }
    }

    :host ::ng-deep {
      .p-tieredmenu {
        display: absolute;
        left: 0;
      }

      .p-menu-list {
        left: 0;
      }
    }
  `,
})
export class NavbarComponent implements OnInit {
  localesList!: MenuItem[];
  isLoginFormVisible = false;
  profileItems!: MenuItem[];
  items!: MenuItem[];
  activeItem!: MenuItem;

  constructor(
    private router: Router,
    private t: TranslateService,
  ) {
    this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.isLoginFormVisible = event.url === '/';
      }
    });
    this.t.use(localStorage.getItem('lang') || 'en');
  }

  /**
   * Initializes the component by fetching translations for different languages
   * and setting up the navigation items and profile items.
   *
   * @return {void}
   */
  ngOnInit() {
    const english$ = this.t.get('ENGLISH');
    const portuguese$ = this.t.get('PORTUGUESE');
    const german$ = this.t.get('GERMAN');
    const spanish$ = this.t.get('SPANISH');

    forkJoin([english$, portuguese$, german$, spanish$]).subscribe(
      ([english, portuguese, german, spanish]) => {
        this.localesList = [
          {
            code: 'en-US',
            label: english,
            route: '/en-US',
            command: () => this.switchLanguage('en'),
          },
          {
            code: 'pt',
            label: portuguese,
            route: '/pt',
            command: () => this.switchLanguage('pt'),
          },
          {
            code: 'de',
            label: german,
            route: '/de',
            command: () => this.switchLanguage('de'),
          },
          {
            code: 'es',
            label: spanish,
            route: '/es',
            command: () => this.switchLanguage('es'),
          },
        ];
      },
    );

    const proposals$ = this.t.get('PROPOSALS');
    const candidates$ = this.t.get('CANDIDATES');
    const positions$ = this.t.get('POSITIONS');
    const extras$ = this.t.get('EXTRAS');
    const taxes$ = this.t.get('TAXES');
    const parameters$ = this.t.get('PARAMETERS');
    const logout$ = this.t.get('LOGOUT');

    forkJoin([proposals$, candidates$, positions$, extras$, taxes$, parameters$]).subscribe(
      ([proposals, candidates, positions, extras, taxes, parameters]) => {
        this.items = [
          {
            label: proposals,
            icon: 'pi pi-fw pi-file',
            routerLink: ['/proposals'],
          },
          {
            label: candidates,
            icon: 'pi pi-fw pi-user',
            routerLink: ['/candidates'],
          },
          {
            label: positions,
            icon: 'pi pi-fw pi-briefcase',
            routerLink: ['/positions'],
          },
          { label: extras, icon: 'pi pi-fw pi-plus', routerLink: ['/extras'] },
          { label: taxes, icon: 'pi pi-fw pi-euro', routerLink: ['/taxes'] },
          {
            label: parameters,
            icon: 'pi pi-fw pi-receipt',
            routerLink: ['/parameters'],
          },
        ];
      },
    );

    forkJoin([logout$]).subscribe(([logOut]) => {
      this.profileItems = [
        {
          label: logOut,
          icon: 'pi pi-fw pi-sign-out',
          routerLink: ['/'],
        },
      ];
    });

    // this.activeItem = this.items[0];
  }

  switchLanguage(lang: string) {
    this.t.use(lang);
    localStorage.setItem('lang', lang);
    window.location.reload();
  }

  goBack(): void {
    if (location.pathname !== '/') {
      this.router.navigate(['dashboard']);
    }
  }

  goLogin(): void {
    if (location.pathname !== '/') {
      this.router.navigate(['']);
    }
  }
}
