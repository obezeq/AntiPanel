import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
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
    provideRouter(routes),
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
