import { ChangeDetectionStrategy, Component, input, output, signal } from '@angular/core';
import { UpperCasePipe } from '@angular/common';
import { Button } from '../../../../components/shared/button/button';

/**
 * Crypto payment option interface.
 */
export interface CryptoOption {
  value: string;
  label: string;
}

/**
 * Add funds event payload.
 */
export interface AddFundsPayload {
  crypto: string;
  amount: number;
}

/**
 * Wallet add funds section component.
 * Provides form for adding funds via cryptocurrency.
 *
 * Features:
 * - Crypto selection dropdown (placeholder for now)
 * - Amount input with validation
 * - Submit button with loading state (uses shared Button component)
 * - Responsive layout (horizontal on desktop, vertical on mobile)
 */
@Component({
  selector: 'app-wallet-add-funds-section',
  templateUrl: './wallet-add-funds-section.html',
  styleUrl: './wallet-add-funds-section.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [UpperCasePipe, Button]
})
export class WalletAddFundsSection {
  /** Whether the form is currently processing */
  readonly isLoading = input<boolean>(false);

  /** Whether the form should be disabled (e.g., processors not loaded) */
  readonly disabled = input<boolean>(false);

  /** Minimum deposit amount from processor */
  readonly minAmount = input<number>(1);

  /** Maximum deposit amount from processor (null = unlimited) */
  readonly maxAmount = input<number | null>(null);

  /** Emits when form is submitted with valid data */
  readonly addFunds = output<AddFundsPayload>();

  /** Available crypto options */
  protected readonly cryptoOptions: CryptoOption[] = [
    { value: 'crypto', label: 'CRYPTO' }
  ];

  /** Currently selected cryptocurrency */
  protected readonly selectedCrypto = signal<string>('crypto');

  /** Amount input value */
  protected readonly amount = signal<string>('');

  /** Error message for amount validation */
  protected readonly amountError = signal<string>('');

  /**
   * Handle amount input changes.
   */
  protected onAmountInput(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.amount.set(target.value);
    // Clear error when user starts typing
    if (this.amountError()) {
      this.amountError.set('');
    }
  }

  /**
   * Handle form submission.
   * Explicitly prevents default to avoid page refresh.
   * Validates amount against processor limits and emits addFunds event if valid.
   */
  protected onSubmit(event: Event): void {
    // Prevent native form submission (page refresh)
    event.preventDefault();
    event.stopPropagation();

    const amountValue = parseFloat(this.amount());
    const min = this.minAmount();
    const max = this.maxAmount();

    if (!this.amount() || isNaN(amountValue)) {
      this.amountError.set('Please enter an amount');
      return;
    }

    if (amountValue < min) {
      this.amountError.set(`Minimum deposit is $${min.toFixed(2)}`);
      return;
    }

    if (max !== null && amountValue > max) {
      this.amountError.set(`Maximum deposit is $${max.toFixed(2)}`);
      return;
    }

    this.amountError.set('');
    this.addFunds.emit({
      crypto: this.selectedCrypto(),
      amount: amountValue
    });
  }
}
