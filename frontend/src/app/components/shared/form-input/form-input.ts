import {
  ChangeDetectionStrategy,
  Component,
  computed,
  forwardRef,
  input,
  signal
} from '@angular/core';
import {
  ControlValueAccessor,
  NG_VALUE_ACCESSOR,
  ReactiveFormsModule
} from '@angular/forms';

export type InputType = 'text' | 'email' | 'password' | 'number' | 'tel' | 'url';

@Component({
  selector: 'app-form-input',
  templateUrl: './form-input.html',
  styleUrl: './form-input.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => FormInput),
      multi: true
    }
  ]
})
export class FormInput implements ControlValueAccessor {
  /** Input label text */
  readonly label = input.required<string>();

  /** Input type */
  readonly type = input<InputType>('text');

  /** Placeholder text */
  readonly placeholder = input<string>('');

  /** Whether the input is required */
  readonly required = input<boolean>(false);

  /** Error message to display */
  readonly errorMessage = input<string>('');

  /** Hint text displayed below the input */
  readonly hint = input<string>('');

  /** Whether to hide the label visually (keeps it in DOM for accessibility) */
  readonly hideLabel = input<boolean>(false);

  /** Autocomplete attribute */
  readonly autocomplete = input<string>('off');

  /** Unique ID for the input (auto-generated if not provided) */
  readonly inputId = input<string>(`input-${crypto.randomUUID().slice(0, 8)}`);

  /** Current input value */
  protected readonly value = signal<string>('');

  /** Whether the input is focused */
  protected readonly isFocused = signal<boolean>(false);

  /** Whether the input is disabled */
  protected readonly isDisabled = signal<boolean>(false);

  /** Whether to show error state */
  protected readonly showError = computed(() => {
    return this.errorMessage() !== '' && !this.isFocused();
  });

  private onChange: (value: string) => void = () => {};
  private onTouched: () => void = () => {};

  // ControlValueAccessor implementation
  writeValue(value: string): void {
    this.value.set(value ?? '');
  }

  registerOnChange(fn: (value: string) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.isDisabled.set(isDisabled);
  }

  protected onInput(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.value.set(target.value);
    this.onChange(target.value);
  }

  protected onFocus(): void {
    this.isFocused.set(true);
  }

  protected onBlur(): void {
    this.isFocused.set(false);
    this.onTouched();
  }
}
