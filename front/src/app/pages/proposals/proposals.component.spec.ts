import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpTestingController } from '@angular/common/http/testing';
import { provideTestDefaults } from '../../../testing/test-providers';
import { environment } from '../../../environments/environment';
import { ToastService } from '../../services/toast.service';
import { Proposal } from '../../utils/types';
import { makeProposal } from '../../../testing/fixtures';

import { ProposalsComponent } from './proposals.component';

const API = environment.API_ENDPOINT;

describe('ProposalsComponent', () => {
  let component: ProposalsComponent;
  let fixture: ComponentFixture<ProposalsComponent>;
  let http: HttpTestingController;
  let toast: ToastService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProposalsComponent],
      providers: provideTestDefaults(),
    }).compileComponents();

    http = TestBed.inject(HttpTestingController);
    toast = TestBed.inject(ToastService);
    fixture = TestBed.createComponent(ProposalsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => http.verify());

  it('stops the loading overlay when the list request fails', () => {
    http
      .expectOne(API + 'proposals/?page=0&size=10')
      .flush('nope', { status: 500, statusText: 'Server Error' });

    expect(component.isLoading()).toBeFalse();
  });

  it('opens and closes the delete dialog', () => {
    http
      .expectOne(API + 'proposals/?page=0&size=10')
      .flush({ proposals: [], currentPage: 0, totalItems: 0 });

    component.showDialog({ id: 7 } as Proposal);
    expect(component.visible()).toBeTrue();
    expect(component.proposalId).toBe(7);

    spyOn(toast, 'success');
    component.deleteProposal();
    expect(component.visible()).toBeFalse();
    http.expectOne(API + 'proposals/7').flush({});
    http
      .expectOne(API + 'proposals/?page=0&size=10')
      .flush({ proposals: [], currentPage: 0, totalItems: 0 });
  });

  it('reports a failed deletion with a specific toast', () => {
    http
      .expectOne(API + 'proposals/?page=0&size=10')
      .flush({ proposals: [], currentPage: 0, totalItems: 0 });
    const error = spyOn(toast, 'error');

    component.showDialog({ id: 7 } as Proposal);
    component.deleteProposal();
    http.expectOne(API + 'proposals/7').flush('nope', { status: 500, statusText: 'Server Error' });

    expect(error).toHaveBeenCalledOnceWith('The proposal could not be deleted');
  });

  it('duplicates a proposal preserving role, njoyn, dates and createdAt', () => {
    http
      .expectOne(API + 'proposals/?page=0&size=10')
      .flush({ proposals: [], currentPage: 0, totalItems: 0 });
    spyOn(toast, 'success');

    const existing = makeProposal({
      njoyn: 'NJ-DUP',
      startDate: '2024-06-01',
      dueDate: '2024-09-30',
      indefinite: false,
    });
    component.duplicateProposal(existing);

    const post = http.expectOne(
      (req) => req.method === 'POST' && req.url === API + 'proposals/',
    );
    const body = post.request.body as Proposal;
    expect(body.id).toBe(0);
    expect(body.njoyn).toBe('NJ-DUP');
    expect(body.role).toBe(existing.role);
    expect(new Date(body.startDate).toISOString()).toBe(new Date('2024-06-01').toISOString());
    expect(new Date(body.dueDate).toISOString()).toBe(new Date('2024-09-30').toISOString());
    expect(typeof body.createdAt).toBe('string');

    post.flush({});
    http
      .expectOne(API + 'proposals/?page=0&size=10')
      .flush({ proposals: [], currentPage: 0, totalItems: 0 });
  });
});
