import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpTestingController } from '@angular/common/http/testing';
import { provideTestDefaults } from '../../../testing/test-providers';
import { environment } from '../../../environments/environment';
import { makeTaxConfiguration } from '../../../testing/fixtures';
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
});
