import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { NgIcon } from '@ng-icons/core';
import { NotificationService } from '../../../../services/notification.service';
import { LoadingService } from '../../../../services/loading.service';
import { EventBusService } from '../../../../services/event-bus.service';
import { Spinner } from '../../../../components/shared/spinner/spinner';

/**
 * Services Section - Fase 2
 *
 * Demuestra:
 * - NotificationService (toasts success, error, warning, info)
 * - LoadingService (spinner global y local)
 * - EventBusService (comunicacion entre componentes)
 */
@Component({
  selector: 'app-services-section',
  templateUrl: './services-section.html',
  styleUrl: './services-section.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [NgIcon, Spinner]
})
export class ServicesSection {
  protected readonly notificationService = inject(NotificationService);
  protected readonly loadingService = inject(LoadingService);
  protected readonly eventBus = inject(EventBusService);

  // =========================================================================
  // NOTIFICACIONES
  // =========================================================================

  protected showSuccess(): void {
    this.notificationService.success('Operacion realizada con exito', {
      title: 'Exito'
    });
  }

  protected showError(): void {
    this.notificationService.error('Ha ocurrido un error en la operacion', {
      title: 'Error'
    });
  }

  protected showWarning(): void {
    this.notificationService.warning('Esta accion requiere atencion', {
      title: 'Advertencia',
      duration: 7000
    });
  }

  protected showInfo(): void {
    this.notificationService.info('Esta es una informacion importante', {
      title: 'Informacion'
    });
  }

  protected dismissAllNotifications(): void {
    this.notificationService.dismissAll();
  }

  // =========================================================================
  // LOADING
  // =========================================================================

  /** Estado de loading local */
  protected readonly isLocalLoading = signal(false);

  protected async simulateLoading(): Promise<void> {
    this.loadingService.show();
    await this.delay(2000);
    this.loadingService.hide();
    this.notificationService.success('Carga completada');
  }

  protected async simulateLocalLoading(): Promise<void> {
    this.isLocalLoading.set(true);
    await this.delay(2000);
    this.isLocalLoading.set(false);
    this.notificationService.info('Carga local completada');
  }

  private delay(ms: number): Promise<void> {
    return new Promise(resolve => setTimeout(resolve, ms));
  }

  // =========================================================================
  // EVENT BUS
  // =========================================================================

  /** Mensaje recibido del event bus */
  protected readonly receivedMessage = this.eventBus.onSignal<string>('demo-message');

  /** Contador de mensajes enviados */
  protected readonly messageCount = signal(0);

  protected sendMessage(): void {
    const count = this.messageCount() + 1;
    this.messageCount.set(count);
    this.eventBus.emit('demo-message', `Mensaje #${count} enviado a las ${new Date().toLocaleTimeString()}`);
    this.notificationService.info(`Mensaje #${count} emitido al event bus`);
  }

  protected clearMessages(): void {
    this.messageCount.set(0);
    this.eventBus.clearHistory();
  }

  // =========================================================================
  // COMUNICACION HERMANOS DEMO
  // =========================================================================

  /** Datos del componente A */
  protected readonly componentAData = signal('');

  /** Datos recibidos en componente B */
  protected readonly componentBData = this.eventBus.onSignal<string>('component-a-data');

  protected sendFromComponentA(): void {
    const data = this.componentAData();
    if (data.trim()) {
      this.eventBus.emit('component-a-data', data);
      this.notificationService.success('Datos enviados de A a B');
      this.componentAData.set('');
    }
  }

  protected onComponentAInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.componentAData.set(input.value);
  }
}
