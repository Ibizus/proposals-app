import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpTestingController } from '@angular/common/http/testing';
import { provideTestDefaults } from '../../../testing/test-providers';
import { environment } from '../../../environments/environment';
import { makeTaxConfiguration } from '../../../testing/fixtures';
import { TaxesComponent } from './taxes.component';
import { ToastService } from '../../services/toast.service';

const API = environment.API_ENDPOINT;

describe('TaxesComponent', () => {
  let component: TaxesComponent;
  let fixture: ComponentFixture<TaxesComponent>;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TaxesComponent],
      providers: provideTestDefaults(),
    }).compileComponents();

    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(TaxesComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => http.verify());

  function flushConfigurations() {
    const active = makeTaxConfiguration({ id: 1, countryCode: 'es', active: true });
    const inactive = makeTaxConfiguration({
      id: 2,
      countryCode: 'pt',
      active: false,
      label: 'Portugal (illustrative example data)',
    });
    http.expectOne(API + 'tax-configurations/').flush([inactive, active]);
    fixture.detectChanges();
    return { active, inactive };
  }

  it('should create', () => {
    fixture.detectChanges();
    flushConfigurations();
    expect(component).toBeTruthy();
  });

  it('loads configurations and selects the active one', () => {
    fixture.detectChanges();
    const { active } = flushConfigurations();

    expect(component.configurations().length).toBe(2);
    expect(component.selectedConfiguration()?.id).toBe(active.id);
    expect(component.isLoading()).toBeFalse();
  });

  it('activates a configuration and reloads the list', () => {
    fixture.detectChanges();
    const { inactive } = flushConfigurations();

    component.onActivate(inactive.id);
    http.expectOne(API + 'tax-configurations/2/activate').flush({ ...inactive, active: true });

    http.expectOne(API + 'tax-configurations/').flush([
      { ...inactive, active: true },
      makeTaxConfiguration({ id: 1, countryCode: 'es', active: false }),
    ]);
    fixture.detectChanges();

    expect(component.configurations().some((c) => c.id === inactive.id && c.active)).toBeTrue();
  });

  it('creates a configuration and reloads the list', () => {
    fixture.detectChanges();
    flushConfigurations();

    component.openCreateDialog();
    component.newConfiguration.countryCode = 'fr';
    component.newConfiguration.label = 'France (illustrative example data)';
    component.newConfiguration.yearlyPayments = 12;
    component.newConfiguration.taxes = [
      { name: 'Social Security', type: 'FIXED', fixedRate: 10, extrasOnly: false, brackets: [] },
    ];

    component.onCreate();
    const req = http.expectOne(API + 'tax-configurations/');
    expect(req.request.method).toBe('POST');
    expect(req.request.body.countryCode).toBe('fr');
    req.flush(makeTaxConfiguration({ id: 3, countryCode: 'fr' }));

    http
      .expectOne(API + 'tax-configurations/')
      .flush([
        makeTaxConfiguration({ id: 3, countryCode: 'fr' }),
        makeTaxConfiguration({ id: 1, countryCode: 'es', active: true }),
        makeTaxConfiguration({ id: 2, countryCode: 'pt', active: false }),
      ]);
    fixture.detectChanges();

    expect(component.configurations().length).toBe(3);
    expect(component.visibleCreate()).toBeFalse();
  });

  it('does not create an invalid configuration', () => {
    fixture.detectChanges();
    flushConfigurations();
    const toast = TestBed.inject(ToastService);
    const error = spyOn(toast, 'error');

    component.openCreateDialog();
    component.newConfiguration.countryCode = '';
    component.onCreate();

    expect(error).toHaveBeenCalledWith('Please fill in all required fields');
  });
});
