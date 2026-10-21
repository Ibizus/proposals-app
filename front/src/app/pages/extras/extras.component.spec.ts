import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpTestingController } from '@angular/common/http/testing';
import { provideTestDefaults } from '../../../testing/test-providers';
import { environment } from '../../../environments/environment';
import { makeExtra } from '../../../testing/fixtures';
import { ToastService } from '../../services/toast.service';

import { ExtrasComponent } from './extras.component';

const API = environment.API_ENDPOINT;

describe('ExtrasComponent', () => {
  let component: ExtrasComponent;
  let fixture: ComponentFixture<ExtrasComponent>;
  let http: HttpTestingController;

  beforeEach(async () => {
    spyOn(console, 'log');
    await TestBed.configureTestingModule({
      imports: [ExtrasComponent],
      providers: provideTestDefaults(),
    }).compileComponents();

    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(ExtrasComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => http.verify());

  it('loads the first page and updates the pagination state', () => {
    http
      .expectOne(API + 'extratemplates/?page=0&size=10')
      .flush({ extras: [makeExtra()], currentPage: 0, totalItems: 1 });

    expect(component.extras().length).toBe(1);
    expect(component.totalRecords()).toBe(1);
    expect(component.rows()).toBe(10);
    expect(component.isLoading()).toBeFalse();
  });

  it('stops the loading overlay when the list request fails', () => {
    http
      .expectOne(API + 'extratemplates/?page=0&size=10')
      .flush('nope', { status: 500, statusText: 'Server Error' });

    expect(component.isLoading()).toBeFalse();
  });

  it('confirms a saved row only after the API accepts it', () => {
    http
      .expectOne(API + 'extratemplates/?page=0&size=10')
      .flush({ extras: [makeExtra()], currentPage: 0, totalItems: 1 });
    const toast = TestBed.inject(ToastService);
    const success = spyOn(toast, 'success');

    component.onRowEditSave(makeExtra({ amount: 150 }));
    expect(success).not.toHaveBeenCalled();
    http.expectOne(API + 'extratemplates/1').flush(makeExtra({ amount: 150 }));

    expect(success).toHaveBeenCalledOnceWith('Extra saved correctly');
  });

  it('shows only a specific error toast when saving a row fails', () => {
    http
      .expectOne(API + 'extratemplates/?page=0&size=10')
      .flush({ extras: [makeExtra()], currentPage: 0, totalItems: 1 });
    const toast = TestBed.inject(ToastService);
    const success = spyOn(toast, 'success');
    const error = spyOn(toast, 'error');

    component.onRowEditSave(makeExtra({ amount: 150 }));
    http
      .expectOne(API + 'extratemplates/1')
      .flush('nope', { status: 400, statusText: 'Bad Request' });

    expect(error).toHaveBeenCalledOnceWith('The extra could not be saved');
    expect(success).not.toHaveBeenCalled();
  });
});
