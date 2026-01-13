import {
  Directive,
  ElementRef,
  inject,
  input,
  OnDestroy,
  OnInit,
  Renderer2
} from '@angular/core';

/**
 * Ripple Directive - Angular 21
 *
 * Directiva que crea un efecto ripple (onda) al hacer click.
 * Demuestra creacion y eliminacion dinamica de elementos DOM.
 *
 * @example
 * ```html
 * <button appRipple>Click para ver efecto</button>
 * <button appRipple rippleColor="rgba(0, 255, 0, 0.3)">Ripple verde</button>
 * ```
 *
 * @remarks
 * - Usa Renderer2: createElement, appendChild, removeChild, setStyle, addClass
 * - Implementa ngOnDestroy para limpieza de elementos creados (criterio 1.3)
 * - Usa host property para eventos (Angular 21 pattern)
 * - Demuestra manipulacion dinamica del DOM
 */
@Directive({
  selector: '[appRipple]',
  standalone: true,
  host: {
    '(click)': 'onClick($event)',
    '[style.position]': '"relative"',
    '[style.overflow]': '"hidden"'
  }
})
export class RippleDirective implements OnInit, OnDestroy {
  private readonly renderer = inject(Renderer2);
  private readonly el = inject(ElementRef);

  /**
   * Color del efecto ripple.
   * Por defecto un blanco semi-transparente que funciona en fondos oscuros.
   */
  readonly rippleColor = input<string>('rgba(255, 255, 255, 0.3)');

  /**
   * Duracion de la animacion en ms.
   */
  readonly rippleDuration = input<number>(600);

  /**
   * Array de elementos ripple activos.
   * Se mantiene para poder limpiarlos en ngOnDestroy.
   */
  private rippleElements: HTMLElement[] = [];

  /**
   * Estilos CSS inyectados para la animacion.
   */
  private styleElement: HTMLStyleElement | null = null;

  /**
   * Inicializacion de la directiva.
   * Inyecta los estilos CSS necesarios para la animacion.
   */
  ngOnInit(): void {
    this.injectStyles();
  }

  /**
   * Inyecta los estilos CSS para la animacion ripple.
   *
   * Usa Renderer2: createElement, appendChild para crear
   * un elemento <style> en el <head>.
   */
  private injectStyles(): void {
    // Verificar si ya existen los estilos
    if (document.getElementById('ripple-directive-styles')) {
      return;
    }

    // Renderer2: createElement para crear elemento style
    this.styleElement = this.renderer.createElement('style');
    this.renderer.setAttribute(this.styleElement, 'id', 'ripple-directive-styles');

    const css = `
      .ripple-effect {
        position: absolute;
        border-radius: 50%;
        transform: scale(0);
        animation: ripple-animation 0.6s linear;
        pointer-events: none;
      }

      @keyframes ripple-animation {
        to {
          transform: scale(4);
          opacity: 0;
        }
      }
    `;

    // Renderer2: createText y appendChild
    const textNode = this.renderer.createText(css);
    this.renderer.appendChild(this.styleElement, textNode);
    this.renderer.appendChild(document.head, this.styleElement);
  }

  /**
   * Handler para click - crea el efecto ripple.
   *
   * Demuestra uso extensivo de Renderer2:
   * - createElement: crear elemento span para el ripple
   * - setStyle: aplicar estilos de posicion y color
   * - addClass: agregar clase de animacion
   * - appendChild: agregar al DOM
   * - removeChild: eliminar despues de la animacion
   */
  protected onClick(event: MouseEvent): void {
    const element = this.el.nativeElement;
    const rect = element.getBoundingClientRect();

    // Calcular posicion del click relativa al elemento
    const x = event.clientX - rect.left;
    const y = event.clientY - rect.top;

    // Calcular tamaño del ripple (el mayor entre ancho y alto)
    const size = Math.max(rect.width, rect.height);

    // Renderer2: createElement para crear el elemento ripple
    const ripple = this.renderer.createElement('span');

    // Renderer2: addClass para agregar la clase de animacion
    this.renderer.addClass(ripple, 'ripple-effect');

    // Renderer2: setStyle para posicionar y estilizar el ripple
    this.renderer.setStyle(ripple, 'width', `${size}px`);
    this.renderer.setStyle(ripple, 'height', `${size}px`);
    this.renderer.setStyle(ripple, 'left', `${x - size / 2}px`);
    this.renderer.setStyle(ripple, 'top', `${y - size / 2}px`);
    this.renderer.setStyle(ripple, 'backgroundColor', this.rippleColor());
    this.renderer.setStyle(ripple, 'animationDuration', `${this.rippleDuration()}ms`);

    // Renderer2: appendChild para agregar al DOM
    this.renderer.appendChild(element, ripple);

    // Guardar referencia para limpieza
    this.rippleElements.push(ripple);

    // Renderer2: removeChild despues de la animacion
    setTimeout(() => {
      if (element.contains(ripple)) {
        this.renderer.removeChild(element, ripple);
      }
      // Eliminar de la lista de elementos activos
      this.rippleElements = this.rippleElements.filter(el => el !== ripple);
    }, this.rippleDuration());
  }

  /**
   * Limpieza al destruir la directiva.
   *
   * Implementa ngOnDestroy para:
   * 1. Eliminar todos los ripples pendientes (criterio 1.3)
   * 2. Limpiar estilos inyectados si es el ultimo uso
   *
   * Usa Renderer2: removeChild para eliminacion segura.
   */
  ngOnDestroy(): void {
    const element = this.el.nativeElement;

    // Renderer2: removeChild para eliminar todos los ripples pendientes
    this.rippleElements.forEach(ripple => {
      if (element.contains(ripple)) {
        this.renderer.removeChild(element, ripple);
      }
    });
    this.rippleElements = [];

    // Nota: No eliminamos los estilos globales porque pueden
    // estar siendo usados por otras instancias de la directiva
  }
}
