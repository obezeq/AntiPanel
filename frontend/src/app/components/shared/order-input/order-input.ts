import {
  ChangeDetectionStrategy,
  Component,
  forwardRef,
  input,
  output,
  signal
} from '@angular/core';
import {
  ControlValueAccessor,
  NG_VALUE_ACCESSOR,
  ReactiveFormsModule
} from '@angular/forms';

@Component({
  selector: 'app-order-input',
  templateUrl: './order-input.html',
  styleUrl: './order-input.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => OrderInput),
      multi: true
    }
  ]
})
export class OrderInput implements ControlValueAccessor {
  /** Placeholder text */
  readonly placeholder = input<string>('e.g., 1000 Instagram followers for @username');

  /** Whether the input is disabled */
  readonly disabled = input<boolean>(false);

  /** Submit event when user presses Enter */
  readonly submitOrder = output<string>();

  /** Current value */
  protected readonly value = signal<string>('');

  /** Whether the input is focused */
  protected readonly isFocused = signal<boolean>(false);

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
    // Handled via input
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

  protected onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && this.value().trim()) {
      event.preventDefault();
      this.submitOrder.emit(this.value());
    }
  }

  protected onSubmitClick(): void {
    if (this.value().trim()) {
      this.submitOrder.emit(this.value());
    }
  }
}
