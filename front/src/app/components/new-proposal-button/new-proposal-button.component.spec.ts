import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { provideTestDefaults } from '../../../testing/test-providers';

import { NewProposalButtonComponent } from './new-proposal-button.component';

describe('NewProposalButtonComponent', () => {
  let component: NewProposalButtonComponent;
  let fixture: ComponentFixture<NewProposalButtonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NewProposalButtonComponent],
      providers: provideTestDefaults(),
    }).compileComponents();

    fixture = TestBed.createComponent(NewProposalButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('navigates to the new proposal page when clicked', () => {
    // The component builds its own Router instance, so spy on that one.
    const router = (component as unknown as { router: Router }).router;
    const navigate = spyOn(router, 'navigate').and.resolveTo(true);

    ((fixture.nativeElement as HTMLElement).querySelector('button') as HTMLButtonElement).click();

    expect(navigate).toHaveBeenCalledWith(['/new-proposal']);
  });
});
