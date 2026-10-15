import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideTestDefaults } from '../../../testing/test-providers';
import { makeCandidate } from '../../../testing/fixtures';

import { CandidateCardComponent } from './candidate-card.component';

describe('CandidateCardComponent', () => {
  let component: CandidateCardComponent;
  let fixture: ComponentFixture<CandidateCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CandidateCardComponent],
      providers: provideTestDefaults(),
    }).compileComponents();

    fixture = TestBed.createComponent(CandidateCardComponent);
    component = fixture.componentInstance;
  });

  function render(maritalStatus: number) {
    fixture.componentRef.setInput('candidate', makeCandidate({ maritalStatus, dependentsNumber: 2 }));
    fixture.detectChanges();
    return (fixture.nativeElement as HTMLElement).textContent ?? '';
  }

  it('renders the candidate details', () => {
    const text = render(0);
    expect(text).toContain('Ana Silva');
    expect(text).toContain('12345678A');
    expect(text).toContain('ana.silva@example.com');
    expect(text).toContain('W-001');
  });

  // The test translate loader returns keys unchanged.
  const statuses: [number, string][] = [
    [0, 'UNMARRIED'],
    [1, 'MARRIED ONE'],
    [2, 'MARRIED TWO'],
    [9, ''],
  ];
  statuses.forEach(([code, key]) => {
    it(`maps marital status ${code} to '${key}'`, () => {
      render(code);
      expect(component.maritalStatus).toBe(key);
    });
  });

  it('updates the marital status when the candidate input changes', () => {
    render(0);
    render(1);
    expect(component.maritalStatus).toBe('MARRIED ONE');
  });
});
