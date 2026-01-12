import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

export type ButtonVariant = 'primary' | 'secondary' | 'ghost' | 'danger';
export type ButtonSize = 'sm' | 'md' | 'lg';
export type ButtonType = 'button' | 'submit' | 'reset';

@Component({
  selector: 'app-button',
  templateUrl: './button.html',
  styleUrl: './button.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    '[class.button--full-width-host]': 'fullWidth()'
  }
})
export class Button {
  /** Button visual variant */
  readonly variant = input<ButtonVariant>('primary');

  /** Button size */
  readonly size = input<ButtonSize>('md');

  /** Button type attribute */
  readonly type = input<ButtonType>('button');

  /** Whether the button is disabled */
  readonly disabled = input<boolean>(false);

  /** Whether the button is in loading state */
  readonly loading = input<boolean>(false);

  /** Whether the button should take full width */
  readonly fullWidth = input<boolean>(false);

  /** Accessible label for icon-only buttons */
  readonly ariaLabel = input<string>('');

  /** Click event emitter */
  readonly buttonClick = output<MouseEvent>();

  protected onClick(event: MouseEvent): void {
    if (!this.disabled() && !this.loading()) {
      this.buttonClick.emit(event);
    }
  }
}
