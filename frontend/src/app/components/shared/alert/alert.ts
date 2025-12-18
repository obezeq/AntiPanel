import { ChangeDetectionStrategy, Component, input, output, signal } from '@angular/core';

export type AlertVariant = 'success' | 'error' | 'warning' | 'info';

@Component({
  selector: 'app-alert',
  templateUrl: './alert.html',
  styleUrl: './alert.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class Alert {
  /** Alert visual variant */
  readonly variant = input<AlertVariant>('info');

  /** Alert title (optional) */
  readonly title = input<string>('');

  /** Whether the alert can be dismissed */
  readonly dismissible = input<boolean>(false);

  /** Emits when the alert is dismissed */
  readonly dismissed = output<void>();

  /** Whether the alert is visible */
  protected readonly isVisible = signal<boolean>(true);

  /** ARIA role based on variant */
  protected getRole(): 'alert' | 'status' {
    const variant = this.variant();
    return variant === 'error' || variant === 'warning' ? 'alert' : 'status';
  }

  protected dismiss(): void {
    this.isVisible.set(false);
    this.dismissed.emit();
  }
}
