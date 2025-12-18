import { Injectable, signal, computed } from '@angular/core';

/**
 * Tipos de notificacion disponibles
 */
export type NotificationType = 'success' | 'error' | 'warning' | 'info';

/**
 * Interfaz para una notificacion
 */
export interface Notification {
  /** ID unico de la notificacion */
  id: string;
  /** Tipo de notificacion */
  type: NotificationType;
  /** Mensaje a mostrar */
  message: string;
  /** Titulo opcional */
  title?: string;
  /** Duracion en ms (0 = no auto-dismiss) */
  duration: number;
  /** Timestamp de creacion */
  createdAt: number;
}

/**
 * Opciones para crear una notificacion
 */
export interface NotificationOptions {
  /** Titulo opcional */
  title?: string;
  /** Duracion en ms (default: 5000, 0 = no auto-dismiss) */
  duration?: number;
}

/** Duracion por defecto de las notificaciones */
const DEFAULT_DURATION = 5000;

/** Maximo numero de notificaciones visibles */
const MAX_NOTIFICATIONS = 5;

/**
 * NotificationService - Servicio de Notificaciones
 *
 * Servicio centralizado para gestionar notificaciones/toasts en la aplicacion.
 * Utiliza Angular Signals para estado reactivo.
 *
 * @example
 * ```typescript
 * // En un componente
 * private notificationService = inject(NotificationService);
 *
 * // Mostrar notificaciones
 * this.notificationService.success('Operacion exitosa');
 * this.notificationService.error('Ha ocurrido un error');
 * this.notificationService.warning('Atencion requerida');
 * this.notificationService.info('Informacion importante');
 *
 * // Con opciones
 * this.notificationService.success('Guardado', {
 *   title: 'Exito',
 *   duration: 3000
 * });
 * ```
 */
@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  /** Estado interno de notificaciones */
  private readonly _notifications = signal<Notification[]>([]);

  /** Notificaciones visibles (solo lectura) */
  readonly notifications = this._notifications.asReadonly();

  /** Computed: Hay notificaciones? */
  readonly hasNotifications = computed(() => this._notifications().length > 0);

  /** Computed: Numero de notificaciones */
  readonly count = computed(() => this._notifications().length);

  /**
   * Genera un ID unico para una notificacion
   */
  private generateId(): string {
    return `notification-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
  }

  /**
   * Agrega una notificacion
   */
  private add(type: NotificationType, message: string, options?: NotificationOptions): string {
    const id = this.generateId();
    const notification: Notification = {
      id,
      type,
      message,
      title: options?.title,
      duration: options?.duration ?? DEFAULT_DURATION,
      createdAt: Date.now()
    };

    this._notifications.update(notifications => {
      // Si excede el maximo, eliminar la mas antigua
      const updated = [...notifications, notification];
      if (updated.length > MAX_NOTIFICATIONS) {
        return updated.slice(-MAX_NOTIFICATIONS);
      }
      return updated;
    });

    // Auto-dismiss si tiene duracion
    if (notification.duration > 0) {
      setTimeout(() => this.dismiss(id), notification.duration);
    }

    return id;
  }

  /**
   * Muestra una notificacion de exito
   */
  success(message: string, options?: NotificationOptions): string {
    return this.add('success', message, options);
  }

  /**
   * Muestra una notificacion de error
   */
  error(message: string, options?: NotificationOptions): string {
    return this.add('error', message, { ...options, duration: options?.duration ?? 0 });
  }

  /**
   * Muestra una notificacion de advertencia
   */
  warning(message: string, options?: NotificationOptions): string {
    return this.add('warning', message, options);
  }

  /**
   * Muestra una notificacion informativa
   */
  info(message: string, options?: NotificationOptions): string {
    return this.add('info', message, options);
  }

  /**
   * Elimina una notificacion por ID
   */
  dismiss(id: string): void {
    this._notifications.update(notifications =>
      notifications.filter(n => n.id !== id)
    );
  }

  /**
   * Elimina todas las notificaciones
   */
  dismissAll(): void {
    this._notifications.set([]);
  }

  /**
   * Elimina todas las notificaciones de un tipo especifico
   */
  dismissByType(type: NotificationType): void {
    this._notifications.update(notifications =>
      notifications.filter(n => n.type !== type)
    );
  }
}
