import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NewExtraButtonComponent } from './new-extra-button.component';

describe('NewExtraButtonComponent', () => {
  let component: NewExtraButtonComponent;
  let fixture: ComponentFixture<NewExtraButtonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NewExtraButtonComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(NewExtraButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
