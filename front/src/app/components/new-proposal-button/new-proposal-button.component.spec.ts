import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NewProposalButtonComponent } from './new-proposal-button.component';

describe('NewProposalButtonComponent', () => {
  let component: NewProposalButtonComponent;
  let fixture: ComponentFixture<NewProposalButtonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NewProposalButtonComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(NewProposalButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
