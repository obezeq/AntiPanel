import { ChangeDetectionStrategy, Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { forkJoin, fromEvent, filter, throttleTime, take } from 'rxjs';
import { Header } from '../../components/layout/header/header';
import { Footer } from '../../components/layout/footer/footer';
import { AuthService } from '../../core/services/auth.service';
import { UserService } from '../../core/services/user.service';
import { InvoiceService, InvoiceResponse, PaymentProcessor } from '../../core/services/invoice.service';
import { NotificationService } from '../../services/notification.service';
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
  private readonly notificationService = inject(NotificationService);
  private readonly destroyRef = inject(DestroyRef);

  /** Current wallet balance formatted as currency */
  protected readonly balance = signal<string>('$0.00');

  /** List of user invoices */
  protected readonly invoices = signal<Invoice[]>([]);

  /** Whether add funds form is processing */
  protected readonly isAddingFunds = signal<boolean>(false);

  /** Processor limits for amount validation */
  protected readonly processorLimits = signal<{ min: number; max: number | null }>({
    min: 1,
    max: null
  });

  /** Whether payment processors have been loaded */
  protected readonly processorsLoaded = signal<boolean>(false);

  /** Active payment processors */
  private paymentProcessors: PaymentProcessor[] = [];

  ngOnInit(): void {
    this.loadWalletData();
    this.setupPaymentPolling();
  }

  /**
   * Set up payment polling when tab gains focus.
   * Checks PROCESSING invoices for payment completion.
   * Uses throttleTime to prevent spam on rapid tab switching.
   */
  private setupPaymentPolling(): void {
    fromEvent(document, 'visibilitychange').pipe(
      filter(() => document.visibilityState === 'visible'),
      throttleTime(5000), // Prevent spam on rapid tab switching
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(() => {
      this.checkProcessingInvoices();
    });
  }

  /**
   * Check all PROCESSING invoices for payment completion.
   * Called when user returns to the tab.
   * Uses forkJoin to batch requests and single subscription for cleanup.
   */
  private checkProcessingInvoices(): void {
    const processing = this.invoices().filter(inv => inv.status === 'processing');

    if (processing.length === 0) {
      return;
    }

    // Use forkJoin to combine all checks into single subscription
    forkJoin(
      processing.map(inv =>
        this.invoiceService.checkPaymentStatus(parseInt(inv.id, 10))
      )
    ).pipe(
      take(1),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: (results) => {
        if (results.some(r => r.status === 'COMPLETED')) {
          this.notificationService.success(
            'Payment completed successfully!',
            { title: 'Payment Received' }
          );
          this.loadWalletData();
        }
      },
      error: (err) => {
        // Log but don't show to user - will retry on next focus
        console.warn('Payment status check failed:', err);
      }
    });
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
   * Creates payment invoice and opens payment page in new tab.
   */
  protected onAddFunds(payload: AddFundsPayload): void {
    this.isAddingFunds.set(true);

    // Find the Paymento processor (crypto)
    const processor = this.paymentProcessors.find(p => p.code === 'paymento');

    if (!processor) {
      this.notificationService.error('Payment processor not available. Please try again later.', {
        title: 'Error'
      });
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

        if (invoice.paymentUrl) {
          // Open payment in new tab with security attributes (prevents tabnabbing)
          window.open(invoice.paymentUrl, '_blank', 'noopener,noreferrer');

          this.notificationService.success(
            'Payment window opened. Complete payment in the new tab.',
            { title: 'Invoice Created' }
          );
        }

        // Reload invoices to show the new pending invoice
        this.loadInvoices();
      },
      error: (err) => {
        this.isAddingFunds.set(false);
        const message = err.error?.message || 'Failed to create invoice. Please try again.';
        this.notificationService.error(message, { title: 'Error' });
      }
    });
  }

  /**
   * Handle invoice click.
   * Opens payment link for pending/processing invoices in new tab.
   */
  protected onInvoiceClick(invoice: Invoice): void {
    if (invoice.paymentUrl) {
      window.open(invoice.paymentUrl, '_blank', 'noopener,noreferrer');
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
        this.processorsLoaded.set(true);

        // Set processor limits for validation in the add-funds form
        const paymento = processors.find(p => p.code === 'paymento');
        if (paymento) {
          this.processorLimits.set({
            min: paymento.minAmount,
            max: paymento.maxAmount
          });
        }
      },
      error: (err) => {
        console.error('Failed to load payment processors:', err);
        this.notificationService.error(
          'Failed to load payment options. Please refresh the page.',
          { title: 'Error', duration: 0 }
        );
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
