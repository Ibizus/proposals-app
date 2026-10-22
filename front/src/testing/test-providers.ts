import { EnvironmentProviders, Provider, importProvidersFrom } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { TranslateModule } from '@ngx-translate/core';
import { MessageService } from 'primeng/api';
import { errorToastInterceptor } from '../app/services/error-toast.interceptor';

/**
 * Providers shared by every component spec:
 * - HttpClient with the app's error-toast interceptor, backed by HttpTestingController
 *   (no real network calls),
 * - an empty router,
 * - noop animations (PrimeNG),
 * - ngx-translate with its default fake loader, so translation keys render as-is.
 */
export function provideTestDefaults(): (Provider | EnvironmentProviders)[] {
  return [
    provideHttpClient(withInterceptors([errorToastInterceptor])),
    provideHttpClientTesting(),
    provideRouter([]),
    provideNoopAnimations(),
    importProvidersFrom(TranslateModule.forRoot()),
    MessageService,
  ];
}
