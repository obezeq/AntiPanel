import { Injectable, signal, computed, Signal } from '@angular/core';
import { Subject, Observable, filter, map } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';

/**
 * Interfaz para un evento del bus
 */
export interface BusEvent<T = unknown> {
  /** Nombre del evento */
  name: string;
  /** Datos del evento */
  data: T;
  /** Timestamp de creacion */
  timestamp: number;
}

/**
 * EventBusService - Servicio de Comunicacion entre Componentes
 *
 * Implementa el patron pub/sub para comunicacion entre componentes hermanos
 * o componentes que no tienen relacion directa padre-hijo.
 *
 * Utiliza tanto Signals como Observables para maxima flexibilidad.
 *
 * @example
 * ```typescript
 * // En un componente emisor
 * private eventBus = inject(EventBusService);
 *
 * // Emitir un evento
 * this.eventBus.emit('user-selected', { id: 123, name: 'John' });
 *
 * // En un componente receptor (usando Signal)
 * readonly selectedUser = this.eventBus.onSignal<User>('user-selected');
 *
 * // En un componente receptor (usando Observable)
 * this.eventBus.on<User>('user-selected').subscribe(user => {
 *   console.log('Usuario seleccionado:', user);
 * });
 * ```
 */
@Injectable({
  providedIn: 'root'
})
export class EventBusService {
  /** Subject para todos los eventos */
  private readonly eventSubject = new Subject<BusEvent>();

  /** Cache de signals por nombre de evento */
  private readonly signalCache = new Map<string, Signal<unknown>>();

  /** Historial de ultimos eventos (para debugging) */
  private readonly _eventHistory = signal<BusEvent[]>([]);
  readonly eventHistory = this._eventHistory.asReadonly();

  /** Maximo de eventos en historial */
  private readonly MAX_HISTORY = 50;

  /**
   * Emite un evento al bus.
   *
   * @param name - Nombre del evento
   * @param data - Datos a enviar
   */
  emit<T>(name: string, data: T): void {
    const event: BusEvent<T> = {
      name,
      data,
      timestamp: Date.now()
    };

    // Agregar al historial
    this._eventHistory.update(history => {
      const updated = [...history, event];
      return updated.slice(-this.MAX_HISTORY);
    });

    // Emitir el evento
    this.eventSubject.next(event);
  }

  /**
   * Suscribe a un evento especifico usando Observable.
   * Usar cuando necesitas transformaciones RxJS o manejo de errores.
   *
   * @param name - Nombre del evento a escuchar
   * @returns Observable con los datos del evento
   */
  on<T>(name: string): Observable<T> {
    return this.eventSubject.pipe(
      filter(event => event.name === name),
      map(event => event.data as T)
    );
  }

  /**
   * Suscribe a un evento especifico usando Signal.
   * Usar cuando quieres reactividad con Signals de Angular.
   * El Signal mantiene el ultimo valor emitido.
   *
   * @param name - Nombre del evento a escuchar
   * @returns Signal con los datos del evento (undefined hasta que se emita)
   */
  onSignal<T>(name: string): Signal<T | undefined> {
    const cacheKey = name;

    if (!this.signalCache.has(cacheKey)) {
      const observable = this.on<T>(name);
      const sig = toSignal(observable);
      this.signalCache.set(cacheKey, sig as Signal<unknown>);
    }

    return this.signalCache.get(cacheKey) as Signal<T | undefined>;
  }

  /**
   * Suscribe a multiples eventos.
   *
   * @param names - Array de nombres de eventos
   * @returns Observable con el evento completo
   */
  onMany(names: string[]): Observable<BusEvent> {
    return this.eventSubject.pipe(
      filter(event => names.includes(event.name))
    );
  }

  /**
   * Suscribe a todos los eventos (util para debugging).
   *
   * @returns Observable con todos los eventos
   */
  onAll(): Observable<BusEvent> {
    return this.eventSubject.asObservable();
  }

  /**
   * Obtiene el ultimo evento de un tipo especifico del historial.
   *
   * @param name - Nombre del evento
   * @returns El ultimo evento o undefined
   */
  getLastEvent<T>(name: string): BusEvent<T> | undefined {
    const history = this._eventHistory();
    return history.filter(e => e.name === name).pop() as BusEvent<T> | undefined;
  }

  /**
   * Limpia el historial de eventos.
   */
  clearHistory(): void {
    this._eventHistory.set([]);
  }

  /**
   * Computed: Numero de eventos en historial
   */
  readonly historyCount = computed(() => this._eventHistory().length);
}
