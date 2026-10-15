import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTestDefaults } from '../../../testing/test-providers';

import { ProposalsComponent } from './proposals.component';

describe('ProposalsComponent', () => {
  let component: ProposalsComponent;
  let fixture: ComponentFixture<ProposalsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProposalsComponent],
      providers: provideTestDefaults(),
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ProposalsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
