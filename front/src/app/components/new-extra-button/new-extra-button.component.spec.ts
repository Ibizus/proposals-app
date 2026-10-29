import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { provideTestDefaults } from '../../../testing/test-providers';
import { ExtrasService } from '../../services/extras.service';

import { NewExtraButtonComponent } from './new-extra-button.component';

describe('NewExtraButtonComponent', () => {
  let component: NewExtraButtonComponent;
  let fixture: ComponentFixture<NewExtraButtonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NewExtraButtonComponent],
      providers: provideTestDefaults(),
    }).compileComponents();

    fixture = TestBed.createComponent(NewExtraButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('creates and resets a new extra without default tax rates or addToSalary', () => {
    const extrasService = TestBed.inject(ExtrasService);
    const createExtraSpy = spyOn(extrasService, 'createExtra').and.returnValue(of({}));

    component.onSaveExtra();
    expect(createExtraSpy).toHaveBeenCalledTimes(1);
    let sentExtra = createExtraSpy.calls.argsFor(0)[0];
    expect(sentExtra).toEqual(
      jasmine.objectContaining({
        name: '',
        amount: 0,
        isPercentage: false,
        isSSTaxable: false,
        isIRSTaxable: false,
        multiplyYearly: 0,
        multiplyMonthly: 0,
      }),
    );
    expect(sentExtra.ssTaxrate).toBeUndefined();
    expect(sentExtra.irsTaxrate).toBeUndefined();
    expect(sentExtra.addToSalary).toBeUndefined();

    component.onSaveExtra();
    expect(createExtraSpy).toHaveBeenCalledTimes(2);
    sentExtra = createExtraSpy.calls.argsFor(1)[0];
    expect(sentExtra.ssTaxrate).toBeUndefined();
    expect(sentExtra.irsTaxrate).toBeUndefined();
    expect(sentExtra.addToSalary).toBeUndefined();
  });
});
