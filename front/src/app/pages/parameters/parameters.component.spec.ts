import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpTestingController } from '@angular/common/http/testing';
import { provideTestDefaults } from '../../../testing/test-providers';
import { environment } from '../../../environments/environment';
import { ToastService } from '../../services/toast.service';
import { Parameter } from '../../utils/types';

import { ParametersComponent } from './parameters.component';

const API = environment.API_ENDPOINT;

function param(id: number, name: string, value: number): Parameter {
  const at = new Date('2024-01-01T00:00:00Z');
  return {
    id,
    name,
    value,
    validFrom: at,
    validUntil: at,
    createdAt: at,
    createdBy: '',
    modifiedAt: at,
    modifiedBy: '',
  };
}

describe('ParametersComponent', () => {
  let component: ParametersComponent;
  let fixture: ComponentFixture<ParametersComponent>;
  let http: HttpTestingController;
  let toast: ToastService;

  beforeEach(async () => {
    spyOn(console, 'log');
    await TestBed.configureTestingModule({
      imports: [ParametersComponent],
      providers: provideTestDefaults(),
    }).compileComponents();

    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(ParametersComponent);
    component = fixture.componentInstance;
    // ToastService is provided by the component itself.
    toast = fixture.debugElement.injector.get(ToastService);
    fixture.detectChanges();

    http.expectOne(API + 'parameters').flush([param(1, 'iht', 10), param(2, 'spp', 5)]);
    fixture.detectChanges();
  });

  afterEach(() => http.verify());

  it('loads parameters on init', () => {
    expect(component.parameters.map((p) => p.name)).toEqual(['iht', 'spp']);
    expect(component.isLoading).toBeFalse();
  });

  it('removes a parameter locally once the API deletes it', fakeAsync(() => {
    const success = spyOn(toast, 'success');

    component.deleteParameter(component.parameters[0]);
    const req = http.expectOne(API + 'parameters/1');
    expect(req.request.method).toBe('DELETE');
    req.flush({});

    expect(component.parameters.map((p) => p.id)).toEqual([2]);
    tick(500);
    expect(success).toHaveBeenCalledWith('Parameter deleted successfully');
  }));

  it('reports a failed modification through the toast service', () => {
    const error = spyOn(toast, 'error');

    component.modifyParameter(param(2, 'spp', 6));
    const req = http.expectOne(API + 'parameters/2');
    expect(req.request.method).toBe('PUT');
    req.flush('nope', { status: 400, statusText: 'Bad Request' });

    expect(error).toHaveBeenCalled();
  });
});
