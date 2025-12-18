import { Injectable, signal, computed } from '@angular/core';

/**
 * LoadingService - Servicio de Estado de Carga
 *
 * Servicio centralizado para gestionar estados de carga globales.
 * Utiliza Angular Signals para estado reactivo.
 * Maneja multiples requests concurrentes con un contador.
 *
 * @example
 * ```typescript
 * // En un componente
 * private loadingService = inject(LoadingService);
 *
 * // Verificar estado de carga
 * @if (loadingService.isLoading()) {
 *   <app-spinner />
 * }
 *
 * // Controlar manualmente
 * this.loadingService.show();
 * await someAsyncOperation();
 * this.loadingService.hide();
 * ```
 */
@Injectable({
  providedIn: 'root'
})
export class LoadingService {
  /**
   * Contador de requests activos.
   * Permite manejar multiples requests concurrentes.
   */
  private readonly _activeRequests = signal(0);

  /**
   * Estado de carga global (solo lectura).
   * Es true cuando hay al menos un request activo.
   */
  readonly isLoading = computed(() => this._activeRequests() > 0);

  /**
   * Numero de requests activos (solo lectura).
   */
  readonly activeRequests = this._activeRequests.asReadonly();

  /**
   * Incrementa el contador de requests activos.
   * Muestra el estado de carga.
   */
  show(): void {
    this._activeRequests.update(count => count + 1);
  }

  /**
   * Decrementa el contador de requests activos.
   * Oculta el estado de carga cuando llega a 0.
   */
  hide(): void {
    this._activeRequests.update(count => Math.max(0, count - 1));
  }

  /**
   * Resetea el contador a 0.
   * Util para limpiar estados pendientes.
   */
  reset(): void {
    this._activeRequests.set(0);
  }

  /**
   * Ejecuta una funcion asincrona mostrando el loading.
   * El loading se oculta automaticamente al finalizar o en caso de error.
   *
   * @example
   * ```typescript
   * const result = await loadingService.withLoading(
   *   () => this.apiService.getData()
   * );
   * ```
   */
  async withLoading<T>(fn: () => Promise<T>): Promise<T> {
    this.show();
    try {
      return await fn();
    } finally {
      this.hide();
    }
  }
}
