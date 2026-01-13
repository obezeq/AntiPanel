import {
  Directive,
  ElementRef,
  inject,
  input,
  OnDestroy,
  Renderer2,
  signal
} from '@angular/core';

export type TooltipPosition = 'top' | 'bottom' | 'left' | 'right';

/** Gap between tooltip and host element (base-8: 8px) */
const TOOLTIP_OFFSET = 8;

/**
 * Directiva Tooltip - Angular 21
 *
 * Muestra un tooltip accesible al hacer hover o focus sobre un elemento.
 * Implementa las mejores practicas de Angular 21 usando `host` property.
 *
 * @example
 * ```html
 * <button appTooltip="Texto del tooltip">Hover me</button>
 * <button appTooltip="Tooltip abajo" tooltipPosition="bottom">Abajo</button>
 * ```
 *
 * @remarks
 * - Usa `host` property para eventos (Angular 21 best practice)
 * - Usa Renderer2 para createElement, appendChild, removeChild
 * - Implementa ngOnDestroy para limpieza de elementos DOM
 * - Posicionamiento inteligente que evita salirse del viewport
 *
 * @see https://angular.dev/guide/components/host-elements
 */
@Directive({
  selector: '[appTooltip]',
  standalone: true,
  host: {
    // Angular 21: host property para eventos (reemplaza @HostListener)
    '(mouseenter)': 'onShowTooltip()',
    '(focus)': 'onShowTooltip()',
    '(mouseleave)': 'onHideTooltip()',
    '(blur)': 'onHideTooltip()',
    '(keydown.escape)': 'onEscape()'
  }
})
export class TooltipDirective implements OnDestroy {
  private readonly el = inject(ElementRef);
  private readonly renderer = inject(Renderer2);

  /** Texto del tooltip */
  readonly appTooltip = input.required<string>();

  /** Posicion del tooltip */
  readonly tooltipPosition = input<TooltipPosition>('top');

  /** Delay antes de mostrar (ms) */
  readonly tooltipDelay = input<number>(200);

  /** Estado interno */
  private readonly isVisible = signal(false);
  private tooltipElement: HTMLElement | null = null;
  private showTimeout: ReturnType<typeof setTimeout> | null = null;
  private readonly tooltipId = `tooltip-${Math.random().toString(36).slice(2, 9)}`;

  /**
   * Muestra el tooltip con delay.
   * Llamado via host property: '(mouseenter)' y '(focus)'
   */
  protected onShowTooltip(): void {
    this.showTimeout = setTimeout(() => {
      this.show();
    }, this.tooltipDelay());
  }

  /**
   * Oculta el tooltip.
   * Llamado via host property: '(mouseleave)' y '(blur)'
   */
  protected onHideTooltip(): void {
    if (this.showTimeout) {
      clearTimeout(this.showTimeout);
      this.showTimeout = null;
    }
    this.hide();
  }

  /**
   * Oculta el tooltip con tecla Escape.
   * Llamado via host property: '(keydown.escape)'
   */
  protected onEscape(): void {
    this.hide();
  }

  ngOnDestroy(): void {
    if (this.showTimeout) {
      clearTimeout(this.showTimeout);
    }
    this.hide();
  }

  private show(): void {
    if (this.isVisible() || !this.appTooltip()) return;

    this.createTooltip();
    this.isVisible.set(true);
  }

  private hide(): void {
    if (!this.isVisible()) return;

    this.removeTooltip();
    this.isVisible.set(false);
  }

  private createTooltip(): void {
    // Crear elemento tooltip
    this.tooltipElement = this.renderer.createElement('div');
    this.renderer.setAttribute(this.tooltipElement, 'id', this.tooltipId);
    this.renderer.setAttribute(this.tooltipElement, 'role', 'tooltip');
    this.renderer.addClass(this.tooltipElement, 'app-tooltip');
    this.renderer.addClass(this.tooltipElement, `app-tooltip--${this.tooltipPosition()}`);

    // Crear contenido
    const text = this.renderer.createText(this.appTooltip());
    this.renderer.appendChild(this.tooltipElement, text);

    // Agregar al body
    this.renderer.appendChild(document.body, this.tooltipElement);

    // Posicionar
    this.positionTooltip();

    // Agregar aria-describedby al elemento host
    this.renderer.setAttribute(this.el.nativeElement, 'aria-describedby', this.tooltipId);

    // Animar entrada
    requestAnimationFrame(() => {
      if (this.tooltipElement) {
        this.renderer.addClass(this.tooltipElement, 'app-tooltip--visible');
      }
    });
  }

  private removeTooltip(): void {
    if (this.tooltipElement) {
      this.renderer.removeAttribute(this.el.nativeElement, 'aria-describedby');
      this.renderer.removeChild(document.body, this.tooltipElement);
      this.tooltipElement = null;
    }
  }

  private positionTooltip(): void {
    if (!this.tooltipElement) return;

    const hostRect = this.el.nativeElement.getBoundingClientRect();
    const tooltipRect = this.tooltipElement.getBoundingClientRect();
    const scrollTop = window.scrollY || document.documentElement.scrollTop;
    const scrollLeft = window.scrollX || document.documentElement.scrollLeft;

    let top = 0;
    let left = 0;

    switch (this.tooltipPosition()) {
      case 'top':
        top = hostRect.top + scrollTop - tooltipRect.height - TOOLTIP_OFFSET;
        left = hostRect.left + scrollLeft + (hostRect.width - tooltipRect.width) / 2;
        break;
      case 'bottom':
        top = hostRect.bottom + scrollTop + TOOLTIP_OFFSET;
        left = hostRect.left + scrollLeft + (hostRect.width - tooltipRect.width) / 2;
        break;
      case 'left':
        top = hostRect.top + scrollTop + (hostRect.height - tooltipRect.height) / 2;
        left = hostRect.left + scrollLeft - tooltipRect.width - TOOLTIP_OFFSET;
        break;
      case 'right':
        top = hostRect.top + scrollTop + (hostRect.height - tooltipRect.height) / 2;
        left = hostRect.right + scrollLeft + TOOLTIP_OFFSET;
        break;
    }

    // Ajustar si se sale de la pantalla
    const viewportWidth = window.innerWidth;
    const viewportHeight = window.innerHeight;

    if (left < 0) left = TOOLTIP_OFFSET;
    if (left + tooltipRect.width > viewportWidth) {
      left = viewportWidth - tooltipRect.width - TOOLTIP_OFFSET;
    }
    if (top < 0) top = TOOLTIP_OFFSET;
    if (top + tooltipRect.height > viewportHeight + scrollTop) {
      top = hostRect.top + scrollTop - tooltipRect.height - TOOLTIP_OFFSET;
    }

    this.renderer.setStyle(this.tooltipElement, 'top', `${top}px`);
    this.renderer.setStyle(this.tooltipElement, 'left', `${left}px`);
  }
}
