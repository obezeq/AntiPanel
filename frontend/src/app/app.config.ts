import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter, withInMemoryScrolling } from '@angular/router';
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
  matDarkMode
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

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes, withInMemoryScrolling({ anchorScrolling: 'enabled' })),
    provideHttpClient(withInterceptors([authInterceptor, loadingInterceptor])),
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
