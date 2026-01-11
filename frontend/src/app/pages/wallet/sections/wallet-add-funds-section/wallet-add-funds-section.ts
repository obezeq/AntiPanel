import { ChangeDetectionStrategy, Component, computed, input, output, signal } from '@angular/core';
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

  /** Computed signal for form validity - disables submit button when invalid */
  protected readonly isFormValid = computed(() => {
    const amountStr = this.amount();
    if (!amountStr) {
      return false;
    }

    const amountValue = parseFloat(amountStr);
    if (isNaN(amountValue) || amountValue <= 0) {
      return false;
    }

    const min = this.minAmount();
    const max = this.maxAmount();

    if (amountValue < min) {
      return false;
    }

    if (max !== null && amountValue > max) {
      return false;
    }

    return true;
  });

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
   * Prevents default form submission to avoid page refresh.
   * Validates amount against processor limits and emits addFunds event if valid.
   */
  protected onSubmit(event: Event): void {
    event.preventDefault();

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
