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

export interface SelectOption {
  value: string;
  label: string;
  disabled?: boolean;
}

@Component({
  selector: 'app-form-select',
  templateUrl: './form-select.html',
  styleUrl: './form-select.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => FormSelect),
      multi: true
    }
  ]
})
export class FormSelect implements ControlValueAccessor {
  /** Select label text */
  readonly label = input.required<string>();

  /** Available options */
  readonly options = input.required<SelectOption[]>();

  /** Placeholder text */
  readonly placeholder = input<string>('Select an option');

  /** Whether the select is required */
  readonly required = input<boolean>(false);

  /** Error message to display */
  readonly errorMessage = input<string>('');

  /** Hint text displayed below the select */
  readonly hint = input<string>('');

  /** Unique ID for the select */
  readonly selectId = input<string>(`select-${crypto.randomUUID().slice(0, 8)}`);

  /** Current value */
  protected readonly value = signal<string>('');

  /** Whether the select is focused */
  protected readonly isFocused = signal<boolean>(false);

  /** Whether the select is disabled */
  protected readonly isDisabled = signal<boolean>(false);

  /** Whether to show error state */
  protected readonly showError = computed(() => {
    return this.errorMessage() !== '' && !this.isFocused();
  });

  private onChange: (value: string) => void = () => {};
  private onTouched: () => void = () => {};

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

  protected onSelectionChange(event: Event): void {
    const target = event.target as HTMLSelectElement;
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
