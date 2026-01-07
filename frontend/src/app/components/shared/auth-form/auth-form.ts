import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  input,
  output,
  signal
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { startWith } from 'rxjs';
import { NgIcon } from '@ng-icons/core';
import { FormInput } from '../form-input/form-input';
import {
  passwordStrengthValidator,
  passwordMatchValidator,
  emailUniqueValidator,
  getPasswordStrengthErrors,
  type PasswordStrengthErrors
} from '../../../core/validators';

export type AuthFormMode = 'login' | 'register';

export interface AuthFormData {
  email: string;
  password: string;
  confirmPassword?: string;
}

@Component({
  selector: 'app-auth-form',
  templateUrl: './auth-form.html',
  styleUrl: './auth-form.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, RouterLink, NgIcon, FormInput]
})
export class AuthForm {
  private readonly fb = inject(NonNullableFormBuilder);

  /** Form mode: login or register */
  readonly mode = input<AuthFormMode>('login');

  /** Whether the form is loading */
  readonly loading = input<boolean>(false);

  /** Server error message to display */
  readonly serverError = input<string>('');

  /** Emits form data on valid submit */
  readonly formSubmit = output<AuthFormData>();

  /** Whether we're in register mode */
  protected readonly isRegisterMode = computed(() => this.mode() === 'register');

  /** Header title based on mode */
  protected readonly headerTitle = computed(() =>
    this.isRegisterMode() ? 'JOIN' : 'ACCESS'
  );

  /** Header subtitle based on mode */
  protected readonly headerSubtitle = computed(() =>
    this.isRegisterMode()
      ? 'Create your AntiPanel account'
      : 'Welcome back to AntiPanel'
  );

  /** Form title (legend text - visually hidden but accessible) */
  protected readonly formTitle = computed(() =>
    this.isRegisterMode() ? 'Registration Form' : 'Login Form'
  );

  /** Submit button text - "JOIN" for register, "ACCESS" for login */
  protected readonly submitButtonText = computed(() =>
    this.isRegisterMode() ? 'JOIN' : 'ACCESS'
  );

  /** Link text for mode switch */
  protected readonly switchModeText = computed(() =>
    this.isRegisterMode()
      ? 'Already have an account?'
      : 'Need an account?'
  );

  /** Link label for mode switch */
  protected readonly switchModeLink = computed(() =>
    this.isRegisterMode() ? 'LOGIN' : 'JOIN ANTIPANEL'
  );

  /** Route for mode switch */
  protected readonly switchModeRoute = computed(() =>
    this.isRegisterMode() ? '/login' : '/register'
  );

  /**
   * Reactive form with type-safe controls.
   * Using NonNullableFormBuilder for better type inference.
   *
   * Note: passwordStrengthValidator and passwordMatchValidator are added
   * dynamically in register mode only via effect.
   */
  protected readonly form = this.fb.group({
    email: ['', {
      validators: [Validators.required, Validators.email],
      updateOn: 'blur' as const
    }],
    password: ['', [
      Validators.required,
      Validators.minLength(8)
    ]],
    confirmPassword: [''],
    termsAccepted: [false]
  });

  /**
   * Signal from form valueChanges to enable reactive computations.
   * This is necessary because computed() doesn't react to form value changes directly.
   */
  private readonly formValues = toSignal(
    this.form.valueChanges.pipe(startWith(this.form.getRawValue())),
    { initialValue: this.form.getRawValue() }
  );

  // =========================================================================
  // Computed validation states
  // =========================================================================

  /** Whether email field has been touched */
  protected readonly emailTouched = signal(false);

  /** Whether password field has been touched */
  protected readonly passwordTouched = signal(false);

  /** Whether confirm password field has been touched */
  protected readonly confirmPasswordTouched = signal(false);

  /** Whether confirm password field has been modified (for real-time validation) */
  protected readonly confirmPasswordDirty = signal(false);

