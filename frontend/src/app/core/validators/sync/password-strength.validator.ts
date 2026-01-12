import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * Password strength validation errors
 */
export interface PasswordStrengthErrors {
  minLength?: boolean;
  uppercase?: boolean;
  lowercase?: boolean;
  number?: boolean;
  special?: boolean;
}

/**
 * Configuration options for password strength validator
 */
export interface PasswordStrengthConfig {
  minLength?: number;
  requireUppercase?: boolean;
  requireLowercase?: boolean;
  requireNumber?: boolean;
  requireSpecial?: boolean;
}

const DEFAULT_CONFIG: Required<PasswordStrengthConfig> = {
  minLength: 8,
  requireUppercase: true,
  requireLowercase: true,
  requireNumber: true,
  requireSpecial: true
};

/**
 * Validates password strength based on configurable requirements.
 *
 * Default requirements:
 * - Minimum 8 characters
 * - At least one uppercase letter
 * - At least one lowercase letter
 * - At least one number
 * - At least one special character (!@#$%^&*()-_=+[]{}|;:'",.<>?/\~`)
 *
 * @param config - Optional configuration to customize validation rules
 * @returns ValidatorFn that returns null if valid, or PasswordStrengthErrors if invalid
 *
 * @example
 * // With default config
 * this.form = this.fb.group({
 *   password: ['', [Validators.required, passwordStrengthValidator()]]
 * });
 *
 * @example
 * // With custom config
 * this.form = this.fb.group({
 *   password: ['', [passwordStrengthValidator({ minLength: 10, requireSpecial: false })]]
 * });
 */
export function passwordStrengthValidator(config?: PasswordStrengthConfig): ValidatorFn {
  const finalConfig = { ...DEFAULT_CONFIG, ...config };

  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value as string;

    if (!value) {
      return null; // Let required validator handle empty values
    }

    const errors: PasswordStrengthErrors = {};

    // Check minimum length
    if (value.length < finalConfig.minLength) {
      errors.minLength = true;
    }

    // Check for uppercase letter
    if (finalConfig.requireUppercase && !/[A-Z]/.test(value)) {
      errors.uppercase = true;
    }

    // Check for lowercase letter
    if (finalConfig.requireLowercase && !/[a-z]/.test(value)) {
      errors.lowercase = true;
    }

    // Check for number
    if (finalConfig.requireNumber && !/\d/.test(value)) {
      errors.number = true;
    }

    // Check for special character (expanded to include common special chars)
    if (finalConfig.requireSpecial && !/[!@#$%^&*()\-_=+\[\]{}|;:'",.<>?/\\~`]/.test(value)) {
      errors.special = true;
    }

    return Object.keys(errors).length > 0 ? { passwordStrength: errors } : null;
  };
}

/**
 * Returns a human-readable error message for password strength validation.
 *
 * @param errors - The PasswordStrengthErrors object from validation
 * @returns Array of error messages describing what requirements are not met
 */
export function getPasswordStrengthErrors(errors: PasswordStrengthErrors): string[] {
  const messages: string[] = [];

  if (errors.minLength) {
    messages.push('At least 8 characters');
  }
  if (errors.uppercase) {
    messages.push('At least one uppercase letter');
  }
  if (errors.lowercase) {
    messages.push('At least one lowercase letter');
  }
  if (errors.number) {
    messages.push('At least one number');
  }
  if (errors.special) {
    messages.push('At least one special character');
  }

  return messages;
}
