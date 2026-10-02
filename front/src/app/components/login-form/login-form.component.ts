import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { ButtonModule } from 'primeng/button';
import { NgForm } from '@angular/forms';
import {
  TranslateModule,
  TranslateService,
  TranslateStore,
} from '@ngx-translate/core';

@Component({
  selector: 'app-login-form',
  standalone: true,
  imports: [
    FormsModule,
    InputTextModule,
    PasswordModule,
    ButtonModule,
    TranslateModule,
  ],
  providers: [TranslateService, TranslateStore],
  template: `
    <div class="grid h-screen place-items-center relative">
      <img src="assets/login-pattern.png" class="w-screen h-screen" />
      <div
        class="my-auto max-w-md bg-white border shadow-md p-8 mx-auto absolute"
      >
        <header
          id="ui-navbar-top"
          class="navbar ui-navbar-top justify-center p-10 mx-auto"
        >
          <span id="siteIcon" class="navbar-brand mr-auto mr-lg-0">
            <div id="DeltaSiteLogo">
              <a id="ctl00_onetidProjectPropertyTitleGraphic" href="/">
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
              href="/"
            >
              <!-- Proposal Admin -->
              {{ 'PROPOSAL ADMIN NAVBAR' | translate }}
            </a>
          </span>
        </header>
        <!-- <img src="assets/logo.png" alt="logo" class="m-10 mx-auto" /> -->
        <div id="welcome">
          <label
            class="flex justify-center text-lg font-semibold text-[#0056D2] ml-auto pb-5"
          >
            {{ 'WELCOME' | translate }}
          </label>
        </div>
        <form #loginForm="ngForm" (ngSubmit)="onSubmit(loginForm)">
          <div class="mb-4">
            <label for="username" class="text-sm text-gray-700">
              {{ 'EMAIL' | translate }}
            </label>
            <input
              pInputText
              type="text"
              id="username"
              name="username"
              placeholder="{{ 'ENTERUSER' | translate }}"
              [(ngModel)]="username"
              required
            />
          </div>
          <div class="mb-4">
            <label for="password" class="text-sm text-gray-700">
              {{ 'PASSWORD' | translate }}
            </label>
            <span class="p-input-icon-right w-full">
              <i
                id="togglePasswordIcon"
                class="pi pi-eye cursor-pointer"
                (click)="togglePassword()"
              ></i>
              <input
                pPassword
                type="password"
                name="password"
                [(ngModel)]="password"
                [feedback]="false"
                [required]="true"
                [showPassword]="showPassword"
                placeholder="{{ 'ENTERPASSWORD' | translate }}"
              />
            </span>
          </div>
          <div class="w-full">
            <p-button
              class="w-full"
              type="submit"
              label="{{ 'LOGIN' | translate }}"
              [rounded]="false"
            >
            </p-button>
          </div>
        </form>
        <div class="flex justify-center mt-3">
          <p>
            {{ 'CANT LOGIN' | translate }}
            <a
              class="size-[14px] text-[#0056D2] font-semibold"
              href="https://intranet.ent.propflow.com/Pages/default.aspx"
              >Click here</a
            >
          </p>
        </div>
      </div>
      <small
        id="small"
        class="absolute bottom-5 left-1/2 transform -translate-x-1/2"
        >&copy; {{ 'RIGHTS' | translate }}</small
      >
    </div>
  `,
  styles: `
    * {
      overflow: hidden;
    }

    .small {
      font-family: Source Sans Pro, "Source Sans Pro", sans-serif;
      size: 12px;
    }

    .ui-navbar-top.navbar[_ngcontent-ng-c498251216] .navbar-brand[_ngcontent-ng-c498251216] {
      font-family: "Source Sans Pro", Arial, "Helvetica Neue", sans-serif;
    }

    .ui-navbar-top.navbar .navbar-brand {
      font-family: "Source Sans Pro", Arial, "Helvetica Neue", sans-serif;
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
      font-size: 16px;
      line-height: 16px;
      border-left: 1px solid #d2d2d2;
      padding: 0px 0px 0px 20px;
      margin: 0px 0px 0px 20px;
      color: #151515;
      font-weight: 600;
    }
    
    :host ::ng-deep {
      .p-button {
        background: #0056D2;
        color: white;
        width: 100%;
        border-radius: 0px;
      }

      .p-button-label {
        font-size: 16px;
        font-weight: 400;
        padding: 6px;
        padding-left: 15px;
        padding-right: 15px;
        font-family: Source Sans Pro, "Source Sans Pro", sans-serif;
      }

      .p-inputtext {
        padding: 8px;
        width: 100%;
      }

      .p-password {
        width: 100%;
        padding:4px;
      }

      .p-password-input {
        padding: 8px;
        width: 100%;
      }

      .p-inputtext {
        border: none !important;
        border-bottom: 1px solid #000 !important;
        border-radius: 0 !important;
        box-shadow: none !important;
        color: #333333;
        background: #EFEFEF;
      }

      .p-inputtext:focus {
        border: none !important;
        border-bottom: 2px solid #0056D2 !important;
        border-radius: 0 !important;
        box-shadow: none !important;
        transition: 100ms ease-out;
      }
      
    }
  `,
})
export class LoginFormComponent {
  loginForm!: NgForm;

  username: string = '';
  password: string = '';

  showPassword: boolean = false;

  router: Router = new Router();

  constructor(private t: TranslateService) {
    this.t.use(localStorage.getItem('lang') || 'en');
  }

  onSubmit(loginForm: NgForm) {
    if (loginForm.valid) {
      this.router.navigate(['/dashboard']);
    } else {
      alert('Please, fill out all the necessary fields.');
    }
  }

  togglePassword(): void {
    this.showPassword = !this.showPassword;

    let togglePasswordIcon = document.getElementById('togglePasswordIcon');

    if (togglePasswordIcon) {
      if (this.showPassword) {
        togglePasswordIcon.className = 'pi pi-eye-slash cursor-pointer';
      } else {
        togglePasswordIcon.className = 'pi pi-eye cursor-pointer';
      }
    }
  }
}