  /** Whether terms checkbox has been interacted with */
  protected readonly termsAcceptedTouched = signal(false);

  /** Email validation pending state */
  protected readonly emailPending = computed(() => {
    return this.form.controls.email.pending;
  });

  /** Whether email is valid (for showing success state) */
  protected readonly emailValid = computed(() => {
    const control = this.form.controls.email;
    return control.valid && this.emailTouched() && !control.pending;
  });

  /** Whether password is valid */
  protected readonly passwordValid = computed(() => {
    const control = this.form.controls.password;
    return control.valid && this.passwordTouched();
  });

  /** Whether confirm password is valid - checks after user starts typing */
  protected readonly confirmPasswordValid = computed(() => {
    if (!this.isRegisterMode()) return true;

    // Only show valid state after user has interacted
    if (!this.confirmPasswordTouched() && !this.confirmPasswordDirty()) return false;

    // Use formValues() for reactivity with OnPush change detection
    const values = this.formValues();
    const passwordValue = values?.password || '';
    const confirmValue = values?.confirmPassword || '';

    // Valid if both have values and they match
    return confirmValue && passwordValue === confirmValue;
  });

  /** Whether terms checkbox is checked */
  protected readonly termsAcceptedValid = computed(() => {
    if (!this.isRegisterMode()) return true;
    return !!this.formValues()?.termsAccepted;
  });

  /** Terms acceptance error message */
  protected readonly termsAcceptedError = computed(() => {
    if (!this.isRegisterMode()) return '';
    if (!this.termsAcceptedTouched()) return '';
    const accepted = this.formValues()?.termsAccepted;
    if (!accepted) return 'You must accept the terms of service';
    return '';
  });

  /** Whether form is currently validating */
  protected readonly isValidating = computed(() => {
    // Trigger reactivity - form state changes when values change
    this.formValues();
    return this.form.pending;
  });

  /** Whether submit button should be disabled */
  protected readonly submitDisabled = computed(() => {
    // Trigger reactivity - form state changes when values change
    this.formValues();
    return this.loading() || this.form.invalid || this.form.pending;
  });

  // =========================================================================
  // Error messages
  // =========================================================================

  /** Email error message */
  protected readonly emailError = computed(() => {
    const control = this.form.controls.email;

    if (!this.emailTouched() || control.pending) return '';

    if (control.hasError('required')) {
      return 'Email is required';
    }
    if (control.hasError('email')) {
      return 'Please enter a valid email address';
    }
    if (control.hasError('emailTaken')) {
      return 'This email is already registered';
    }
    if (control.hasError('emailCheckFailed')) {
      return 'Unable to verify email. Please try again.';
    }
    return '';
  });

  /** Password error message */
  protected readonly passwordError = computed(() => {
    const control = this.form.controls.password;

    if (!this.passwordTouched()) return '';

    if (control.hasError('required')) {
      return 'Password is required';
    }
    if (control.hasError('minlength')) {
      return 'Password must be at least 8 characters';
    }
    if (control.hasError('passwordStrength')) {
      const errors = control.getError('passwordStrength') as PasswordStrengthErrors;
      const messages = getPasswordStrengthErrors(errors);
      return `Password must have: ${messages.join(', ')}`;
    }
    return '';
  });

  /** Confirm password error message - shows in real-time for better UX */
  protected readonly confirmPasswordError = computed(() => {
    if (!this.isRegisterMode()) return '';

    // Use formValues() for reactivity with OnPush change detection
    const values = this.formValues();
    const passwordValue = values?.password || '';
    const confirmValue = values?.confirmPassword || '';

    // If user hasn't interacted with the field yet, don't show errors
    if (!this.confirmPasswordTouched() && !this.confirmPasswordDirty()) return '';

    // If field is empty and user has started typing (or blurred)
    if (!confirmValue) {
      return 'Please confirm your password';
    }

    // Show mismatch error in real-time while typing
    if (confirmValue && passwordValue !== confirmValue) {
      return 'Passwords do not match';
    }

    return '';
  });

