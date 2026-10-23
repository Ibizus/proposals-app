import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTestDefaults } from '../../../testing/test-providers';

import { NewPositionButtonComponent } from './new-position-button.component';

describe('NewPositionButtonComponent', () => {
  let component: NewPositionButtonComponent;
  let fixture: ComponentFixture<NewPositionButtonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NewPositionButtonComponent],
      providers: provideTestDefaults(),
    }).compileComponents();

    fixture = TestBed.createComponent(NewPositionButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
