import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NgForm } from '@angular/forms';
import { TranslateService } from '@ngx-translate/core';
import { provideTestDefaults } from '../../../testing/test-providers';

import { LoginFormComponent } from './login-form.component';

describe('LoginFormComponent', () => {
  let component: LoginFormComponent;
  let fixture: ComponentFixture<LoginFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoginFormComponent],
      providers: provideTestDefaults(),
    }).compileComponents();

    fixture = TestBed.createComponent(LoginFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('navigates to the dashboard when the form is valid', () => {
    const navigate = spyOn(component.router, 'navigate').and.resolveTo(true);
    component.onSubmit({ valid: true } as NgForm);
    expect(navigate).toHaveBeenCalledWith(['/dashboard']);
  });

  it('shows an in-app error and stays put when the form is invalid', () => {
    const translate = fixture.debugElement.injector.get(TranslateService);
    translate.setTranslation('en', {
      'LOGIN REQUIRED FIELDS': 'Please fill out all the necessary fields.',
    });
    translate.use('en');
    const navigate = spyOn(component.router, 'navigate');
    component.onSubmit({ valid: false } as NgForm);
    fixture.detectChanges();
    expect((fixture.nativeElement as HTMLElement).textContent).toContain(
      'Please fill out all the necessary fields.',
    );
    expect(navigate).not.toHaveBeenCalled();
  });

  it('toggles password visibility and its icon', () => {
    const icon = (fixture.nativeElement as HTMLElement).querySelector(
      '#togglePasswordIcon',
    ) as HTMLElement;

    component.togglePassword();
    expect(component.showPassword).toBeTrue();
    expect(icon.className).toContain('pi-eye-slash');

    component.togglePassword();
    expect(component.showPassword).toBeFalse();
    expect(icon.className).not.toContain('pi-eye-slash');
  });
});
