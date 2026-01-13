import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  computed,
  ElementRef,
  HostListener,
  inject,
  input,
  output,
  Renderer2,
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

/**
 * Header Component - Angular 21
 *
 * Componente de cabecera responsive con multiple variantes para diferentes
 * contextos de la aplicacion (home, login, dashboard, admin, etc.).
 *
 * @remarks
 * - Usa `viewChild()` y `viewChildren()` signals para acceso a elementos DOM
 * - Implementa AfterViewInit para inicializacion post-render
 * - Usa @HostListener para eventos globales (document:click, document:keydown.escape)
 * - Usa Renderer2 para manipulacion segura del DOM
 * - Menu hamburguesa con animacion CSS y cierre automatico
 * - Dropdown de perfil con navegacion por teclado WAI-ARIA
 */
@Component({
  selector: 'app-header',
  templateUrl: './header.html',
  styleUrl: './header.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, RouterLinkActive, NgIcon]
})
export class Header implements AfterViewInit {
  /** Theme service for dark/light mode toggle */
  protected readonly themeService = inject(ThemeService);

  /**
   * AfterViewInit lifecycle hook.
   *
   * Called after Angular has fully initialized the component's view.
   * With signal-based queries (viewChild/viewChildren), this is where
   * the signals are guaranteed to have their initial values.
   */
  ngAfterViewInit(): void {
    // viewChild/viewChildren signals are now populated
    // No additional initialization needed - signals handle reactivity
  }

  /** Renderer2 para manipulacion segura del DOM */
  private readonly renderer = inject(Renderer2);

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

  /** Emits when profile button is clicked */
  readonly profileClick = output<void>();

  /** Emits when logout is clicked */
  readonly logoutClick = output<void>();

  /** Mobile menu open state */
  protected readonly isMobileMenuOpen = signal(false);

  /** Profile dropdown open state */
  protected readonly isProfileDropdownOpen = signal(false);

  /**
   * Referencia al contenedor del perfil para deteccion de click fuera.
   *
   * En Angular 21, `viewChild()` es una signal query que reemplaza
   * al decorator @ViewChild tradicional. Beneficios:
   *
   * - Devuelve Signal<ElementRef<T> | undefined> para reactividad
   * - Se actualiza automaticamente cuando el elemento esta disponible
   * - Puede usarse con effect() para reaccionar a cambios del DOM
   *
   * Esta referencia se usa en onDocumentClick() para detectar clicks
   * fuera del dropdown y cerrarlo automaticamente.
   *
   * @see https://angular.dev/guide/signals/queries
   */
  private readonly profileContainerRef = viewChild<ElementRef<HTMLElement>>('profileContainer');

  /**
   * Referencias a los items del dropdown para navegacion por teclado.
   *
   * `viewChildren()` es la API signal para acceder a multiples elementos.
   * Devuelve Signal<readonly ElementRef<T>[]>.
   *
   * Se usa para implementar navegacion WAI-ARIA en el dropdown:
   * - ArrowUp/ArrowDown para moverse entre items
   * - Home/End para ir al primer/ultimo item
   * - Cumple WCAG 2.1.1 (Keyboard) y 2.4.3 (Focus Order)
   */
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

  /**
   * Handles global ESC key to close mobile menu and dropdown.
   * Complements document:click handler for complete keyboard accessibility.
   * WCAG 2.1.1 (Keyboard), 2.1.2 (No Keyboard Trap)
   */
  @HostListener('document:keydown.escape')
  onGlobalEscape(): void {
    if (this.isMobileMenuOpen()) {
      this.closeMobileMenu();
    }
    if (this.isProfileDropdownOpen()) {
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
