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

// NIF/NIE validator (Spanish ID)
export {
  nifValidator,
  getNifError,
  isValidNif,
  type NifValidationError
} from './sync/nif.validator';

// ============================================================================
// Async Validators
// ============================================================================

export {
  emailUniqueValidator,
  createEmailUniqueValidator
} from './async/email-unique.validator';

// Username availability validator
export {
  usernameAvailableValidator,
  createUsernameAvailableValidator,
  usernameFormatValidator,
  getUsernameTakenError,
  getUsernameFormatError,
  type UsernameCheckService,
  type UsernameValidatorConfig
} from './async/username-available.validator';
