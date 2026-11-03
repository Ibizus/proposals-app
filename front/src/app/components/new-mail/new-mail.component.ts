import { Component, Input } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { finalize } from 'rxjs';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { TooltipModule } from 'primeng/tooltip';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { MailService } from '../../services/mail.service';
import { ToastService } from '../../services/toast.service';
import { callerHandlesErrors } from '../../services/error-toast.interceptor';

function notBlank(control: AbstractControl): ValidationErrors | null {
  return (control.value ?? '').trim() ? null : { required: true };
}

@Component({
  selector: 'app-new-mail',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    DialogModule,
    InputTextModule,
    InputTextareaModule,
    TooltipModule,
    ButtonModule,
    TranslateModule,
  ],
  providers: [],
  templateUrl: './new-mail.component.html',
  styleUrl: './new-mail.component.scss',
})
export class NewMailComponent {
  @Input() proposalId!: number;

  visible = false;
  sending = false;
  emailPlaceholder = '';

  emailForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    subject: ['', notBlank],
    message: ['', notBlank],
  });

  constructor(
    private fb: FormBuilder,
    private mailService: MailService,
    private toastService: ToastService,
    private t: TranslateService,
  ) {}

  showDialog() {
    this.visible = true;
  }

  sendEmail() {
    if (this.sending) {
      return;
    }
    if (this.emailForm.invalid) {
      this.emailForm.markAllAsTouched();
      this.toastService.warn(this.t.instant('INVALID FORM'));
      return;
    }

    const email = this.emailForm.value.email ?? '';
    const subject = this.emailForm.value.subject ?? '';
    const message = this.emailForm.value.message ?? '';

    this.sending = true;
    this.mailService
      .newEmail(email, subject, message, this.proposalId, callerHandlesErrors())
      .pipe(finalize(() => (this.sending = false)))
      .subscribe({
        next: () => {
          this.toastService.success(this.t.instant('EMAIL SENT SUCCESS'));
          this.visible = false;
          this.emailForm.reset();
        },
        error: () => {
          this.toastService.error(this.t.instant('EMAIL SEND ERROR MESSAGE'));
        },
      });
  }
}
