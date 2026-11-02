import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpTestingController } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { provideTestDefaults } from '../../../testing/test-providers';
import { environment } from '../../../environments/environment';
import { makeTaxConfiguration } from '../../../testing/fixtures';
import { ExtrasService } from '../../services/extras.service';
import { NewExtraButtonComponent } from './new-extra-button.component';

const API = environment.API_ENDPOINT;

describe('NewExtraButtonComponent', () => {
  let component: NewExtraButtonComponent;
  let fixture: ComponentFixture<NewExtraButtonComponent>;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NewExtraButtonComponent],
      providers: provideTestDefaults(),
    }).compileComponents();

    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(NewExtraButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    http
      .expectOne(API + 'tax-configurations/active')
      .flush(makeTaxConfiguration({ id: 1, active: true }));
    fixture.detectChanges();
  });

  afterEach(() => http.verify());

  it('loads the active configuration taxes as options', () => {
    expect(component.taxOptions().length).toBe(2);
    expect(component.taxOptions().map((o) => o.value)).toEqual([10, 20]);
  });

  it('clears selected taxes when the extra is marked exempt', () => {
    component.extra.taxIds = [10];
    component.extra.exempt = true;

    component.onExemptChange();

    expect(component.extra.taxIds).toEqual([]);
  });

  it('creates and resets a new extra without tax ids and not exempt by default', () => {
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
        exempt: false,
        multiplyYearly: 0,
        multiplyMonthly: 0,
        addToSalary: false,
        taxConfigurationId: 1,
      }),
    );
    expect(sentExtra.taxIds).toEqual([]);

    component.onSaveExtra();
    expect(createExtraSpy).toHaveBeenCalledTimes(2);
    sentExtra = createExtraSpy.calls.argsFor(1)[0];
    expect(sentExtra.taxIds).toEqual([]);
    expect(sentExtra.exempt).toBeFalse();
    expect(sentExtra.taxConfigurationId).toBe(1);
  });
});
