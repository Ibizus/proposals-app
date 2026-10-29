import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { NgIconComponent, provideIcons } from '@ng-icons/core';
import { matAddCircleOutline } from '@ng-icons/material-icons/outline';

@Component({
  selector: 'app-new-proposal-button',
  standalone: true,
  imports: [NgIconComponent],
  viewProviders: [provideIcons({ matAddCircleOutline })],
  template: `
    <button
      class="fixed bottom-0 right-0 m-8 hover:scale-105 transition-all"
      (click)="newProposal()"
    >
      <ng-icon name="matAddCircleOutline" size="100px" color="black" />
    </button>
  `,
  styles: ``,
})
export class NewProposalButtonComponent {
  private router: Router = new Router();

  newProposal() {
    this.router.navigate(['/new-proposal']);
  }
}
