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
  matRefresh,
  matAccountBalanceWallet
} from '@ng-icons/material-icons/baseline';
import {
  iconoirInstagram,
  iconoirTiktok,
  iconoirYoutube,
  iconoirTwitter,
  iconoirFacebook,
  iconoirSpotify,
  iconoirTelegram,
  iconoirDiscord
} from '@ng-icons/iconoir';

import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideIcons({
      // Material Icons
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
      matRefresh,
      matAccountBalanceWallet,
      // Iconoir - Social Media
      iconoirInstagram,
      iconoirTiktok,
      iconoirYoutube,
      iconoirTwitter,
      iconoirFacebook,
      iconoirSpotify,
      iconoirTelegram,
      iconoirDiscord
    })
  ]
};
