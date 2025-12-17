import { Injectable, signal, computed, effect, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

/**
 * Theme types
 * - 'light' | 'dark': User manually selected theme
 * - 'system': Follow OS preference (prefers-color-scheme)
 */
type Theme = 'light' | 'dark' | 'system';
type ResolvedTheme = 'light' | 'dark';

/**
 * ThemeService - Global theme management using Angular 21 signals
 *
 * Features:
 * - Signal-based reactive state (Angular 21 best practice)
 * - localStorage persistence
 * - System preference detection (prefers-color-scheme)
 * - Cross-tab synchronization
 * - SSR-safe implementation
 * - Default to dark mode
 */
@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly isBrowser = isPlatformBrowser(this.platformId);

  // Private state signals
  private readonly selectedTheme = signal<Theme>('dark'); // Default to dark
  private readonly systemTheme = signal<ResolvedTheme>('dark');

  // Public computed signals
  /**
   * The currently active theme (resolved from selected + system)
   */
  readonly currentTheme = computed<ResolvedTheme>(() => {
    return this.selectedTheme() === 'system'
      ? this.systemTheme()
      : this.selectedTheme() as ResolvedTheme;
  });

  /**
   * Whether current theme is dark mode
   */
  readonly isDark = computed(() => this.currentTheme() === 'dark');

  constructor() {
    if (!this.isBrowser) return;

    // 1. Detect system preference first
    this.initializeSystemTheme();

    // 2. Load from localStorage (overrides system preference)
    this.initializeTheme();

    // 3. Effect: Persist theme changes to localStorage
    effect(() => {
      localStorage.setItem('antipanel-theme', this.selectedTheme());
    });

    // 4. Effect: Apply theme to DOM
    effect(() => {
      const theme = this.currentTheme();
      if (theme === 'light') {
        document.documentElement.setAttribute('data-theme', 'light');
      } else {
        document.documentElement.removeAttribute('data-theme');
      }
    });

    // 5. Cross-tab synchronization
    this.setupStorageListener();
  }

  /**
   * Toggle between light and dark themes
   */
  toggleTheme(): void {
    const current = this.currentTheme();
    this.selectedTheme.set(current === 'light' ? 'dark' : 'light');
  }

  /**
   * Set a specific theme
   */
  setTheme(theme: Theme): void {
    this.selectedTheme.set(theme);
  }

  /**
   * Initialize theme from localStorage
   * If no stored preference, keeps default ('dark')
   */
  private initializeTheme(): void {
    const stored = localStorage.getItem('antipanel-theme') as Theme;
    if (stored && ['light', 'dark', 'system'].includes(stored)) {
      this.selectedTheme.set(stored);
    }
    // If no stored value, default 'dark' is already set
  }

  /**
   * Detect and listen for system theme preference changes
   */
  private initializeSystemTheme(): void {
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
    this.systemTheme.set(mediaQuery.matches ? 'dark' : 'light');

    // Listen for system preference changes in real-time
    mediaQuery.addEventListener('change', (event) => {
      this.systemTheme.set(event.matches ? 'dark' : 'light');
    });
  }

  /**
   * Sync theme changes across browser tabs
   */
  private setupStorageListener(): void {
    window.addEventListener('storage', (event) => {
      if (event.key === 'antipanel-theme' && event.newValue) {
        const newTheme = event.newValue as Theme;
        if (['light', 'dark', 'system'].includes(newTheme)) {
          this.selectedTheme.set(newTheme);
        }
      }
    });
  }
}
