import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTestDefaults } from '../../../testing/test-providers';

import { NewMailComponent } from './new-mail.component';

describe('NewMailComponent', () => {
  let component: NewMailComponent;
  let fixture: ComponentFixture<NewMailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NewMailComponent],
      providers: provideTestDefaults(),
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(NewMailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
