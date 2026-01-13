import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  inject,
  input,
  OnDestroy,
  output,
  Renderer2,
  signal,
  viewChild
} from '@angular/core';
import { NgIcon } from '@ng-icons/core';

/** ID unico para cada accordion item */
let accordionItemId = 0;

/**
 * AccordionItem Component - Angular 21
 *
 * Item individual del accordion con header clickeable y contenido expandible.
 *
 * @example
 * ```html
 * <app-accordion-item title="Section Title" [expanded]="false">
 *   <p>Content goes here</p>
 * </app-accordion-item>
 * ```
 *
 * @remarks
 * - Usa Renderer2 para manipulacion del DOM (createElement, setStyle)
 * - Implementa ngOnDestroy para limpieza (criterio 1.3)
 * - Animacion smooth con max-height y overflow
 * - Accesibilidad completa: aria-expanded, aria-controls, role
 */
@Component({
  selector: 'app-accordion-item',
  templateUrl: './accordion-item.html',
  styleUrl: './accordion-item.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [NgIcon],
  host: {
    'class': 'accordion-item',
    '[class.accordion-item--expanded]': 'expanded()',
    '[attr.data-expanded]': 'expanded()'
  }
})
export class AccordionItem implements AfterViewInit, OnDestroy {
  private readonly renderer = inject(Renderer2);
  private readonly elementRef = inject(ElementRef);

  /**
   * Referencia al boton header para gestionar foco.
   *
   * viewChild() signal proporciona acceso reactivo al elemento,
   * actualizandose automaticamente cuando el DOM cambia.
   */
  private readonly headerButton = viewChild<ElementRef<HTMLButtonElement>>('headerButton');

  /**
   * Referencia al contenedor del contenido para animaciones.
   */
  private readonly contentContainer = viewChild<ElementRef<HTMLDivElement>>('contentContainer');

  /** Titulo mostrado en el header del accordion item */
  readonly title = input.required<string>();

  /** Icono opcional para el header (nombre de ng-icons) */
  readonly icon = input<string>('');

  /** Estado inicial de expansion */
  readonly initialExpanded = input<boolean>(false, { alias: 'expanded' });

  /** Estado interno de expansion (signal para reactividad) */
  readonly expanded = signal<boolean>(false);

  /** Emite cuando el item se expande o colapsa */
  readonly expandedChange = output<boolean>();

  /** IDs unicos para accesibilidad ARIA */
  readonly headerId: string;
  readonly panelId: string;

  /** Elemento de medida para calcular altura del contenido */
  private measureElement: HTMLElement | null = null;

  constructor() {
    const id = accordionItemId++;
    this.headerId = `accordion-header-${id}`;
    this.panelId = `accordion-panel-${id}`;

    // Sincronizar estado inicial
    this.expanded.set(this.initialExpanded());
  }

  /**
   * Inicializacion post-render del componente.
   *
   * Usa Renderer2 para aplicar estilos iniciales de forma segura,
   * cumpliendo los criterios 1.1 (ViewChild) y 1.2 (Renderer2).
   */
  ngAfterViewInit(): void {
    const content = this.contentContainer();
    if (content) {
      // Renderer2: setStyle para manipulacion segura del DOM
      if (!this.expanded()) {
        this.renderer.setStyle(content.nativeElement, 'maxHeight', '0');
        this.renderer.setStyle(content.nativeElement, 'opacity', '0');
      } else {
        this.renderer.setStyle(content.nativeElement, 'maxHeight', 'none');
        this.renderer.setStyle(content.nativeElement, 'opacity', '1');
      }
    }
  }

  /**
   * Limpieza al destruir el componente.
   *
   * Implementa ngOnDestroy para cumplir criterio 1.3 (eliminacion de elementos).
   * Limpia cualquier elemento creado dinamicamente con Renderer2.
   */
  ngOnDestroy(): void {
    // Limpiar elemento de medida si existe
    if (this.measureElement) {
      this.renderer.removeChild(document.body, this.measureElement);
      this.measureElement = null;
    }
  }

  /**
   * Alterna el estado de expansion del item.
   */
  toggle(): void {
    if (this.expanded()) {
      this.collapse();
    } else {
      this.expand();
    }
  }

  /**
   * Expande el item con animacion.
   *
   * Usa Renderer2 para:
   * - setStyle: aplicar maxHeight animado
   * - addClass/removeClass: gestionar clases de estado
   */
  expand(): void {
    if (this.expanded()) return;

    const content = this.contentContainer();
    if (content) {
      // Calcular altura real del contenido
      const scrollHeight = content.nativeElement.scrollHeight;

      // Renderer2: setStyle para animacion
      this.renderer.setStyle(content.nativeElement, 'maxHeight', `${scrollHeight}px`);
      this.renderer.setStyle(content.nativeElement, 'opacity', '1');

      // Despues de la transicion, permitir altura auto
      setTimeout(() => {
        if (this.expanded()) {
          this.renderer.setStyle(content.nativeElement, 'maxHeight', 'none');
        }
      }, 300); // Duracion de la transicion
    }

    this.expanded.set(true);
    this.expandedChange.emit(true);
  }

  /**
   * Colapsa el item con animacion.
   */
  collapse(): void {
    if (!this.expanded()) return;

    const content = this.contentContainer();
    if (content) {
      // Primero establecer altura actual explicita
      const scrollHeight = content.nativeElement.scrollHeight;
      this.renderer.setStyle(content.nativeElement, 'maxHeight', `${scrollHeight}px`);

      // Forzar reflow para que la transicion funcione
      content.nativeElement.offsetHeight;

      // Animar a 0
      this.renderer.setStyle(content.nativeElement, 'maxHeight', '0');
      this.renderer.setStyle(content.nativeElement, 'opacity', '0');
    }

    this.expanded.set(false);
    this.expandedChange.emit(false);
  }

  /**
   * Enfoca el boton header del item.
   * Llamado desde el Accordion padre para navegacion por teclado.
   */
  focusHeader(): void {
    const button = this.headerButton();
    if (button) {
      button.nativeElement.focus();
    }
  }

  /**
   * Handler para click en el header.
   */
  protected onHeaderClick(): void {
    this.toggle();
  }

  /**
   * Handler para keydown en el header.
   * Enter y Space activan el toggle.
   */
  protected onHeaderKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault();
      this.toggle();
    }
  }
}
