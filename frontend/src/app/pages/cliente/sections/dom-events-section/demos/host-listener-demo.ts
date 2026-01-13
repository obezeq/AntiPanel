import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  HostListener,
  inject,
  OnDestroy,
  Renderer2,
  signal,
  ViewChild
} from '@angular/core';

/**
 * COMPONENTE DEMO: @HostListener
 *
 * Este componente demuestra el uso del decorador @HostListener
 * para capturar eventos globales del documento y ventana.
 *
 * NOTA IMPORTANTE: En Angular 21, @HostListener esta deprecated en favor
 * de la propiedad `host` en el decorator del componente. Sin embargo,
 * este componente existe para demostrar conocimiento del patron tradicional
 * segun los requisitos de la rubrica academica.
 *
 * Los componentes de produccion de este proyecto (header.ts, modal.ts, etc.)
 * utilizan la propiedad `host` que es el patron moderno recomendado.
 *
 * @example
 * ```html
 * <app-host-listener-demo />
 * ```
 *
 * @remarks
 * Implementa 3 @HostListener diferentes:
 * - document:click - Para detectar click fuera del dropdown
 * - document:keydown.escape - Para cerrar con tecla ESC
 * - window:resize - Para detectar cambios de tamanio de ventana
 *
 * @see https://angular.dev/api/core/HostListener
 * @see https://angular.dev/guide/components/host-elements (patron moderno)
 */
@Component({
  selector: 'app-host-listener-demo',
  templateUrl: './host-listener-demo.html',
  styleUrl: './host-listener-demo.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HostListenerDemoComponent implements AfterViewInit, OnDestroy {
  private readonly renderer = inject(Renderer2);

  /**
   * Referencia al contenedor principal del demo.
   *
   * Usa @ViewChild decorator (patron tradicional) para demostrar
   * el acceso al DOM. En componentes de produccion usamos viewChild() function.
   */
  @ViewChild('demoContainer') demoContainer!: ElementRef<HTMLDivElement>;

  /**
   * Referencia al dropdown para detectar clicks dentro/fuera.
   */
  @ViewChild('dropdownContainer') dropdownContainer!: ElementRef<HTMLDivElement>;

  /** Estado de apertura del dropdown */
  protected readonly isDropdownOpen = signal(false);

  /** Ancho actual de la ventana */
  protected readonly windowWidth = signal(window.innerWidth);

  /** Altura actual de la ventana */
  protected readonly windowHeight = signal(window.innerHeight);

  /** Ultima vez que se presiono ESC */
  protected readonly lastEscapeTime = signal('');

  /** Contador de clicks fuera del dropdown */
  protected readonly clickOutsideCount = signal(0);

  /** Log de eventos capturados */
  protected readonly eventLog = signal<string[]>([]);

  // =========================================================================
  // @HostListener DECORATORS - Patron tradicional para la rubrica
  // =========================================================================

  /**
   * @HostListener para capturar clicks en el documento.
   *
   * Este es el patron TRADICIONAL para detectar clicks fuera de un elemento.
   * Usado comunmente para cerrar dropdowns, modales, y menus.
   *
   * El patron MODERNO (Angular 21) seria usar la propiedad `host`:
   * ```typescript
   * host: {
   *   '(document:click)': 'onDocumentClick($event)'
   * }
   * ```
   *
   * @param event - Evento de click del documento
   */
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (this.isDropdownOpen() && this.dropdownContainer) {
      const target = event.target as HTMLElement;
      const container = this.dropdownContainer.nativeElement;

      // Verificar si el click fue fuera del dropdown
      if (!container.contains(target)) {
        this.isDropdownOpen.set(false);
        this.clickOutsideCount.update(c => c + 1);
        this.addToLog('document:click - Dropdown cerrado (click fuera)');
      }
    }
  }

  /**
   * @HostListener para capturar tecla ESC en el documento.
   *
   * Angular permite especificar teclas especificas usando la sintaxis
   * `keydown.{key}`. Esto es mas limpio que verificar event.key manualmente.
   *
   * Otras variantes utiles:
   * - 'document:keydown.enter'
   * - 'document:keydown.arrowup'
   * - 'document:keydown.arrowdown'
   * - 'document:keydown.tab'
   */
  @HostListener('document:keydown.escape')
  onEscapeKey(): void {
    if (this.isDropdownOpen()) {
      this.isDropdownOpen.set(false);
      this.lastEscapeTime.set(new Date().toLocaleTimeString());
      this.addToLog('document:keydown.escape - Dropdown cerrado con ESC');
    }
  }

  /**
   * @HostListener para capturar resize de la ventana.
   *
   * Util para:
   * - Ajustar layouts responsive
   * - Recalcular posiciones de elementos
   * - Actualizar dimensiones de canvas
   * - Cerrar menus en cambios de viewport
   */
  @HostListener('window:resize')
  onWindowResize(): void {
    this.windowWidth.set(window.innerWidth);
    this.windowHeight.set(window.innerHeight);
    this.addToLog(`window:resize - ${window.innerWidth}x${window.innerHeight}`);
  }

  // =========================================================================
  // LIFECYCLE HOOKS
  // =========================================================================

  /**
   * ngAfterViewInit - Acceso seguro al DOM despues del render.
   *
   * En este punto los @ViewChild estan disponibles.
   * Usamos Renderer2 para manipulacion segura del DOM.
   */
  ngAfterViewInit(): void {
    if (this.demoContainer) {
      // Renderer2: setAttribute para marcar el componente como inicializado
      this.renderer.setAttribute(
        this.demoContainer.nativeElement,
        'data-demo',
        'host-listener'
      );
      this.renderer.setAttribute(
        this.demoContainer.nativeElement,
        'data-initialized',
        new Date().toISOString()
      );

      // Renderer2: addClass para aplicar clase de estado
      this.renderer.addClass(this.demoContainer.nativeElement, 'demo--initialized');
    }

    this.addToLog('ngAfterViewInit - Componente inicializado');
  }

  /**
   * ngOnDestroy - Limpieza al destruir el componente.
   *
   * Aunque @HostListener no requiere limpieza manual (Angular lo gestiona),
   * implementamos ngOnDestroy para demostrar buenas practicas.
   */
  ngOnDestroy(): void {
    // Angular gestiona automaticamente los @HostListener
    // No es necesario hacer removeEventListener
    console.log('HostListenerDemoComponent destruido');
  }

  // =========================================================================
  // METODOS DE INTERACCION
  // =========================================================================

  /**
   * Alterna el estado del dropdown.
   */
  protected toggleDropdown(): void {
    this.isDropdownOpen.update(open => !open);
    const state = this.isDropdownOpen() ? 'abierto' : 'cerrado';
    this.addToLog(`toggleDropdown - Dropdown ${state}`);
  }

  /**
   * Selecciona una opcion del dropdown.
   */
  protected selectOption(option: string): void {
    this.addToLog(`selectOption - Seleccionado: ${option}`);
    this.isDropdownOpen.set(false);
  }

  /**
   * Limpia el log de eventos.
   */
  protected clearLog(): void {
    this.eventLog.set([]);
  }

  /**
   * Agrega un mensaje al log de eventos.
   */
  private addToLog(message: string): void {
    const timestamp = new Date().toLocaleTimeString();
    this.eventLog.update(log => [`[${timestamp}] ${message}`, ...log.slice(0, 9)]);
  }
}
