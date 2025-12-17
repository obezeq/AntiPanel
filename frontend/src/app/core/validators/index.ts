// ============================================================================
// Sync Validators
// ============================================================================

// Password strength validator
export {
  passwordStrengthValidator,
  getPasswordStrengthErrors,
  type PasswordStrengthErrors,
  type PasswordStrengthConfig
} from './sync/password-strength.validator';

// Password match (cross-field) validator
export {
  passwordMatchValidator,
  getPasswordMismatchError
} from './sync/password-match.validator';

// Custom pattern validators
export {
  customPatternValidator,
  phoneValidator,
  urlValidator,
  slugValidator,
  lettersOnlyValidator,
  creditCardValidator,
  usZipCodeValidator,
  esPostalCodeValidator,
  noWhitespaceValidator,
  type PatternConfig
} from './sync/custom-pattern.validator';

// ============================================================================
// Async Validators
// ============================================================================

export {
  emailUniqueValidator,
  createEmailUniqueValidator
} from './async/email-unique.validator';
