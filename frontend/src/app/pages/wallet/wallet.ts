import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Header } from '../../components/layout/header/header';
import { Footer } from '../../components/layout/footer/footer';
import { AuthService } from '../../core/services/auth.service';
import { UserService } from '../../core/services/user.service';
import { InvoiceService, InvoiceResponse, PaymentProcessor } from '../../core/services/invoice.service';
import { WalletBalanceSection } from './sections/wallet-balance-section/wallet-balance-section';
import { WalletAddFundsSection, AddFundsPayload } from './sections/wallet-add-funds-section/wallet-add-funds-section';
import { WalletInvoicesSection, Invoice } from './sections/wallet-invoices-section/wallet-invoices-section';

/**
 * Wallet page component.
 * Handles adding funds to user balance and displays invoices.
 *
 * Features:
 * - Balance display with back navigation
 * - Add funds form with crypto selection
 * - Invoice list with status badges
 * - Grid background with ambient glow
 *
 * @example
 * Route: /wallet (requires authentication)
 */
@Component({
  selector: 'app-wallet',
  templateUrl: './wallet.html',
  styleUrl: './wallet.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    Header,
    Footer,
    WalletBalanceSection,
    WalletAddFundsSection,
    WalletInvoicesSection
  ]
})
export class Wallet implements OnInit {
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);
  private readonly userService = inject(UserService);
  private readonly invoiceService = inject(InvoiceService);

  /** Current wallet balance formatted as currency */
  protected readonly balance = signal<string>('$0.00');

  /** List of user invoices */
  protected readonly invoices = signal<Invoice[]>([]);

  /** Whether add funds form is processing */
  protected readonly isAddingFunds = signal<boolean>(false);

  /** Active payment processors */
  private paymentProcessors: PaymentProcessor[] = [];

  ngOnInit(): void {
    this.loadWalletData();
  }

  /**
   * Handle logout click from header.
   * Subscribes to logout observable to properly notify backend.
   */
  protected onLogoutClick(): void {
    this.authService.logout().subscribe({
      next: () => this.router.navigate(['/home']),
      error: () => this.router.navigate(['/home'])
    });
  }

  /**
   * Navigate back to dashboard.
   */
  protected onBackClick(): void {
    this.router.navigate(['/dashboard']);
  }

  /**
   * Handle add funds form submission.
   * Creates payment invoice and redirects to payment page.
   */
  protected onAddFunds(payload: AddFundsPayload): void {
    this.isAddingFunds.set(true);

    // Find the Paymento processor (crypto)
    const processor = this.paymentProcessors.find(p => p.code === 'paymento');

    if (!processor) {
      console.error('Paymento processor not found');
      this.isAddingFunds.set(false);
      return;
    }

    this.invoiceService.createInvoice({
      processorId: processor.id,
      amount: payload.amount,
      currency: 'USD'
    }).subscribe({
      next: (invoice) => {
        this.isAddingFunds.set(false);

        // Redirect to payment page if URL is available
        if (invoice.paymentUrl) {
          window.location.href = invoice.paymentUrl;
        } else {
          // Reload invoices to show the new pending invoice
          this.loadInvoices();
        }
      },
      error: (err) => {
        console.error('Failed to create invoice:', err);
        this.isAddingFunds.set(false);
      }
    });
  }

  /**
   * Handle invoice click.
   * Opens payment link for pending/processing invoices.
   */
  protected onInvoiceClick(invoice: Invoice): void {
    // If invoice has a payment URL, redirect to it
    if (invoice.paymentUrl) {
      window.location.href = invoice.paymentUrl;
    }
  }

  /**
   * Load wallet data from services.
   */
  private loadWalletData(): void {
    // Load user balance
    this.userService.getStatistics().subscribe({
      next: (stats) => {
        this.balance.set(`$${stats.balance.toFixed(2)}`);
      },
      error: (err) => {
        console.error('Failed to load wallet balance:', err);
      }
    });

    // Load payment processors
    this.invoiceService.getPaymentProcessors().subscribe({
      next: (processors) => {
        this.paymentProcessors = processors;
      },
      error: (err) => {
        console.error('Failed to load payment processors:', err);
      }
    });

    // Load invoices
    this.loadInvoices();
  }

  /**
   * Load invoices from the API.
   */
  private loadInvoices(): void {
    this.invoiceService.getInvoices(0, 50).subscribe({
      next: (response) => {
        const mappedInvoices = response.content.map(inv => this.mapInvoice(inv));
        this.invoices.set(mappedInvoices);
      },
      error: (err) => {
        console.error('Failed to load invoices:', err);
      }
    });
  }

  /**
   * Map API invoice response to component Invoice interface.
   */
  private mapInvoice(inv: InvoiceResponse): Invoice {
    return {
      id: inv.id.toString(),
      invoiceNumber: inv.id.toString().padStart(5, '0'),
      amount: inv.amount,
      currency: inv.processor.code === 'paymento' ? 'CRYPTO' : inv.currency,
      status: this.invoiceService.mapStatus(inv.status),
      createdAt: new Date(inv.createdAt),
      paymentUrl: inv.paymentUrl ?? undefined
    };
  }
}
