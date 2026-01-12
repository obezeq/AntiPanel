import { ApplicationConfig, APP_INITIALIZER, provideBrowserGlobalErrorListeners } from '@angular/core';
import { PreloadAllModules, provideRouter, withInMemoryScrolling, withPreloading } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { authInterceptor, loadingInterceptor } from './core/interceptors';
import { provideIcons } from '@ng-icons/core';
import {
  matHome,
  matDashboard,
  matShoppingCart,
  matPerson,
  matSettings,
  matMenu,
  matClose,
  matKeyboardArrowDown,
  matKeyboardArrowUp,
  matCheck,
  matError,
  matWarning,
  matInfo,
  matSearch,
  matAdd,
  matRemove,
  matEdit,
  matDelete,
  matVisibility,
  matVisibilityOff,
  matArrowBack,
  matArrowForward,
  matArrowUpward,
  matRefresh,
  matAccountBalanceWallet,
  // Stats card icons
  matShowChart,
  matSchedule,
  matCheckCircle,
  matQueryStats,
  // Theme toggle icons
  matLightMode,
  matDarkMode,
  // Support page icons
  matEmail
} from '@ng-icons/material-icons/baseline';
import {
  iconoirInstagram,
  iconoirTiktok,
  iconoirYoutube,
  iconoirTwitter,
  iconoirFacebook,
  iconoirSpotify,
  iconoirTelegram,
  iconoirDiscord,
  iconoirLinkedin,
  iconoirFlash
} from '@ng-icons/iconoir';
import { simpleSnapchat } from '@ng-icons/simple-icons';

import { routes } from './app.routes';
import { TokenRefreshService } from './core/services/token-refresh.service';

/**
 * Factory function to initialize TokenRefreshService.
 * The service uses effect() internally to auto-schedule token refresh.
 */
function initTokenRefresh(service: TokenRefreshService) {
  return () => service;
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(
      routes,
      withInMemoryScrolling({ anchorScrolling: 'enabled' }),
      withPreloading(PreloadAllModules)
    ),
    provideHttpClient(withInterceptors([authInterceptor, loadingInterceptor])),
    // Initialize token refresh service on app startup
    {
      provide: APP_INITIALIZER,
      useFactory: initTokenRefresh,
      deps: [TokenRefreshService],
      multi: true
    },
    provideIcons({
      // Material Icons - Navigation & UI
      matHome,
      matDashboard,
      matShoppingCart,
      matPerson,
      matSettings,
      matMenu,
      matClose,
      matKeyboardArrowDown,
      matKeyboardArrowUp,
      matCheck,
      matError,
      matWarning,
      matInfo,
      matSearch,
      matAdd,
      matRemove,
      matEdit,
      matDelete,
      matVisibility,
      matVisibilityOff,
      matArrowBack,
      matArrowForward,
      matArrowUpward,
      matRefresh,
      matAccountBalanceWallet,
      // Material Icons - Stats Cards
      matShowChart,
      matSchedule,
      matCheckCircle,
      matQueryStats,
      // Material Icons - Theme Toggle
      matLightMode,
      matDarkMode,
      // Material Icons - Support Page
      matEmail,
      // Iconoir - Social Media
      iconoirInstagram,
      iconoirTiktok,
      iconoirYoutube,
      iconoirTwitter,
      iconoirFacebook,
      iconoirSpotify,
      iconoirTelegram,
      iconoirDiscord,
      iconoirLinkedin,
      // Iconoir - Utility
      iconoirFlash,
      // Simple Icons - Brand logos
      simpleSnapchat
    })
  ]
};
