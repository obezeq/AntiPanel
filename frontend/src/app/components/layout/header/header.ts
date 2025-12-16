import { ChangeDetectionStrategy, Component, computed, input, output, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

export type HeaderVariant = 'home' | 'dashboard' | 'admin';

interface NavItem {
  label: string;
  path: string;
  external?: boolean;
}

@Component({
  selector: 'app-header',
  templateUrl: './header.html',
  styleUrl: './header.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, RouterLinkActive]
})
export class Header {
  /** Header variant determines which navigation items to show */
  readonly variant = input<HeaderVariant>('home');

  /** Whether the user is authenticated */
  readonly isAuthenticated = input<boolean>(false);

  /** User's wallet balance (when authenticated) */
  readonly walletBalance = input<string>('$0.00');

  /** Emits when access button is clicked */
  readonly accessClick = output<void>();

  /** Emits when wallet button is clicked */
  readonly walletClick = output<void>();

  /** Emits when profile button is clicked */
  readonly profileClick = output<void>();

  /** Mobile menu open state */
  protected readonly isMobileMenuOpen = signal(false);

  /** Navigation items based on variant */
  protected readonly navItems = computed<NavItem[]>(() => {
    const variant = this.variant();

    switch (variant) {
      case 'dashboard':
        return [
          { label: 'Dashboard', path: '/dashboard' },
          { label: 'Orders', path: '/orders' },
          { label: 'Services', path: '/services' }
        ];
      case 'admin':
        return [
          { label: 'Dashboard', path: '/admin' },
          { label: 'Orders', path: '/admin/orders' },
          { label: 'Users', path: '/admin/users' },
          { label: 'Services', path: '/admin/services' }
        ];
      default:
        return [
          { label: 'Services', path: '/services' },
          { label: 'API', path: '/api' },
          { label: 'Terms of Service', path: '/terms' }
        ];
    }
  });

  protected toggleMobileMenu(): void {
    this.isMobileMenuOpen.update(open => !open);
  }

  protected closeMobileMenu(): void {
    this.isMobileMenuOpen.set(false);
  }

  protected onAccessClick(): void {
    this.accessClick.emit();
    this.closeMobileMenu();
  }

  protected onWalletClick(): void {
    this.walletClick.emit();
    this.closeMobileMenu();
  }

  protected onProfileClick(): void {
    this.profileClick.emit();
    this.closeMobileMenu();
  }
}
