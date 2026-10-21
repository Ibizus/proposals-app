import {
  HttpContext,
  HttpContextToken,
  HttpErrorResponse,
  HttpInterceptorFn,
} from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { ToastService } from './toast.service';

const CALLER_HANDLES_ERRORS = new HttpContextToken<boolean>(() => false);

/**
 * Request context for callers that report their own, context-specific error
 * toast, so the generic fallback toast is not shown on top of it.
 */
export function callerHandlesErrors(): HttpContext {
  return new HttpContext().set(CALLER_HANDLES_ERRORS, true);
}

/**
 * Shows a generic error toast for every failed HTTP call whose caller has not
 * opted out with `callerHandlesErrors()`.
 */
export const errorToastInterceptor: HttpInterceptorFn = (req, next) => {
  const toastService = inject(ToastService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (!req.context.get(CALLER_HANDLES_ERRORS)) {
        toastService.error('Request failed. Please try again.');
      }
      return throwError(() => error);
    }),
  );
};
