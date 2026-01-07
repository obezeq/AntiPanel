import { AbstractControl, AsyncValidatorFn, ValidationErrors } from '@angular/forms';
import { Observable, of, timer } from 'rxjs';
import { map, switchMap, catchError } from 'rxjs/operators';

/**
 * Service interface for checking email existence.
 * This allows dependency injection of the actual service.
 */
export interface EmailCheckService {
  checkEmailExists(email: string): Observable<boolean>;
}

/**
 * Creates an async validator that checks if an email is already taken.
 * This version requires an EmailCheckService to be injected.
 *
 * @param emailCheckService - Service that implements checkEmailExists method
 * @param debounceMs - Debounce time in milliseconds (default: 500)
 * @returns AsyncValidatorFn
 *
 * @example
 * // In a component
 * private authService = inject(AuthService);
 *
 * this.form = this.fb.group({
 *   email: ['', {
 *     validators: [Validators.required, Validators.email],
 *     asyncValidators: [createEmailUniqueValidator(this.authService)],
 *     updateOn: 'blur'
 *   }]
 * });
 */
export function createEmailUniqueValidator(
  emailCheckService: EmailCheckService,
  debounceMs: number = 500
): AsyncValidatorFn {
  return (control: AbstractControl): Observable<ValidationErrors | null> => {
    const email = control.value as string;

    // Don't validate empty or invalid email format
    if (!email || !isValidEmailFormat(email)) {
      return of(null);
    }

    return timer(debounceMs).pipe(
      switchMap(() => emailCheckService.checkEmailExists(email)),
      map(exists => (exists ? { emailTaken: true } : null)),
      catchError(() => of({ emailCheckFailed: true })) // Return error so UI can inform user
    );
  };
}

/**
 * Simulated async validator for demonstration purposes.
 * This validator simulates an API call with a configurable delay
 * and uses a list of "taken" emails for testing.
 *
 * @param debounceMs - Debounce time in milliseconds (default: 500)
 * @returns AsyncValidatorFn
 *
 * @example
 * // In a component (for demonstration/testing)
 * this.form = this.fb.group({
 *   email: ['', {
 *     validators: [Validators.required, Validators.email],
 *     asyncValidators: [emailUniqueValidator()],
 *     updateOn: 'blur'
 *   }]
 * });
 */
export function emailUniqueValidator(debounceMs: number = 500): AsyncValidatorFn {
  // Simulated list of taken emails for demonstration
  const takenEmails = [
    'admin@antipanel.com',
    'test@test.com',
    'user@example.com',
    'demo@demo.com'
  ];

  return (control: AbstractControl): Observable<ValidationErrors | null> => {
    const email = control.value as string;

    // Don't validate empty or invalid email format
    if (!email || !isValidEmailFormat(email)) {
      return of(null);
    }

    // Simulate API call with delay
    return timer(debounceMs).pipe(
      map(() => {
        const emailLower = email.toLowerCase().trim();
        const isTaken = takenEmails.includes(emailLower);
        return isTaken ? { emailTaken: true } : null;
      }),
      catchError(() => of({ emailCheckFailed: true }))
    );
  };
}

/**
 * Helper function to check if email has valid format.
 * Used to avoid unnecessary API calls for obviously invalid emails.
 */
function isValidEmailFormat(email: string): boolean {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
}

/**
 * Returns the error message for email taken error.
 *
 * @returns Error message string
 */
export function getEmailTakenError(): string {
  return 'This email is already registered';
}

/**
 * Returns the error message for email check failure.
 *
 * @returns Error message string
 */
export function getEmailCheckFailedError(): string {
  return 'Unable to verify email. Please try again.';
}
