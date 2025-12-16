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

@Component({
  selector: 'app-form-textarea',
  templateUrl: './form-textarea.html',
  styleUrl: './form-textarea.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => FormTextarea),
      multi: true
    }
  ]
})
export class FormTextarea implements ControlValueAccessor {
  /** Textarea label text */
  readonly label = input.required<string>();

  /** Placeholder text */
  readonly placeholder = input<string>('');

  /** Whether the textarea is required */
  readonly required = input<boolean>(false);

  /** Error message to display */
  readonly errorMessage = input<string>('');

  /** Hint text displayed below the textarea */
  readonly hint = input<string>('');

  /** Number of visible rows */
  readonly rows = input<number>(4);

  /** Maximum character count */
  readonly maxLength = input<number | null>(null);

  /** Unique ID for the textarea */
  readonly textareaId = input<string>(`textarea-${crypto.randomUUID().slice(0, 8)}`);

  /** Current value */
  protected readonly value = signal<string>('');

  /** Whether the textarea is focused */
  protected readonly isFocused = signal<boolean>(false);

  /** Whether the textarea is disabled */
  protected readonly isDisabled = signal<boolean>(false);

  /** Whether to show error state */
  protected readonly showError = computed(() => {
    return this.errorMessage() !== '' && !this.isFocused();
  });

  /** Character count */
  protected readonly charCount = computed(() => this.value().length);

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

  protected onInput(event: Event): void {
    const target = event.target as HTMLTextAreaElement;
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
