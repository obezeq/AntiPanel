import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * Configuration for custom pattern validator
 */
export interface PatternConfig {
  pattern: RegExp;
  errorKey?: string;
  errorMessage?: string;
}

/**
 * Creates a custom pattern validator with configurable error key and message.
 * Unlike the built-in Validators.pattern, this allows custom error keys
 * for more specific error handling.
 *
 * @param config - Configuration object with pattern, error key, and message
 * @returns ValidatorFn that returns null if valid, or error object if invalid
 *
 * @example
 * // Phone number validation
 * this.form = this.fb.group({
 *   phone: ['', [
 *     customPatternValidator({
 *       pattern: /^[+]?[(]?[0-9]{1,3}[)]?[-\s.]?[0-9]{1,4}[-\s.]?[0-9]{1,4}[-\s.]?[0-9]{1,9}$/,
 *       errorKey: 'invalidPhone',
 *       errorMessage: 'Please enter a valid phone number'
 *     })
 *   ]]
 * });
 *
 * @example
 * // URL validation
 * this.form = this.fb.group({
 *   website: ['', [
 *     customPatternValidator({
 *       pattern: /^https?:\/\/(www\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b([-a-zA-Z0-9()@:%_+.~#?&//=]*)$/,
 *       errorKey: 'invalidUrl',
 *       errorMessage: 'Please enter a valid URL'
 *     })
 *   ]]
 * });
 */
export function customPatternValidator(config: PatternConfig): ValidatorFn {
  const { pattern, errorKey = 'pattern', errorMessage = 'Invalid format' } = config;

  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value as string;

    if (!value) {
      return null; // Let required validator handle empty values
    }

    if (!pattern.test(value)) {
      return {
        [errorKey]: {
          pattern: pattern.toString(),
          actualValue: value,
          message: errorMessage
        }
      };
    }

    return null;
  };
}

// ============================================================================
// Pre-configured pattern validators for common use cases
// ============================================================================

/**
 * Validates phone numbers in Spanish formats.
 * Normalizes input by removing spaces and hyphens before validation.
 *
 * Valid formats (all normalize to 9 digits + optional prefix):
 * - 666666666 (9 digits, no spaces)
 * - 666 666 666 (groups of 3)
 * - 666 66 66 66 (groups of 3-2-2-2, common Spanish format)
 * - +34 666666666 (international prefix)
 * - +34 666 666 666
 * - +34 666 66 66 66
 * - +34-666-66-66-66 (with hyphens)
 *
 * Invalid formats:
 * - 12345 (too short)
 * - +3411111666666666 (too many digits)
 */
export function phoneValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value as string;
    if (!value) return null;

    // Normalize: remove spaces and hyphens
    const normalized = value.replace(/[\s-]/g, '');

    // Pattern: optional international prefix (1-3 digits) + exactly 9 digits
    const pattern = /^(\+\d{1,3})?\d{9}$/;

    if (!pattern.test(normalized)) {
      return {
        invalidPhone: {
          pattern: pattern.toString(),
          actualValue: value,
          message: 'Introduce un telefono valido (ej: 666 666 666 o +34 666 66 66 66)'
        }
      };
    }
    return null;
  };
}

/**
 * Validates URLs (http or https).
 */
export function urlValidator(): ValidatorFn {
  return customPatternValidator({
    pattern: /^https?:\/\/(www\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b([-a-zA-Z0-9()@:%_+.~#?&//=]*)$/,
    errorKey: 'invalidUrl',
    errorMessage: 'Please enter a valid URL (http:// or https://)'
  });
}

/**
 * Validates alphanumeric strings with optional underscores and hyphens.
 * Useful for usernames or slugs.
 */
export function slugValidator(): ValidatorFn {
  return customPatternValidator({
    pattern: /^[a-zA-Z0-9_-]+$/,
    errorKey: 'invalidSlug',
    errorMessage: 'Only letters, numbers, underscores, and hyphens allowed'
  });
}

/**
 * Validates that the value contains only letters (no numbers or special characters).
 */
export function lettersOnlyValidator(): ValidatorFn {
  return customPatternValidator({
    pattern: /^[a-zA-ZÀ-ÿ\s]+$/,
    errorKey: 'lettersOnly',
    errorMessage: 'Only letters are allowed'
  });
}

/**
 * Validates credit card numbers (basic validation, 13-19 digits).
 * Note: For production, use a proper credit card validation library.
 */
export function creditCardValidator(): ValidatorFn {
  return customPatternValidator({
    pattern: /^[0-9]{13,19}$/,
    errorKey: 'invalidCreditCard',
    errorMessage: 'Please enter a valid credit card number'
  });
}

/**
 * Validates postal/zip codes (US format: 12345 or 12345-6789).
 */
export function usZipCodeValidator(): ValidatorFn {
  return customPatternValidator({
    pattern: /^\d{5}(-\d{4})?$/,
    errorKey: 'invalidZipCode',
    errorMessage: 'Please enter a valid ZIP code (e.g., 12345 or 12345-6789)'
  });
}

/**
 * Validates Spanish postal codes (5 digits).
 */
export function esPostalCodeValidator(): ValidatorFn {
  return customPatternValidator({
    pattern: /^(?:0[1-9]|[1-4]\d|5[0-2])\d{3}$/,
    errorKey: 'invalidPostalCode',
    errorMessage: 'Please enter a valid Spanish postal code'
  });
}

/**
 * Validates that the value does not contain any whitespace.
 */
export function noWhitespaceValidator(): ValidatorFn {
  return customPatternValidator({
    pattern: /^\S+$/,
    errorKey: 'hasWhitespace',
    errorMessage: 'No spaces allowed'
  });
}
