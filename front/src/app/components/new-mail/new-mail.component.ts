import { Component, Input, Output, EventEmitter } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { TooltipModule } from 'primeng/tooltip';
import {
  TranslateModule,
  TranslateService,
  TranslateStore,
} from '@ngx-translate/core';
@Component({
  selector: 'app-new-mail',
  standalone: true,
  imports: [
    FormsModule,
    DialogModule,
    TooltipModule,
    ButtonModule,
    TranslateModule,
  ],
  providers: [TranslateService, TranslateStore],
  templateUrl: './new-mail.component.html',
  styleUrl: './new-mail.component.scss',
})
export class NewMailComponent {
  @Input() proposalId!: number;
  @Output() sendEmailClicked: EventEmitter<{
    email: string;
    subject: string;
    message: string;
    proposalId: number;
  }> = new EventEmitter<{
    email: string;
    subject: string;
    message: string;
    proposalId: number;
  }>();

  visible = false;
  position = 'right';
  emailPlaceholder = '';
  emailInput = '';
  subjectInput = '';
  messageInput = '';

  showDialog() {
    this.visible = true;
  }

  sendEmail() {
    this.visible = false;
    // console.log(
    //   'Returning event with all the information needed, proposalId: ' +
    //     this.proposalId +
    //     ' - ' +
    //     this.emailInput +
    //     ' - ' +
    //     this.subjectInput +
    //     ' - ' +
    //     this.messageInput +
    //     ' - '
    // );

    this.sendEmailClicked.emit({
      email: this.emailInput,
      subject: this.subjectInput,
      message: this.messageInput,
      proposalId: this.proposalId,
    });

    this.emailInput = '';
    this.subjectInput = '';
    this.messageInput = '';
  }
}
