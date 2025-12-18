import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { NgIcon } from '@ng-icons/core';
import {
  NotificationService,
  Notification,
  NotificationType
} from '../../../services/notification.service';

/**
 * ToastContainer - Contenedor de Notificaciones
 *
 * Componente global que muestra las notificaciones/toasts.
 * Debe ser incluido una sola vez en el componente raiz (app.ts).
 *
 * @example
 * ```html
 * <!-- En app.html -->
 * <router-outlet />
 * <app-toast-container />
 * ```
 */
@Component({
  selector: 'app-toast-container',
  templateUrl: './toast-container.html',
  styleUrl: './toast-container.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [NgIcon]
})
export class ToastContainer {
  protected readonly notificationService = inject(NotificationService);

  /** Obtiene el icono segun el tipo de notificacion */
  protected getIcon(type: NotificationType): string {
    const icons: Record<NotificationType, string> = {
      success: 'matCheckCircle',
      error: 'matError',
      warning: 'matWarning',
      info: 'matInfo'
    };
    return icons[type];
  }

  /** Obtiene el titulo por defecto segun el tipo */
  protected getDefaultTitle(type: NotificationType): string {
    const titles: Record<NotificationType, string> = {
      success: 'Exito',
      error: 'Error',
      warning: 'Advertencia',
      info: 'Informacion'
    };
    return titles[type];
  }

  /** Cierra una notificacion */
  protected dismiss(notification: Notification): void {
    this.notificationService.dismiss(notification.id);
  }

  /** Maneja el evento de teclado para cerrar con ESC */
  protected onKeydown(event: KeyboardEvent, notification: Notification): void {
    if (event.key === 'Escape') {
      event.preventDefault();
      this.dismiss(notification);
    }
  }

  /** Track function para ngFor */
  protected trackByNotificationId(_index: number, notification: Notification): string {
    return notification.id;
  }
}
