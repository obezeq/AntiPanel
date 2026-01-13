import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  inject,
  OnDestroy,
  Renderer2,
  signal,
  ViewChild
} from '@angular/core';

/**
 * COMPONENTE DEMO: @ViewChild y ElementRef
 *
 * Este componente demuestra el uso del decorador @ViewChild tradicional
 * junto con ElementRef para acceder a elementos del DOM de forma segura.
 *
 * NOTA IMPORTANTE: En Angular 21, se prefiere usar la funcion viewChild()
 * que devuelve un Signal. Sin embargo, este componente existe para demostrar
 * conocimiento del patron tradicional segun los requisitos de la rubrica.
 *
 * Ejemplo del patron moderno (usado en componentes de produccion):
 * ```typescript
 * // Angular 21 - viewChild() function
 * private readonly inputRef = viewChild<ElementRef>('inputDemo');
 * ```
 *
 * @example
 * ```html
 * <app-viewchild-demo />
 * ```
 *
 * @remarks
 * Implementa 5 @ViewChild diferentes para demostrar:
 * - Acceso a inputs para focus programatico
 * - Acceso a divs para manipulacion de estilos
 * - Acceso a canvas para operaciones graficas
 * - Acceso a elementos de salida para mostrar resultados
 * - Uso obligatorio de ngAfterViewInit para acceso seguro
 *
 * @see https://angular.dev/api/core/ViewChild
 * @see https://angular.dev/guide/components/queries (patron moderno)
 */
