import {
  Directive,
  ElementRef,
  inject,
  input,
  OnDestroy,
  Renderer2,
  signal
} from '@angular/core';

/**
 * Highlight Directive - Angular 21
 *
 * Directiva que aplica un efecto de resaltado al hacer hover o focus.
 * Demuestra uso extensivo de Renderer2 para manipulacion segura del DOM.
 *
 * @example
 * ```html
 * <div appHighlight>Resaltame al pasar el mouse</div>
 * <div appHighlight highlightColor="var(--color-success)">Color personalizado</div>
 * ```
 *
 * @remarks
 * - Usa Renderer2: setStyle, removeStyle, addClass, removeClass
 * - Implementa ngOnDestroy para limpieza (criterio 1.3)
 * - Usa host property para eventos (Angular 21 pattern)
 * - Signal-based state management
 */
@Directive({
  selector: '[appHighlight]',
  standalone: true,
  host: {
    '(mouseenter)': 'onMouseEnter()',
    '(mouseleave)': 'onMouseLeave()',
    '(focus)': 'onFocus()',
    '(blur)': 'onBlur()'
  }
})
export class HighlightDirective implements OnDestroy {
  private readonly renderer = inject(Renderer2);
  private readonly el = inject(ElementRef);

  /**
   * Color de resaltado. Puede ser cualquier valor CSS valido.
   * Por defecto usa el color primario claro del design system.
   */
  readonly highlightColor = input<string>('var(--color-tiny-info)');

  /**
   * Escala de transformacion al resaltar.
   * Por defecto 1.02 para un efecto sutil.
   */
  readonly highlightScale = input<number>(1.02);

  /**
   * Estado interno de resaltado usando signal.
   */
  private readonly isHighlighted = signal(false);

  /**
   * Guarda el color de fondo original para restaurarlo.
   */
  private originalBackground: string | null = null;

  /**
   * Handler para mouseenter - activa el resaltado.
   */
  protected onMouseEnter(): void {
    this.highlight();
  }

  /**
   * Handler para mouseleave - desactiva el resaltado.
   */
  protected onMouseLeave(): void {
    this.unhighlight();
  }

  /**
   * Handler para focus - activa el resaltado (accesibilidad).
   */
  protected onFocus(): void {
    this.highlight();
  }

  /**
   * Handler para blur - desactiva el resaltado.
   */
  protected onBlur(): void {
    this.unhighlight();
  }

  /**
   * Aplica el efecto de resaltado usando Renderer2.
   *
   * Demuestra uso de:
   * - setStyle: aplicar estilos inline
   * - addClass: agregar clase CSS
   */
  private highlight(): void {
    if (this.isHighlighted()) return;

    const element = this.el.nativeElement;

    // Guardar color original si no se ha guardado
    if (this.originalBackground === null) {
      this.originalBackground = element.style.backgroundColor || '';
    }

    // Renderer2: setStyle para aplicar estilos de forma segura
    this.renderer.setStyle(element, 'backgroundColor', this.highlightColor());
    this.renderer.setStyle(element, 'transform', `scale(${this.highlightScale()})`);
    this.renderer.setStyle(element, 'transition', 'background-color 0.2s ease, transform 0.2s ease');

    // Renderer2: addClass para agregar clase de estado
    this.renderer.addClass(element, 'highlighted');
    this.renderer.addClass(element, 'app-highlight--active');

    this.isHighlighted.set(true);
  }

  /**
   * Remueve el efecto de resaltado usando Renderer2.
   *
   * Demuestra uso de:
   * - removeStyle: quitar estilos inline
   * - removeClass: quitar clase CSS
   */
  private unhighlight(): void {
    if (!this.isHighlighted()) return;

    const element = this.el.nativeElement;

    // Renderer2: removeStyle/setStyle para restaurar estado original
    if (this.originalBackground) {
      this.renderer.setStyle(element, 'backgroundColor', this.originalBackground);
    } else {
      this.renderer.removeStyle(element, 'backgroundColor');
    }
    this.renderer.setStyle(element, 'transform', 'scale(1)');

    // Renderer2: removeClass para quitar clases de estado
    this.renderer.removeClass(element, 'highlighted');
    this.renderer.removeClass(element, 'app-highlight--active');

    this.isHighlighted.set(false);
  }

  /**
   * Limpieza al destruir la directiva.
   *
   * Implementa ngOnDestroy para asegurar que los estilos se limpian
   * si el componente se destruye mientras esta resaltado.
   * Cumple criterio 1.3 (eliminacion de elementos/estilos).
   */
  ngOnDestroy(): void {
    if (this.isHighlighted()) {
      const element = this.el.nativeElement;

      // Limpiar estilos aplicados
      this.renderer.removeStyle(element, 'backgroundColor');
      this.renderer.removeStyle(element, 'transform');
      this.renderer.removeStyle(element, 'transition');

      // Limpiar clases
      this.renderer.removeClass(element, 'highlighted');
      this.renderer.removeClass(element, 'app-highlight--active');
    }
  }
}
