import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { DatePipe, DecimalPipe, UpperCasePipe } from '@angular/common';

/**
 * Invoice status type.
 */
export type InvoiceStatus = 'pending' | 'processing' | 'paid' | 'failed' | 'expired' | 'cancelled';

/**
 * Invoice data interface.
 */
export interface Invoice {
  /** Unique identifier */
  id: string;
  /** Display invoice number (e.g., "00001") */
  invoiceNumber: string;
  /** Amount in USD */
  amount: number;
  /** Currency type (e.g., "CRYPTO") */
  currency: string;
  /** Current status */
  status: InvoiceStatus;
  /** Creation timestamp */
  createdAt: Date;
  /** Payment URL for redirect (optional) */
  paymentUrl?: string;
}

/**
 * Wallet invoices section component.
 * Displays list of payment invoices with status badges.
 *
 * Features:
 * - Status badges (PENDING yellow, PAID green, EXPIRED red)
 * - Invoice details (number, amount, currency, date)
 * - Semantic HTML with definition lists
 * - Empty state handling
 */
@Component({
  selector: 'app-wallet-invoices-section',
  templateUrl: './wallet-invoices-section.html',
  styleUrl: './wallet-invoices-section.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DatePipe, DecimalPipe, UpperCasePipe]
})
export class WalletInvoicesSection {
  /** List of invoices to display */
  readonly invoices = input.required<Invoice[]>();

  /** Emits when an invoice is clicked */
  readonly invoiceClick = output<Invoice>();

  /** Status labels for display */
  protected readonly statusLabels: Record<InvoiceStatus, string> = {
    pending: 'PENDING',
    processing: 'PROCESSING',
    paid: 'PAID',
    failed: 'FAILED',
    expired: 'EXPIRED',
    cancelled: 'CANCELLED'
  };

  /**
   * Handle invoice click.
   */
  protected onInvoiceClick(invoice: Invoice): void {
    this.invoiceClick.emit(invoice);
  }
}
