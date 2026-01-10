import { ChangeDetectionStrategy, Component, input, output, signal } from '@angular/core';
import { UpperCasePipe } from '@angular/common';

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
 * - Submit button with loading state
 * - Responsive layout (horizontal on desktop, vertical on mobile)
 */
@Component({
  selector: 'app-wallet-add-funds-section',
  templateUrl: './wallet-add-funds-section.html',
  styleUrl: './wallet-add-funds-section.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [UpperCasePipe]
})
export class WalletAddFundsSection {
  /** Whether the form is currently processing */
  readonly isLoading = input<boolean>(false);

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
   * Validates amount and emits addFunds event if valid.
   */
  protected onSubmit(): void {
    const amountValue = parseFloat(this.amount());

    if (!this.amount() || isNaN(amountValue)) {
      this.amountError.set('Please enter an amount');
      return;
    }

    if (amountValue <= 0) {
      this.amountError.set('Amount must be greater than zero');
      return;
    }

    this.amountError.set('');
    this.addFunds.emit({
      crypto: this.selectedCrypto(),
      amount: amountValue
    });
  }
}
