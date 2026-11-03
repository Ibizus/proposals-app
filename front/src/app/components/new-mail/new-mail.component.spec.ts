import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpTestingController } from '@angular/common/http/testing';
import { TranslateService } from '@ngx-translate/core';
import { provideTestDefaults } from '../../../testing/test-providers';
import { NewMailComponent } from './new-mail.component';
import { ToastService } from '../../services/toast.service';

describe('NewMailComponent', () => {
  let component: NewMailComponent;
  let fixture: ComponentFixture<NewMailComponent>;
  let http: HttpTestingController;
  let toast: ToastService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NewMailComponent],
      providers: provideTestDefaults(),
    }).compileComponents();

    http = TestBed.inject(HttpTestingController);
    toast = TestBed.inject(ToastService);

    const translate = TestBed.inject(TranslateService);
    translate.setTranslation('en', {
      'PLEASE ENTER A VALID EMAIL': 'Please enter a valid email.',
      'SUBJECT REQUIRED': 'Subject is required.',
      'MESSAGE REQUIRED': 'Message is required.',
      'INVALID FORM': 'Invalid form.',
      'EMAIL SENT SUCCESS': 'Email sent.',
      'EMAIL SEND ERROR MESSAGE': 'Failed to send email.',
    });
    translate.use('en');

    fixture = TestBed.createComponent(NewMailComponent);
    component = fixture.componentInstance;
    component.proposalId = 7;
    fixture.detectChanges();
  });

  afterEach(() => http.verify());

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('shows validation errors and does not send when the form is empty', () => {
    const toastSpy = spyOn(toast, 'warn');
    component.emailForm.setValue({ email: '', subject: '', message: '' });

    component.sendEmail();

    expect(toastSpy).toHaveBeenCalledWith('Invalid form.');
    expect(component.visible).toBeFalse();
    http.expectNone((req) => req.url.includes('send/to/'));
  });

  it('rejects an invalid email address', () => {
    const toastSpy = spyOn(toast, 'warn');
    component.emailForm.setValue({ email: 'not-an-email', subject: 'Subject', message: 'Body' });

    component.sendEmail();

    expect(toastSpy).toHaveBeenCalledWith('Invalid form.');
    http.expectNone((req) => req.url.includes('send/to/'));
  });

  it('rejects a subject or message made only of whitespace', () => {
    const toastSpy = spyOn(toast, 'warn');
    component.emailForm.setValue({
      email: 'recruiter@example.com',
      subject: '   ',
      message: '\n\t ',
    });

    component.sendEmail();

    expect(toastSpy).toHaveBeenCalledWith('Invalid form.');
    expect(component.emailForm.controls.subject.invalid).toBeTrue();
    expect(component.emailForm.controls.message.invalid).toBeTrue();
    http.expectNone((req) => req.url.includes('send/to/'));
  });

  it('ignores a second click while a send is pending and allows a retry after a failure', () => {
    component.emailForm.setValue({
      email: 'recruiter@example.com',
      subject: 'Offer',
      message: 'Please review.',
    });

    component.sendEmail();
    component.sendEmail();

    http
      .expectOne((r) => r.url.includes('send/to/'))
      .flush('Mail server error', { status: 500, statusText: 'Server Error' });

    component.sendEmail();
    http.expectOne((r) => r.url.includes('send/to/')).flush({});
  });

  it('sends the email when the form is valid', () => {
    const successSpy = spyOn(toast, 'success');
    component.emailForm.setValue({
      email: 'recruiter@example.com',
      subject: 'Offer',
      message: 'Please review.',
    });

    component.sendEmail();

    const req = http.expectOne(
      (r) =>
        r.url.includes('send/to/') &&
        r.url.includes('to=recruiter%40example.com') &&
        r.url.includes('subject=Offer') &&
        r.url.includes('proposalId=7'),
    );
    req.flush({});

    expect(successSpy).toHaveBeenCalledWith('Email sent.');
    expect(component.visible).toBeFalse();
    expect(component.emailForm.value).toEqual({ email: null, subject: null, message: null });
  });

  it('shows an error toast when the backend fails', () => {
    const errorSpy = spyOn(toast, 'error');
    component.emailForm.setValue({
      email: 'recruiter@example.com',
      subject: 'Offer',
      message: 'Please review.',
    });

    component.sendEmail();

    const req = http.expectOne(
      (r) =>
        r.url.includes('send/to/') &&
        r.url.includes('to=recruiter%40example.com') &&
        r.url.includes('subject=Offer') &&
        r.url.includes('proposalId=7'),
    );
    req.flush('Mail server error', { status: 500, statusText: 'Server Error' });

    expect(errorSpy).toHaveBeenCalledOnceWith('Failed to send email.');
  });
});
