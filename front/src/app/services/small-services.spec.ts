import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { MessageService } from 'primeng/api';
import { AutoCompleteCompleteEvent } from 'primeng/autocomplete';
import { environment } from '../../environments/environment';
import { makeProposal } from '../../testing/fixtures';
import { DashboardService } from './dashboard.service';
import { MailService } from './mail.service';
import { OfficeService } from './office.service';
import { ParameterService } from './parameter.service';
import { Parameter } from '../utils/types';
import { ToastService } from './toast.service';
import { callerHandlesErrors, errorToastInterceptor } from './error-toast.interceptor';

const API = environment.API_ENDPOINT;

describe('HTTP services', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
    spyOn(console, 'log');
  });

  afterEach(() => http.verify());

  function expectGet(url: string) {
    const req = http.expectOne(url);
    expect(req.request.method).toBe('GET');
    return req;
  }

  describe('DashboardService', () => {
    it('hits the dashboard endpoints', () => {
      const service = TestBed.inject(DashboardService);
      service.getSumary().subscribe();
      service.getProposalsByStatus().subscribe();
      service.getProposalsByDate().subscribe();

      expectGet(API + 'dashboard/summary').flush({});
      expectGet(API + 'dashboard/proposals/status').flush({});
      expectGet(API + 'dashboard/proposals/createdat').flush({});
    });
  });

  describe('ParameterService', () => {
    it('lists, modifies and deletes parameters', () => {
      const service = TestBed.inject(ParameterService);
      const parameter = { id: 3, name: 'iht', value: 10 } as Parameter;
      service.getParameters().subscribe();
      service.modifyParameter(parameter).subscribe();
      service.deleteParameter(parameter).subscribe();

      expectGet(API + 'parameters').flush([]);
      expect(http.match(API + 'parameters/3').map((r) => r.request.method)).toEqual([
        'PUT',
        'DELETE',
      ]);
    });
  });

  describe('OfficeService', () => {
    it('passes the autocomplete query as a filter and sorts results', () => {
      const service = TestBed.inject(OfficeService);
      let offices: unknown;
      service.getOffices({ originalEvent: new Event('input'), query: 'lis' } as AutoCompleteCompleteEvent).subscribe((o) => (offices = o));

      const req = http.expectOne((r) => r.url === API + 'offices/');
      expect(req.request.params.get('filter')).toBe('lis');
      req.flush(['Porto', 'Lisbon']);

      expect(offices).toEqual(['Lisbon', 'Porto']);
    });
  });

  describe('MailService', () => {
    it('posts the candidate notification with the current language', () => {
      const service = TestBed.inject(MailService);
      const proposal = makeProposal();
      let completed = false;
      service.notifyByEmail(proposal).subscribe({ complete: () => (completed = true) });

      const req = http.expectOne(API + 'send/candidate/?language=en');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(proposal);
      req.flush({});
      expect(completed).toBeTrue();
    });
  });
});

describe('ToastService', () => {
  let messages: jasmine.SpyObj<MessageService>;
  let service: ToastService;

  beforeEach(() => {
    messages = jasmine.createSpyObj<MessageService>('MessageService', ['add']);
    TestBed.configureTestingModule({
      providers: [{ provide: MessageService, useValue: messages }],
    });
    service = TestBed.inject(ToastService);
    spyOn(console, 'log');
  });

  const cases: [keyof ToastService, string, string][] = [
    ['success', 'success', 'Success'],
    ['info', 'info', 'Info'],
    ['warn', 'warn', 'Warn'],
    ['error', 'error', 'Error'],
  ];
  cases.forEach(([method, severity, summary]) => {
    it(`shows a ${severity} toast on the bottom-center channel`, () => {
      (service[method] as (m: string) => void)('hello');
      expect(messages.add).toHaveBeenCalledOnceWith({
        key: 'bc',
        severity,
        summary,
        detail: 'hello',
      });
    });
  });
});

describe('errorToastInterceptor', () => {
  let http: HttpTestingController;
  let client: HttpClient;
  let messages: jasmine.SpyObj<MessageService>;

  beforeEach(() => {
    messages = jasmine.createSpyObj<MessageService>('MessageService', ['add']);
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([errorToastInterceptor])),
        provideHttpClientTesting(),
        { provide: MessageService, useValue: messages },
      ],
    });
    http = TestBed.inject(HttpTestingController);
    client = TestBed.inject(HttpClient);
  });

  afterEach(() => http.verify());

  it('shows an error toast when an HTTP call fails', () => {
    client.get('/api/test').subscribe({ error: () => undefined });

    http.expectOne('/api/test').flush('nope', {
      status: 500,
      statusText: 'Server Error',
    });

    expect(messages.add).toHaveBeenCalledWith(
      jasmine.objectContaining({
        key: 'bc',
        severity: 'error',
        detail: 'Request failed. Please try again.',
      }),
    );
  });

  it('leaves the error toast to callers that handle the error themselves', () => {
    let failed = false;
    client.get('/api/test', { context: callerHandlesErrors() }).subscribe({
      error: () => (failed = true),
    });

    http.expectOne('/api/test').flush('nope', {
      status: 500,
      statusText: 'Server Error',
    });

    expect(failed).toBeTrue();
    expect(messages.add).not.toHaveBeenCalled();
  });
});
