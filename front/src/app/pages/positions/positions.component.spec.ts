import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpTestingController } from '@angular/common/http/testing';
import { provideTestDefaults } from '../../../testing/test-providers';
import { makePosition } from '../../../testing/fixtures';
import { environment } from '../../../environments/environment';

import { PositionsComponent } from './positions.component';

describe('PositionsComponent', () => {
  let component: PositionsComponent;
  let fixture: ComponentFixture<PositionsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PositionsComponent],
      providers: provideTestDefaults(),
    }).compileComponents();

    fixture = TestBed.createComponent(PositionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('sorts the candidates column by candidate count', () => {
    TestBed.inject(HttpTestingController)
      .expectOne(environment.API_ENDPOINT + 'positions/?page=0&size=10')
      .flush({
        positions: [
          makePosition({ id: 1, candidates: [10, 11] }),
          makePosition({ id: 2, candidates: [2] }),
          makePosition({ id: 3, candidates: [1, 2, 3] }),
        ],
        currentPage: 0,
        totalItems: 3,
        totalPages: 1,
      });
    fixture.detectChanges();

    const host: HTMLElement = fixture.nativeElement;
    host.querySelectorAll<HTMLElement>('th')[3].click();
    fixture.detectChanges();

    const counts = Array.from(host.querySelectorAll('tbody tr')).map((row) =>
      row.querySelectorAll('td')[3]?.textContent?.trim(),
    );
    expect(counts).toEqual(['1', '2', '3']);
  });
});