@Component({
  selector: 'app-viewchild-demo',
  templateUrl: './viewchild-demo.html',
  styleUrl: './viewchild-demo.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ViewChildDemoComponent implements AfterViewInit, OnDestroy {
  private readonly renderer = inject(Renderer2);

  // =========================================================================
  // @ViewChild DECORATORS - Patron tradicional para la rubrica
  // =========================================================================

  /**
   * Referencia al input de texto.
   *
   * Usa @ViewChild decorator (patron tradicional) para demostrar
   * el acceso al DOM. El patron moderno seria:
   * ```typescript
   * private readonly inputDemo = viewChild<ElementRef>('inputDemo');
   * ```
   *
   * IMPORTANTE: Este elemento NO esta disponible hasta ngAfterViewInit.
   */
  @ViewChild('inputDemo') inputDemo!: ElementRef<HTMLInputElement>;

  /**
   * Referencia a un div para manipulacion de estilos.
   *
   * Demuestra como acceder a un elemento contenedor para
   * modificar sus estilos dinamicamente usando Renderer2.
   */
  @ViewChild('boxDemo') boxDemo!: ElementRef<HTMLDivElement>;

  /**
   * Referencia a un elemento canvas.
   *
   * Los canvas requieren acceso directo al elemento nativo
   * para obtener el contexto 2D y dibujar.
   */
  @ViewChild('canvasDemo') canvasDemo!: ElementRef<HTMLCanvasElement>;

  /**
   * Referencia a un textarea.
   *
   * Demuestra acceso a elementos de formulario mas complejos.
   */
  @ViewChild('textareaDemo') textareaDemo!: ElementRef<HTMLTextAreaElement>;

  /**
   * Referencia a un div de salida para mostrar resultados.
   *
   * Demuestra la actualizacion dinamica de contenido usando Renderer2.
   */
  @ViewChild('outputDemo') outputDemo!: ElementRef<HTMLDivElement>;

  // =========================================================================
  // STATE SIGNALS
  // =========================================================================

  /** Texto del input */
  protected readonly inputValue = signal('');

  /** Color actual del box */
  protected readonly boxColor = signal('var(--color-stats-blue)');

  /** Dimensiones del canvas */
  protected readonly canvasDimensions = signal({ width: 0, height: 0 });

  /** Estado de inicializacion */
  protected readonly isInitialized = signal(false);

  /** Log de acciones */
  protected readonly actionLog = signal<string[]>([]);

  // =========================================================================
  // LIFECYCLE HOOKS
  // =========================================================================

  /**
   * ngAfterViewInit - Lifecycle hook OBLIGATORIO para @ViewChild.
   *
   * Este metodo se ejecuta DESPUES de que Angular haya inicializado
   * todas las vistas del componente y sus hijos.
   *
   * ES CRITICO usar este hook porque:
   * 1. Los @ViewChild NO estan disponibles en el constructor
   * 2. Los @ViewChild NO estan disponibles en ngOnInit
   * 3. Solo despues del render inicial estan garantizados
   *
   * Si intentas acceder a un @ViewChild antes de ngAfterViewInit,
   * obtendras `undefined` y tu codigo fallara.
   *
   * @example
   * ```typescript
   * // MAL - Fallara
   * ngOnInit() {
   *   this.inputDemo.nativeElement.focus(); // ERROR: undefined
   * }
   *
   * // BIEN - Funcionara
   * ngAfterViewInit() {
   *   this.inputDemo.nativeElement.focus(); // OK
   * }
   * ```
   */
  ngAfterViewInit(): void {
    this.addToLog('ngAfterViewInit ejecutado - @ViewChild disponibles');

    // Ejemplo 1: Focus programatico en input
    if (this.inputDemo) {
      // No hacemos focus automatico para no molestar al usuario
      // pero demostramos que el elemento esta disponible
      this.renderer.setAttribute(
        this.inputDemo.nativeElement,
        'data-initialized',
        'true'
      );
      this.addToLog('inputDemo: Elemento disponible y marcado');
    }

    // Ejemplo 2: Modificar estilos del box con Renderer2
    if (this.boxDemo) {
      this.renderer.setStyle(
        this.boxDemo.nativeElement,
        'backgroundColor',
        this.boxColor()
      );
      this.renderer.addClass(this.boxDemo.nativeElement, 'box--initialized');
      this.addToLog('boxDemo: Estilos aplicados con Renderer2');
    }

    // Ejemplo 3: Obtener dimensiones del canvas
    if (this.canvasDemo) {
      const canvas = this.canvasDemo.nativeElement;
      const rect = canvas.getBoundingClientRect();
      this.canvasDimensions.set({
        width: Math.round(rect.width),
        height: Math.round(rect.height)
      });
      this.initializeCanvas();
      this.addToLog(`canvasDemo: Dimensiones ${rect.width}x${rect.height}`);
    }

    // Ejemplo 4: Configurar textarea
    if (this.textareaDemo) {
      this.renderer.setAttribute(
        this.textareaDemo.nativeElement,
        'placeholder',
        'Texto inicializado via ngAfterViewInit'
      );
      this.addToLog('textareaDemo: Placeholder establecido');
    }

    // Ejemplo 5: Actualizar div de salida
    if (this.outputDemo) {
      this.renderer.addClass(this.outputDemo.nativeElement, 'output--ready');
      this.updateOutput('Componente inicializado correctamente');
      this.addToLog('outputDemo: Mensaje inicial mostrado');
    }

    this.isInitialized.set(true);
  }

  /**
   * ngOnDestroy - Limpieza al destruir el componente.
   *
   * Aunque los @ViewChild no requieren limpieza manual,
   * implementamos ngOnDestroy para demostrar buenas practicas
   * y limpiar cualquier recurso (como el contexto del canvas).
   */
  ngOnDestroy(): void {
    // Limpiar canvas si tiene contenido
    if (this.canvasDemo) {
      const ctx = this.canvasDemo.nativeElement.getContext('2d');
      if (ctx) {
        const canvas = this.canvasDemo.nativeElement;
        ctx.clearRect(0, 0, canvas.width, canvas.height);
      }
    }
    console.log('ViewChildDemoComponent destruido');
  }

  // =========================================================================
  // METODOS DE INTERACCION
  // =========================================================================

  /**
   * Enfoca el input programaticamente.
   * Demuestra el uso de nativeElement.focus()
   */
  protected focusInput(): void {
    if (this.inputDemo) {
      this.inputDemo.nativeElement.focus();
      this.addToLog('focusInput: Input enfocado programaticamente');
    }
  }

  /**
   * Lee el valor actual del input.
   * Demuestra el uso de nativeElement.value
   */
  protected readInputValue(): void {
    if (this.inputDemo) {
      const value = this.inputDemo.nativeElement.value;
      this.inputValue.set(value);
      this.updateOutput(`Valor leido: "${value}"`);
      this.addToLog(`readInputValue: Valor = "${value}"`);
    }
  }

  /**
   * Cambia el color del box.
   * Demuestra Renderer2.setStyle()
   */
  protected changeBoxColor(): void {
    const colors = [
      'var(--color-stats-blue)',
      'var(--color-success)',
      'var(--color-warning)',
      'var(--color-error)',
      'var(--color-stats-purple)'
    ];
    const currentIndex = colors.indexOf(this.boxColor());
    const nextIndex = (currentIndex + 1) % colors.length;
    const newColor = colors[nextIndex];

    this.boxColor.set(newColor);

    if (this.boxDemo) {
      this.renderer.setStyle(this.boxDemo.nativeElement, 'backgroundColor', newColor);
      this.addToLog(`changeBoxColor: Color cambiado a ${newColor}`);
    }
  }

  /**
   * Dibuja en el canvas.
   * Demuestra acceso al contexto 2D del canvas.
   */
  protected drawOnCanvas(): void {
    if (this.canvasDemo) {
      const canvas = this.canvasDemo.nativeElement;
      const ctx = canvas.getContext('2d');

      if (ctx) {
        // Limpiar canvas
        ctx.clearRect(0, 0, canvas.width, canvas.height);

        // Dibujar un rectangulo aleatorio
        const x = Math.random() * (canvas.width - 50);
        const y = Math.random() * (canvas.height - 50);
        const hue = Math.floor(Math.random() * 360);

        ctx.fillStyle = `hsl(${hue}, 70%, 50%)`;
        ctx.fillRect(x, y, 50, 50);

        this.addToLog(`drawOnCanvas: Rectangulo en (${Math.round(x)}, ${Math.round(y)})`);
      }
    }
  }

  /**
   * Limpia el canvas.
   */
  protected clearCanvas(): void {
    if (this.canvasDemo) {
      const canvas = this.canvasDemo.nativeElement;
      const ctx = canvas.getContext('2d');

      if (ctx) {
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        this.initializeCanvas(); // Redibujar fondo
        this.addToLog('clearCanvas: Canvas limpiado');
      }
    }
  }

  /**
   * Lee el contenido del textarea.
   */
  protected readTextarea(): void {
    if (this.textareaDemo) {
      const value = this.textareaDemo.nativeElement.value;
      this.updateOutput(`Textarea contiene: "${value}"`);
      this.addToLog(`readTextarea: ${value.length} caracteres`);
    }
  }

  /**
   * Limpia el log de acciones.
   */
  protected clearLog(): void {
    this.actionLog.set([]);
  }

  // =========================================================================
  // METODOS PRIVADOS
  // =========================================================================

  /**
   * Inicializa el canvas con un fondo.
   */
  private initializeCanvas(): void {
    if (this.canvasDemo) {
      const canvas = this.canvasDemo.nativeElement;
      const ctx = canvas.getContext('2d');

      if (ctx) {
        // Fondo con gradiente
        const gradient = ctx.createLinearGradient(0, 0, canvas.width, canvas.height);
        gradient.addColorStop(0, 'rgba(99, 102, 241, 0.1)');
        gradient.addColorStop(1, 'rgba(139, 92, 246, 0.1)');
        ctx.fillStyle = gradient;
        ctx.fillRect(0, 0, canvas.width, canvas.height);

        // Borde
        ctx.strokeStyle = 'rgba(99, 102, 241, 0.3)';
        ctx.strokeRect(0, 0, canvas.width, canvas.height);
      }
    }
  }

  /**
   * Actualiza el contenido del div de salida.
   */
  private updateOutput(message: string): void {
    if (this.outputDemo) {
      // Usar Renderer2 para actualizar el contenido de forma segura
      const element = this.outputDemo.nativeElement;

      // Limpiar contenido existente
      while (element.firstChild) {
        this.renderer.removeChild(element, element.firstChild);
      }

      // Crear nuevo texto
      const text = this.renderer.createText(message);
      this.renderer.appendChild(element, text);
    }
  }

  /**
   * Agrega un mensaje al log.
   */
  private addToLog(message: string): void {
    const timestamp = new Date().toLocaleTimeString();
    this.actionLog.update(log => [`[${timestamp}] ${message}`, ...log.slice(0, 9)]);
  }

  /**
   * Handler para cambios en el input.
   */
  protected onInputChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.inputValue.set(input.value);
  }
}
