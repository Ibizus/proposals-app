import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { ToastService } from './toast.service';

/**
 * Shows a global error toast for every failed HTTP call, so components only
 * have to report success (and non-HTTP validation errors) themselves.
 */
export const errorToastInterceptor: HttpInterceptorFn = (req, next) => {
  const toastService = inject(ToastService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      toastService.error('Request failed. Please try again.');
      return throwError(() => error);
    }),
  );
};
