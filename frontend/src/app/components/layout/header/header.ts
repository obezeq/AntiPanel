import {
  ChangeDetectionStrategy,
  Component,
  computed,
  ElementRef,
  HostListener,
  inject,
  input,
  output,
  signal,
  viewChild,
  viewChildren
} from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { NgIcon } from '@ng-icons/core';
import { ThemeService } from '../../../services/theme.service';

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
  fragment?: string;
  external?: boolean;
}

@Component({
  selector: 'app-header',
  templateUrl: './header.html',
  styleUrl: './header.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, RouterLinkActive, NgIcon]
})
export class Header {
  /** Theme service for dark/light mode toggle */
  protected readonly themeService = inject(ThemeService);

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

  /** Profile container reference for click-outside detection */
  private readonly profileContainerRef = viewChild<ElementRef<HTMLElement>>('profileContainer');

  /** Dropdown menu items for keyboard navigation (WCAG 2.1.1) */
  private readonly dropdownItems = viewChildren<ElementRef<HTMLElement>>('dropdownItem');

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
        // NEW ORDER and SERVICES scroll to sections within dashboard
        return [
          { label: 'NEW ORDER', path: '/dashboard', fragment: 'order-section' },
          { label: 'SERVICES', path: '/dashboard', fragment: 'services-section' },
          { label: 'SUPPORT', path: '/support' }
        ];
      case 'loggedIn':
        // Logged In (non-dashboard): DASHBOARD, NEW ORDER, SERVICES, SUPPORT
        return [
          { label: 'DASHBOARD', path: '/dashboard' },
          { label: 'NEW ORDER', path: '/dashboard', fragment: 'order-section' },
          { label: 'SERVICES', path: '/dashboard', fragment: 'services-section' },
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
    // Focus first item when dropdown opens (WCAG 2.4.7)
    if (this.isProfileDropdownOpen()) {
      setTimeout(() => this.focusFirstDropdownItem(), 0);
    }
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

  protected onThemeToggle(): void {
    this.themeService.toggleTheme();
  }

  // ---------------------------------------------------------------------------
  // Click Outside Detection (for closing dropdown)
  // ---------------------------------------------------------------------------

  /**
   * Closes the profile dropdown when clicking outside the container.
   * Replaces the overlay pattern for cleaner, more reliable behavior.
   */
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    // Only process if dropdown is open
    if (!this.isProfileDropdownOpen()) return;

    const target = event.target as HTMLElement;
    const container = this.profileContainerRef()?.nativeElement;

    // Close dropdown if click is outside the profile container
    if (container && !container.contains(target)) {
      this.closeProfileDropdown();
    }
  }

  // ---------------------------------------------------------------------------
  // Keyboard Navigation (WCAG 2.1.1, 2.4.3)
  // ---------------------------------------------------------------------------

  /**
   * Handles keyboard navigation within the profile dropdown menu.
   * Per WAI-ARIA: Arrow keys navigate items, Escape closes menu.
   */
  protected onDropdownKeydown(event: KeyboardEvent): void {
    const items = this.dropdownItems();
    if (!items.length) return;

    const currentIndex = this.getCurrentDropdownItemIndex(items);

    switch (event.key) {
      case 'ArrowDown':
        event.preventDefault();
        this.focusDropdownItem(items, currentIndex + 1);
        break;
      case 'ArrowUp':
        event.preventDefault();
        this.focusDropdownItem(items, currentIndex - 1);
        break;
      case 'Escape':
        event.preventDefault();
        this.closeProfileDropdown();
        break;
      case 'Tab':
        // Allow Tab to exit menu naturally, then close
        this.closeProfileDropdown();
        break;
    }
  }

  /**
   * Gets the currently focused item index within dropdown.
   */
  private getCurrentDropdownItemIndex(items: readonly ElementRef<HTMLElement>[]): number {
    const activeElement = document.activeElement;
    return items.findIndex(item => item.nativeElement === activeElement);
  }

  /**
   * Focuses a dropdown item by index with wraparound.
   */
  private focusDropdownItem(items: readonly ElementRef<HTMLElement>[], index: number): void {
    const length = items.length;
    // Wraparound navigation
    const targetIndex = ((index % length) + length) % length;
    items[targetIndex]?.nativeElement.focus();
  }

  /**
   * Focuses the first dropdown item (called when dropdown opens).
   */
  private focusFirstDropdownItem(): void {
    const items = this.dropdownItems();
    if (items.length > 0) {
      items[0].nativeElement.focus();
    }
  }
}
