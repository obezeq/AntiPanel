import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  input,
  output,
  signal
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { FormInput } from '../form-input/form-input';

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
  imports: [ReactiveFormsModule, RouterLink, FormInput]
})
export class AuthForm {
  private readonly fb = inject(FormBuilder);

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

  /** Submit button text */
  protected readonly submitButtonText = computed(() => 'ACCESS');

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

  /** Reactive form */
  protected readonly form = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
    confirmPassword: ['']
  });

  /** Email error message */
  protected readonly emailError = signal<string>('');

  /** Password error message */
  protected readonly passwordError = signal<string>('');

  /** Confirm password error message */
  protected readonly confirmPasswordError = signal<string>('');

  protected onSubmit(): void {
    this.clearErrors();

    if (!this.validateForm()) {
      return;
    }

    const formData: AuthFormData = {
      email: this.form.value.email ?? '',
      password: this.form.value.password ?? ''
    };

    if (this.isRegisterMode()) {
      formData.confirmPassword = this.form.value.confirmPassword ?? '';
    }

    this.formSubmit.emit(formData);
  }

  private validateForm(): boolean {
    let isValid = true;
    const { email, password, confirmPassword } = this.form.controls;

    // Email validation
    if (email.hasError('required')) {
      this.emailError.set('Email is required');
      isValid = false;
    } else if (email.hasError('email')) {
      this.emailError.set('Please enter a valid email address');
      isValid = false;
    }

    // Password validation
    if (password.hasError('required')) {
      this.passwordError.set('Password is required');
      isValid = false;
    } else if (password.hasError('minlength')) {
      this.passwordError.set('Password must be at least 8 characters');
      isValid = false;
    }

    // Confirm password validation (register mode only)
    if (this.isRegisterMode()) {
      if (!confirmPassword.value) {
        this.confirmPasswordError.set('Please confirm your password');
        isValid = false;
      } else if (confirmPassword.value !== password.value) {
        this.confirmPasswordError.set('Passwords do not match');
        isValid = false;
      }
    }

    return isValid;
  }

  private clearErrors(): void {
    this.emailError.set('');
    this.passwordError.set('');
    this.confirmPasswordError.set('');
  }
}