  /** Password strength hints for UI - always show in register mode for better UX */
  protected readonly passwordStrengthHints = computed(() => {
    // Only show hints in register mode
    if (!this.isRegisterMode()) return [];

    // Use formValues() for reactivity with OnPush change detection
    const values = this.formValues();
    const value = values?.password || '';

    // Always return hints in register mode so user knows requirements upfront
    return [
      { label: 'At least 8 characters', valid: value.length >= 8 },
      { label: 'Uppercase letter', valid: /[A-Z]/.test(value) },
      { label: 'Lowercase letter', valid: /[a-z]/.test(value) },
      { label: 'Number', valid: /\d/.test(value) },
      { label: 'Special character', valid: /[!@#$%^&*()\-_=+\[\]{}|;:'",.<>?/\\~`]/.test(value) }
    ];
  });

  constructor() {
    // Effect to apply/remove validators based on mode
    effect(() => {
      const isRegister = this.isRegisterMode();
      const emailControl = this.form.controls.email;
      const passwordControl = this.form.controls.password;
      const termsControl = this.form.controls.termsAccepted;

      if (isRegister) {
        // Enable async validator for email in register mode
        emailControl.setAsyncValidators([emailUniqueValidator(500)]);
        // Add password strength validator in register mode
        passwordControl.setValidators([
          Validators.required,
          Validators.minLength(8),
          passwordStrengthValidator()
        ]);
        // Add password match validator at form group level (register mode only)
        this.form.setValidators([passwordMatchValidator('password', 'confirmPassword')]);
        // Require terms acceptance in register mode
        termsControl.setValidators([Validators.requiredTrue]);
      } else {
        // Remove async validator in login mode
        emailControl.clearAsyncValidators();
        // Remove password strength validator in login mode (only required + minLength)
        passwordControl.setValidators([
          Validators.required,
          Validators.minLength(8)
        ]);
        // Remove form group validators in login mode
        this.form.clearValidators();
        // Remove terms validator in login mode
        termsControl.clearValidators();
      }
      emailControl.updateValueAndValidity();
      passwordControl.updateValueAndValidity();
      termsControl.updateValueAndValidity();
      this.form.updateValueAndValidity();
    });
  }

  // =========================================================================
  // Event handlers
  // =========================================================================

  protected onEmailBlur(): void {
    this.emailTouched.set(true);
  }

  protected onPasswordBlur(): void {
    this.passwordTouched.set(true);
  }

  protected onConfirmPasswordBlur(): void {
    this.confirmPasswordTouched.set(true);
  }

  protected onConfirmPasswordInput(): void {
    this.confirmPasswordDirty.set(true);
  }

  protected onTermsBlur(): void {
    this.termsAcceptedTouched.set(true);
  }

  protected onSubmit(): void {
    // Mark all as touched for validation display
    this.emailTouched.set(true);
    this.passwordTouched.set(true);
    if (this.isRegisterMode()) {
      this.confirmPasswordTouched.set(true);
      this.termsAcceptedTouched.set(true);
    }

    // Mark form as touched to trigger validation
    this.form.markAllAsTouched();

    // Check if form is valid
    if (this.form.invalid || this.form.pending) {
      return;
    }

    const formData: AuthFormData = {
      email: this.form.controls.email.value,
      password: this.form.controls.password.value
    };

    if (this.isRegisterMode()) {
      formData.confirmPassword = this.form.controls.confirmPassword.value;
    }

    this.formSubmit.emit(formData);
  }

  /**
   * Reset the form to initial state.
   * Useful after successful submission or mode change.
   */
  resetForm(): void {
    this.form.reset();
    this.emailTouched.set(false);
    this.passwordTouched.set(false);
    this.confirmPasswordTouched.set(false);
    this.confirmPasswordDirty.set(false);
    this.termsAcceptedTouched.set(false);
  }
}
