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
 * COMPONENTE DEMO: Renderer2 para manipulacion del DOM
 *
 * Este componente demuestra TODOS los metodos principales de Renderer2
 * para manipulacion segura del DOM en Angular.
 *
 * IMPORTANTE: Renderer2 es la forma recomendada de manipular el DOM
 * en Angular porque:
 * 1. Es abstracto y funciona en diferentes plataformas (browser, server, web worker)
 * 2. Es seguro contra ataques XSS cuando se usa correctamente
 * 3. Permite que Angular rastree los cambios del DOM
 *
 * Metodos demostrados:
 * - createElement: Crear elementos del DOM
 * - createText: Crear nodos de texto
 * - appendChild: Agregar hijos a un elemento
 * - removeChild: Eliminar hijos de un elemento
 * - setStyle: Aplicar estilos CSS
 * - removeStyle: Eliminar estilos CSS
 * - addClass: Agregar clases CSS
 * - removeClass: Eliminar clases CSS
 * - setAttribute: Establecer atributos
 * - removeAttribute: Eliminar atributos
 * - listen: Agregar event listeners
 *
 * @example
 * ```html
 * <app-renderer2-demo />
 * ```
 *
 * @see https://angular.dev/api/core/Renderer2
 */
@Component({
  selector: 'app-renderer2-demo',
  templateUrl: './renderer2-demo.html',
  styleUrl: './renderer2-demo.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class Renderer2DemoComponent implements AfterViewInit, OnDestroy {
  private readonly renderer = inject(Renderer2);

  // =========================================================================
  // @ViewChild REFERENCES
  // =========================================================================

  /**
   * Contenedor principal donde se agregan elementos dinamicos.
   */
  @ViewChild('dynamicContainer') dynamicContainer!: ElementRef<HTMLDivElement>;

  /**
   * Contenedor para demostrar estilos.
   */
  @ViewChild('styleContainer') styleContainer!: ElementRef<HTMLDivElement>;

  /**
   * Contenedor para demostrar clases.
   */
  @ViewChild('classContainer') classContainer!: ElementRef<HTMLDivElement>;

  /**
   * Contenedor para demostrar atributos.
   */
  @ViewChild('attributeContainer') attributeContainer!: ElementRef<HTMLDivElement>;

  // =========================================================================
  // STATE SIGNALS
  // =========================================================================

  /** Elementos creados dinamicamente */
  protected readonly createdElements = signal<HTMLElement[]>([]);

  /** Contador de elementos creados */
  protected readonly elementCount = signal(0);

  /** Log de operaciones Renderer2 */
  protected readonly operationLog = signal<string[]>([]);

  /** Estado de inicializacion */
  protected readonly isInitialized = signal(false);

  /** Estilos actuales aplicados */
  protected readonly currentStyles = signal<Record<string, string>>({});

  /** Clases actuales aplicadas */
  protected readonly currentClasses = signal<string[]>([]);

  /** Atributos actuales aplicados */
  protected readonly currentAttributes = signal<Record<string, string>>({});

  // =========================================================================
  // PRIVATE PROPERTIES
  // =========================================================================

  /** Funciones para cancelar listeners */
  private readonly unlistenFunctions: (() => void)[] = [];

  /** Referencia a elementos creados para limpieza */
  private readonly elementsToClean: HTMLElement[] = [];

  /** Exponer Object para uso en template (Object.keys, Object.entries) */
  protected readonly Object = Object;

  // =========================================================================
  // LIFECYCLE HOOKS
  // =========================================================================

  /**
   * ngAfterViewInit - Inicializacion despues del render.
   *
   * Usamos Renderer2 para configurar el estado inicial de los contenedores.
   */
  ngAfterViewInit(): void {
    this.addToLog('ngAfterViewInit - Componente inicializado');

    // Configurar contenedor dinamico
    if (this.dynamicContainer) {
      this.renderer.setAttribute(
        this.dynamicContainer.nativeElement,
        'data-demo',
        'renderer2'
      );
      this.renderer.addClass(this.dynamicContainer.nativeElement, 'container--ready');
      this.addToLog('setAttribute: data-demo="renderer2"');
      this.addToLog('addClass: container--ready');
    }

    // Configurar contenedor de estilos
    if (this.styleContainer) {
      this.renderer.setStyle(this.styleContainer.nativeElement, 'minHeight', '60px');
      this.addToLog('setStyle: minHeight="60px"');
    }

    this.isInitialized.set(true);
  }

  /**
   * ngOnDestroy - Limpieza OBLIGATORIA.
   *
   * Este hook es CRITICO para prevenir memory leaks:
   * 1. Eliminar todos los elementos creados dinamicamente
   * 2. Cancelar todos los event listeners creados con listen()
   *
   * Si no hacemos esta limpieza:
   * - Los elementos huerfanos permanecen en memoria
   * - Los listeners siguen activos aunque el componente no exista
   * - Se producen memory leaks que degradan la aplicacion
   */
  ngOnDestroy(): void {
    // 1. Eliminar todos los elementos creados dinamicamente
    this.elementsToClean.forEach(element => {
      if (element.parentNode) {
        this.renderer.removeChild(element.parentNode, element);
      }
    });
    this.elementsToClean.length = 0;

    // 2. Cancelar todos los listeners
    this.unlistenFunctions.forEach(unlisten => unlisten());
    this.unlistenFunctions.length = 0;

    console.log('Renderer2DemoComponent destruido - Recursos liberados');
  }

  // =========================================================================
  // METODOS DE CREACION DE ELEMENTOS
  // =========================================================================

  /**
   * Crea un elemento dinamico usando Renderer2.
   *
   * Demuestra:
   * - createElement: Crear el elemento div
   * - createText: Crear nodo de texto
   * - appendChild: Agregar texto al div y div al contenedor
   * - setStyle: Aplicar estilos
   * - addClass: Agregar clase
   * - setAttribute: Agregar atributos
   * - listen: Agregar click listener
   */
  protected createDynamicElement(): void {
    if (!this.dynamicContainer) return;

    const container = this.dynamicContainer.nativeElement;

    // 1. createElement - Crear elemento div
    const element = this.renderer.createElement('div');
    this.addToLog('createElement: div');

    // 2. setAttribute - Agregar atributos
    const id = `dynamic-${Date.now()}`;
    this.renderer.setAttribute(element, 'id', id);
    this.renderer.setAttribute(element, 'data-created', new Date().toISOString());
    this.renderer.setAttribute(element, 'role', 'listitem');
    this.addToLog(`setAttribute: id="${id}"`);

    // 3. addClass - Agregar clases
    this.renderer.addClass(element, 'dynamic-element');
    this.renderer.addClass(element, 'fade-in');
    this.addToLog('addClass: dynamic-element, fade-in');

    // 4. setStyle - Aplicar estilos
    const hue = Math.floor(Math.random() * 360);
    this.renderer.setStyle(element, 'backgroundColor', `hsl(${hue}, 70%, 95%)`);
    this.renderer.setStyle(element, 'borderLeft', `4px solid hsl(${hue}, 70%, 50%)`);
    this.renderer.setStyle(element, 'padding', '0.75rem 1rem');
    this.renderer.setStyle(element, 'marginBottom', '0.5rem');
    this.renderer.setStyle(element, 'borderRadius', '6px');
    this.renderer.setStyle(element, 'cursor', 'pointer');
    this.renderer.setStyle(element, 'transition', 'all 0.2s ease');
    this.addToLog(`setStyle: backgroundColor=hsl(${hue}, 70%, 95%)`);

    // 5. createText y appendChild - Agregar contenido
    const count = this.elementCount() + 1;
    const text = this.renderer.createText(`Elemento #${count} - Click para eliminar`);
    this.renderer.appendChild(element, text);
    this.addToLog('createText + appendChild: texto agregado');

    // 6. listen - Agregar event listener con auto-eliminacion
    const unlisten = this.renderer.listen(element, 'click', () => {
      this.removeElement(element, unlisten);
    });
    this.unlistenFunctions.push(unlisten);
    this.addToLog('listen: click handler agregado');

    // 7. appendChild - Agregar al contenedor
    this.renderer.appendChild(container, element);
    this.addToLog('appendChild: elemento agregado al contenedor');

    // Actualizar estado
    this.elementsToClean.push(element);
    this.elementCount.update(c => c + 1);
    this.createdElements.update(elements => [...elements, element]);
  }

  /**
   * Elimina un elemento especifico.
   *
   * Demuestra:
   * - removeChild: Eliminar el elemento del DOM
   */
  private removeElement(element: HTMLElement, unlisten: () => void): void {
    if (!this.dynamicContainer) return;

    // Remover listener primero
    unlisten();
    const unlistenIndex = this.unlistenFunctions.indexOf(unlisten);
    if (unlistenIndex > -1) {
      this.unlistenFunctions.splice(unlistenIndex, 1);
    }

    // Remover del array de limpieza
    const cleanIndex = this.elementsToClean.indexOf(element);
    if (cleanIndex > -1) {
      this.elementsToClean.splice(cleanIndex, 1);
    }

    // Remover del DOM
    this.renderer.removeChild(this.dynamicContainer.nativeElement, element);
    this.addToLog('removeChild: elemento eliminado');

    // Actualizar estado
    this.createdElements.update(elements =>
      elements.filter(el => el !== element)
    );
  }

  /**
   * Elimina el ultimo elemento creado.
   *
   * Demuestra removeChild con el ultimo elemento.
   */
  protected removeLastElement(): void {
    const elements = this.createdElements();
    if (elements.length === 0) return;

    const lastElement = elements[elements.length - 1];

    // Buscar y ejecutar el unlisten correspondiente
    // (El ultimo unlisten en la lista corresponde al ultimo elemento)
    if (this.unlistenFunctions.length > 0) {
      const unlisten = this.unlistenFunctions.pop();
      if (unlisten) unlisten();
    }

    // Remover del array de limpieza
    const cleanIndex = this.elementsToClean.indexOf(lastElement);
    if (cleanIndex > -1) {
      this.elementsToClean.splice(cleanIndex, 1);
    }

    // Remover del DOM
    if (this.dynamicContainer) {
      this.renderer.removeChild(this.dynamicContainer.nativeElement, lastElement);
    }

    this.addToLog('removeChild: ultimo elemento eliminado');
    this.createdElements.update(elements => elements.slice(0, -1));
  }

  /**
   * Elimina todos los elementos creados.
   */
  protected clearAllElements(): void {
    if (!this.dynamicContainer) return;

    const container = this.dynamicContainer.nativeElement;

    // Eliminar todos los listeners
    this.unlistenFunctions.forEach(unlisten => unlisten());
    this.unlistenFunctions.length = 0;

    // Eliminar todos los elementos
    this.elementsToClean.forEach(element => {
      if (container.contains(element)) {
        this.renderer.removeChild(container, element);
      }
    });
    this.elementsToClean.length = 0;

    this.addToLog('removeChild: todos los elementos eliminados');
    this.createdElements.set([]);
  }

  // =========================================================================
  // METODOS DE ESTILOS
  // =========================================================================

  /**
   * Aplica un estilo usando setStyle.
   */
  protected applyStyle(property: string, value: string): void {
    if (!this.styleContainer) return;

    this.renderer.setStyle(this.styleContainer.nativeElement, property, value);
    this.currentStyles.update(styles => ({ ...styles, [property]: value }));
    this.addToLog(`setStyle: ${property}="${value}"`);
  }

  /**
   * Elimina un estilo usando removeStyle.
   */
  protected removeStyle(property: string): void {
    if (!this.styleContainer) return;

    this.renderer.removeStyle(this.styleContainer.nativeElement, property);
    this.currentStyles.update(styles => {
      const newStyles = { ...styles };
      delete newStyles[property];
      return newStyles;
    });
    this.addToLog(`removeStyle: ${property}`);
  }

  /**
   * Aplica estilos predefinidos para demo.
   */
  protected applyPredefinedStyles(): void {
    this.applyStyle('backgroundColor', 'var(--color-primary)');
    this.applyStyle('color', 'white');
    this.applyStyle('padding', '1rem');
    this.applyStyle('borderRadius', '8px');
    this.applyStyle('fontWeight', '500');
  }

  /**
   * Resetea todos los estilos.
   */
  protected resetStyles(): void {
    if (!this.styleContainer) return;

    const styles = this.currentStyles();
    Object.keys(styles).forEach(property => {
      this.renderer.removeStyle(this.styleContainer.nativeElement, property);
    });

    this.currentStyles.set({});
    this.addToLog('removeStyle: todos los estilos eliminados');
  }

  // =========================================================================
  // METODOS DE CLASES
  // =========================================================================

  /**
   * Agrega una clase usando addClass.
   */
  protected addClassToElement(className: string): void {
    if (!this.classContainer) return;

    this.renderer.addClass(this.classContainer.nativeElement, className);
    this.currentClasses.update(classes => [...classes, className]);
    this.addToLog(`addClass: ${className}`);
  }

  /**
   * Elimina una clase usando removeClass.
   */
  protected removeClassFromElement(className: string): void {
    if (!this.classContainer) return;

    this.renderer.removeClass(this.classContainer.nativeElement, className);
    this.currentClasses.update(classes => classes.filter(c => c !== className));
    this.addToLog(`removeClass: ${className}`);
  }

  /**
   * Alterna la clase 'highlighted'.
   */
  protected toggleHighlight(): void {
    const classes = this.currentClasses();
    if (classes.includes('highlighted')) {
      this.removeClassFromElement('highlighted');
    } else {
      this.addClassToElement('highlighted');
    }
  }

  /**
   * Alterna la clase 'scaled'.
   */
  protected toggleScale(): void {
    const classes = this.currentClasses();
    if (classes.includes('scaled')) {
      this.removeClassFromElement('scaled');
    } else {
      this.addClassToElement('scaled');
    }
  }

  // =========================================================================
  // METODOS DE ATRIBUTOS
  // =========================================================================

  /**
   * Establece un atributo usando setAttribute.
   */
  protected setAttributeOnElement(name: string, value: string): void {
    if (!this.attributeContainer) return;

    this.renderer.setAttribute(this.attributeContainer.nativeElement, name, value);
    this.currentAttributes.update(attrs => ({ ...attrs, [name]: value }));
    this.addToLog(`setAttribute: ${name}="${value}"`);
  }

  /**
   * Elimina un atributo usando removeAttribute.
   */
  protected removeAttributeFromElement(name: string): void {
    if (!this.attributeContainer) return;

    this.renderer.removeAttribute(this.attributeContainer.nativeElement, name);
    this.currentAttributes.update(attrs => {
      const newAttrs = { ...attrs };
      delete newAttrs[name];
      return newAttrs;
    });
    this.addToLog(`removeAttribute: ${name}`);
  }

  /**
   * Aplica atributos de accesibilidad.
   */
  protected applyAccessibilityAttributes(): void {
    this.setAttributeOnElement('role', 'region');
    this.setAttributeOnElement('aria-label', 'Demo de atributos');
    this.setAttributeOnElement('tabindex', '0');
  }

  /**
   * Aplica atributos de datos.
   */
  protected applyDataAttributes(): void {
    this.setAttributeOnElement('data-demo', 'true');
    this.setAttributeOnElement('data-timestamp', Date.now().toString());
    this.setAttributeOnElement('data-version', '1.0');
  }

  // =========================================================================
  // UTILIDADES
  // =========================================================================

  /**
   * Limpia el log de operaciones.
   */
  protected clearLog(): void {
    this.operationLog.set([]);
  }

  /**
   * Agrega un mensaje al log.
   */
  private addToLog(message: string): void {
    const timestamp = new Date().toLocaleTimeString();
    this.operationLog.update(log => [`[${timestamp}] ${message}`, ...log.slice(0, 14)]);
  }
}
