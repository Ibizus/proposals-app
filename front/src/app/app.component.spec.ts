import { TestBed } from '@angular/core/testing';
import { HttpClientModule } from '@angular/common/http';
import { AppComponent } from './app.component';
import { provideTestDefaults } from '../testing/test-providers';

describe('AppComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: provideTestDefaults(),
    })
      // AppComponent imports HttpClientModule, which would shadow the testing
      // backend with a real one; drop it so no request can leave the browser.
      .overrideComponent(AppComponent, { remove: { imports: [HttpClientModule] } })
      .compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it(`should have the 'front' title`, () => {
    const fixture = TestBed.createComponent(AppComponent);
    expect(fixture.componentInstance.title).toEqual('front');
  });

  it('should render the navbar and the router outlet', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('app-navbar')).not.toBeNull();
    expect(compiled.querySelector('router-outlet')).not.toBeNull();
  });

  it('should build the main menu on init with Proposals active', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    const app = fixture.componentInstance;
    expect(app.items.map((i) => i.label)).toEqual([
      'Proposals',
      'Candidates',
      'Extras',
      'Taxes',
    ]);
    expect(app.activeItem).toBe(app.items[0]);
  });
});
