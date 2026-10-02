import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NewMailComponent } from './new-mail.component';

describe('NewMailComponent', () => {
  let component: NewMailComponent;
  let fixture: ComponentFixture<NewMailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NewMailComponent]
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
