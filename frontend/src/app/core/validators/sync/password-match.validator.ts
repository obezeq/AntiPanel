import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * Cross-field validator that checks if two password fields match.
 * This validator should be applied at the FormGroup level, not individual controls.
 *
 * @param passwordField - Name of the password control (default: 'password')
 * @param confirmField - Name of the confirmation control (default: 'confirmPassword')
 * @returns ValidatorFn that returns null if passwords match, or { passwordMismatch: true } if they don't
 *
 * @example
 * // Basic usage with default field names
 * this.form = this.fb.group({
 *   password: ['', Validators.required],
 *   confirmPassword: ['', Validators.required]
 * }, {
 *   validators: [passwordMatchValidator()]
 * });
 *
 * @example
 * // Custom field names
 * this.form = this.fb.group({
 *   newPassword: ['', Validators.required],
 *   repeatPassword: ['', Validators.required]
 * }, {
 *   validators: [passwordMatchValidator('newPassword', 'repeatPassword')]
 * });
 *
 * @example
 * // Checking for the error in template
 * <div *ngIf="form.hasError('passwordMismatch')">
 *   Passwords do not match
 * </div>
 */
export function passwordMatchValidator(
  passwordField: string = 'password',
  confirmField: string = 'confirmPassword'
): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const password = control.get(passwordField);
    const confirmPassword = control.get(confirmField);

    // Don't validate if controls don't exist
    if (!password || !confirmPassword) {
      return null;
    }

    // Don't validate if either field is empty (let required validator handle that)
    if (!password.value || !confirmPassword.value) {
      return null;
    }

    // Check if passwords match
    if (password.value !== confirmPassword.value) {
      // Set error on the confirm password control for styling purposes
      // Use null-safe spread to avoid issues with null errors
      const existingErrors = confirmPassword.errors || {};
      confirmPassword.setErrors({
        ...existingErrors,
        passwordMismatch: true
      });
      return { passwordMismatch: true };
    }

    // If passwords match, remove the mismatch error from confirmPassword
    if (confirmPassword.hasError('passwordMismatch')) {
      const errors = { ...confirmPassword.errors };
      delete errors['passwordMismatch'];
      confirmPassword.setErrors(Object.keys(errors).length > 0 ? errors : null);
    }

    return null;
  };
}

/**
 * Returns the error message for password mismatch.
 *
 * @returns Error message string
 */
export function getPasswordMismatchError(): string {
  return 'Passwords do not match';
}
