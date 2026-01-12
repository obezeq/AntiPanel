import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

/**
 * Wallet balance section component.
 * Displays current balance with back navigation button.
 *
 * Features:
 * - Back button for navigation to dashboard
 * - Balance label and amount display
 * - Responsive typography (64px desktop -> 32px mobile)
 * - Semantic HTML with figure/figcaption
 */
@Component({
  selector: 'app-wallet-balance-section',
  templateUrl: './wallet-balance-section.html',
  styleUrl: './wallet-balance-section.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class WalletBalanceSection {
  /** Current wallet balance formatted as currency string (e.g., "$369.33") */
  readonly balance = input.required<string>();

  /** Emits when back button is clicked */
  readonly backClick = output<void>();
}
