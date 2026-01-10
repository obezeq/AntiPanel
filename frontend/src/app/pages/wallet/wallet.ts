import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Header } from '../../components/layout/header/header';
import { Footer } from '../../components/layout/footer/footer';
import { AuthService } from '../../core/services/auth.service';
import { UserService } from '../../core/services/user.service';
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

  /** Current wallet balance formatted as currency */
  protected readonly balance = signal<string>('$0.00');

  /** List of user invoices */
  protected readonly invoices = signal<Invoice[]>([]);

  /** Whether add funds form is processing */
  protected readonly isAddingFunds = signal<boolean>(false);

  ngOnInit(): void {
    this.loadWalletData();
  }

  /**
   * Handle wallet click from header.
   * Already on wallet page, no action needed.
   */
  protected onWalletClick(): void {
    // Already on wallet page
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
   * Creates payment invoice for the specified amount.
   */
  protected onAddFunds(payload: AddFundsPayload): void {
    this.isAddingFunds.set(true);

    // TODO: Integrate with payment service when backend is ready
    // For now, simulate a brief loading state
    console.log('Adding funds:', payload);

    setTimeout(() => {
      this.isAddingFunds.set(false);
      // TODO: Navigate to payment page or show invoice modal
    }, 1500);
  }

  /**
   * Handle invoice click.
   * Opens invoice details or payment link.
   */
  protected onInvoiceClick(invoice: Invoice): void {
    // TODO: Open invoice details modal or navigate to payment page
    console.log('Invoice clicked:', invoice);
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

    // TODO: Load invoices from invoice service when backend is ready
    // For now, show mock data for demonstration
    this.invoices.set([
      {
        id: '1',
        invoiceNumber: '00001',
        amount: 369.33,
        currency: 'CRYPTO',
        status: 'pending',
        createdAt: new Date('2025-11-11T03:33:00')
      },
      {
        id: '2',
        invoiceNumber: '00002',
        amount: 69.33,
        currency: 'CRYPTO',
        status: 'pending',
        createdAt: new Date('2025-11-11T03:33:00')
      },
      {
        id: '3',
        invoiceNumber: '00003',
        amount: 33.00,
        currency: 'CRYPTO',
        status: 'paid',
        createdAt: new Date('2025-11-11T03:33:00')
      },
      {
        id: '4',
        invoiceNumber: '00004',
        amount: 33.00,
        currency: 'CRYPTO',
        status: 'paid',
        createdAt: new Date('2025-11-11T03:33:00')
      },
      {
        id: '5',
        invoiceNumber: '00005',
        amount: 25.00,
        currency: 'CRYPTO',
        status: 'expired',
        createdAt: new Date('2025-11-10T15:20:00')
      }
    ]);
  }
}
