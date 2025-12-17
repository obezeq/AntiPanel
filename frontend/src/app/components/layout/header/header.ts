import { ChangeDetectionStrategy, Component, computed, input, output, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

/**
 * Header variants matching Figma design:
 * - home: Landing page with Logo+Text and ACCESS button
 * - login: Login page with Logo+Text and REGISTER button
 * - register: Register page with Logo+Text and LOGIN button
 * - dashboard: User dashboard with Logo only, nav items, WALLET and Profile dropdown
 * - loggedIn: Authenticated non-dashboard pages with DASHBOARD link, nav, WALLET and Profile dropdown
 * - admin: Admin panel with Logo, PANEL NAME, admin info and Profile
 */
export type HeaderVariant = 'home' | 'login' | 'register' | 'dashboard' | 'loggedIn' | 'admin';

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
  /** Header variant determines layout and navigation items */
  readonly variant = input<HeaderVariant>('home');

  /** User's wallet balance (for authenticated variants) */
  readonly walletBalance = input<string>('$0.00');

  /** Admin name (for admin variant) */
  readonly adminName = input<string>('ADMIN');

  /** Admin department (for admin variant) */
  readonly adminDepartment = input<string>('DEPARTMENT');

  /** Panel name (for admin variant) */
  readonly panelName = input<string>('PANEL NAME');

  /** Emits when wallet button is clicked */
  readonly walletClick = output<void>();

  /** Emits when profile button is clicked */
  readonly profileClick = output<void>();

  /** Emits when logout is clicked */
  readonly logoutClick = output<void>();

  /** Mobile menu open state */
  protected readonly isMobileMenuOpen = signal(false);

  /** Profile dropdown open state */
  protected readonly isProfileDropdownOpen = signal(false);

  /** Whether to show full logo (icon + text) or just icon */
  protected readonly showFullLogo = computed(() => {
    const variant = this.variant();
    return variant === 'home' || variant === 'login' || variant === 'register';
  });

  /** Whether user is authenticated (has nav items, wallet, profile) */
  protected readonly isAuthenticated = computed(() => {
    const variant = this.variant();
    return variant === 'dashboard' || variant === 'loggedIn' || variant === 'admin';
  });

  /** Whether to show navigation items (admin has NO navigation) */
  protected readonly showNavigation = computed(() => {
    const variant = this.variant();
    return variant === 'dashboard' || variant === 'loggedIn';
  });

  /** Whether to show mobile menu (hamburger + sidebar) */
  protected readonly showMobileMenu = computed(() => {
    const variant = this.variant();
    return variant === 'dashboard' || variant === 'loggedIn' || variant === 'admin';
  });

  /** Access button text based on variant */
  protected readonly accessButtonText = computed(() => {
    const variant = this.variant();
    if (variant === 'login') return 'REGISTER';
    if (variant === 'register') return 'LOGIN';
    return 'ACCESS';
  });

  /** Access button link based on variant */
  protected readonly accessButtonLink = computed(() => {
    const variant = this.variant();
    if (variant === 'login') return '/register';
    if (variant === 'register') return '/login';
    return '/login';
  });

  /** Navigation items based on variant */
  protected readonly navItems = computed<NavItem[]>(() => {
    const variant = this.variant();

    switch (variant) {
      case 'dashboard':
        // Dashboard: NEW ORDER, SERVICES, SUPPORT
        return [
          { label: 'NEW ORDER', path: '/new-order' },
          { label: 'SERVICES', path: '/services' },
          { label: 'SUPPORT', path: '/support' }
        ];
      case 'loggedIn':
        // Logged In (non-dashboard): DASHBOARD, NEW ORDER, SERVICES, SUPPORT
        return [
          { label: 'DASHBOARD', path: '/dashboard' },
          { label: 'NEW ORDER', path: '/new-order' },
          { label: 'SERVICES', path: '/services' },
          { label: 'SUPPORT', path: '/support' }
        ];
      default:
        // Admin and other variants have NO navigation items
        return [];
    }
  });

  protected toggleMobileMenu(): void {
    this.isMobileMenuOpen.update(open => !open);
  }

  protected closeMobileMenu(): void {
    this.isMobileMenuOpen.set(false);
  }

  protected toggleProfileDropdown(): void {
    this.isProfileDropdownOpen.update(open => !open);
  }

  protected closeProfileDropdown(): void {
    this.isProfileDropdownOpen.set(false);
  }

  protected onWalletClick(): void {
    this.walletClick.emit();
    this.closeMobileMenu();
  }

  protected onProfileClick(): void {
    this.toggleProfileDropdown();
  }

  protected onOrdersClick(): void {
    this.closeProfileDropdown();
    this.closeMobileMenu();
  }

  protected onLogoutClick(): void {
    this.logoutClick.emit();
    this.closeProfileDropdown();
    this.closeMobileMenu();
  }
}
