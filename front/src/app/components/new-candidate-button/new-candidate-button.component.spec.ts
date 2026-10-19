import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTestDefaults } from '../../../testing/test-providers';

import { NewCandidateButtonComponent } from './new-candidate-button.component';

describe('NewCandidateButtonComponent', () => {
  let component: NewCandidateButtonComponent;
  let fixture: ComponentFixture<NewCandidateButtonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NewCandidateButtonComponent],
      providers: provideTestDefaults(),
    }).compileComponents();

    fixture = TestBed.createComponent(NewCandidateButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
