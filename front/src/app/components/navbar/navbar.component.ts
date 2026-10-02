import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { Router, NavigationEnd } from '@angular/router';
import { AvatarModule } from 'primeng/avatar';
import { AvatarGroupModule } from 'primeng/avatargroup';
import { TabMenuModule } from 'primeng/tabmenu';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MenuItem } from 'primeng/api';
import {
  TranslateModule,
  TranslateService,
  TranslateStore,
} from '@ngx-translate/core';
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
        class="navbar ui-navbar-top pl-10 my-auto translate-y-[3px]"
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
      <div class="mx-auto hidden lg:block">
        <p-tabMenu [model]="items" class="bg-white"></p-tabMenu>
      </div>
      <div class="w-[281px] flex justify-end">
        <i
          class="pi pi-language align-middle m-8 cursor-pointer hover:"
          (click)="langMenu.toggle($event)"
        ></i>
        <button (click)="menu.toggle($event)">
          <p-avatar
            icon="pi pi-user"
            class="mr-10"
            size="large"
            shape="circle"
          ></p-avatar>
        </button>

        <button class="lg:hidden p-2 mr-10" (click)="navMenu.toggle($event)">
          <i class="pi pi-bars text-xl mr-10"></i>
        </button>
      </div>
    </nav>
    <p-tieredMenu
      #menu
      [model]="profileItems"
      [popup]="true"
      class="left-0"
    ></p-tieredMenu>
    <p-tieredMenu #langMenu [model]="localesList" [popup]="true"></p-tieredMenu>
    <p-tieredMenu #navMenu [model]="items" [popup]="true"></p-tieredMenu>
    }
  `,
  styles: `
  :host ::ng-deep  {

    .pi{
      cursor: pointer;
      color: #0056D2; /* Color para iconos */
    }

    button {
      border: none;
      color: white;
      padding: 8px 16px;
      border-radius: 5px;
      transition: background-color 0.3s;
    }

    button:hover {
      color: #0056D2;
    }

    // .ui-navbar-top {
    //   box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    // }
  }
  
    *, *::before, *::after {
      box-sizing: border-box;
    }

    @media (max-width: 991.98px) {
      .ui-navbar-top #siteIcon {
          -ms-flex-positive: 1;
          flex-grow: 1;
      }
    }

    @media screen and (min-width: 320px) {
      .ui-navbar-top.navbar .navbar-brand {
          font-size: calc(18px + 2 * ((100vw - 320px) / 1280));
      }
    }

    .ui-navbar-top.navbar[_ngcontent-ng-c498251216] .navbar-brand[_ngcontent-ng-c498251216] {
      font-family: "Source Sans Pro", Arial, "Helvetica Neue", sans-serif;
      // box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }

    .ui-navbar-top.navbar .navbar-brand {
      font-family: "Source Sans Pro", Arial, "Helvetica Neue", sans-serif;
      // box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }

    #siteIcon #ctl00_PlaceHolderSiteName_ProjectPropertyTitle {
      margin-left: .75rem;
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
export class NavbarComponent {
  localesList!: MenuItem[];
  isLoginFormVisible: boolean = false;
  profileItems!: MenuItem[];
  items!: MenuItem[];
  activeItem!: MenuItem;

  constructor(private router: Router, private t: TranslateService) {
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
      }
    );

    const proposals$ = this.t.get('PROPOSALS');
    const candidates$ = this.t.get('CANDIDATES');
    const extras$ = this.t.get('EXTRAS');
    const taxes$ = this.t.get('TAXES');
    const parameters$ = this.t.get('PARAMETERS');
    const logout$ = this.t.get('LOGOUT');

    forkJoin([proposals$, candidates$, extras$, taxes$, parameters$]).subscribe(
      ([proposals, candidates, extras, taxes, parameters]) => {
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
          { label: extras, icon: 'pi pi-fw pi-plus', routerLink: ['/extras'] },
          { label: taxes, icon: 'pi pi-fw pi-euro', routerLink: ['/taxes'] },
          {
            label: parameters,
            icon: 'pi pi-fw pi-receipt',
            routerLink: ['/parameters'],
          },
        ];
      }
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
