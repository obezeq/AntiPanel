import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  inject,
  input,
  output,
  effect,
  Renderer2,
  signal,
  viewChild
} from '@angular/core';
import { DOCUMENT } from '@angular/common';

/** Selector for focusable elements */
const FOCUSABLE_SELECTOR = [
  'button:not([disabled])',
  'input:not([disabled])',
  'select:not([disabled])',
  'textarea:not([disabled])',
  'a[href]',
  '[tabindex]:not([tabindex="-1"])'
].join(', ');

/**
 * Modal Component - Angular 21
 *
 * Componente de dialogo modal accesible con focus trap y gestion de teclado.
 *
 * @example
 * ```html
 * <app-modal [isOpen]="showModal()" (closeRequest)="showModal.set(false)">
 *   <p>Modal content here</p>
 * </app-modal>
 * ```
 *
 * @remarks
 * - Usa `viewChild()` signal (Angular 21) para acceso al elemento dialog
 * - Implementa AfterViewInit para inicializacion post-render
 * - Usa `host` property para eventos globales (Angular 21 best practice)
 * - Usa Renderer2 para manipulacion segura del DOM
 * - Focus trap completo con Tab/Shift+Tab
 * - Cierre con ESC y click en overlay configurable
 *
 * @see https://angular.dev/guide/components/host-elements
 */
@Component({
  selector: 'app-modal',
  templateUrl: './modal.html',
  styleUrl: './modal.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    'class': 'modal-host',
    // Angular 21: host property para eventos globales (reemplaza @HostListener)
    '(window:resize)': 'onWindowResize()'
  }
})
export class Modal implements AfterViewInit {
  private readonly document = inject(DOCUMENT);
  private readonly renderer = inject(Renderer2);

  /** Element that had focus before modal opened */
  private previouslyFocusedElement: HTMLElement | null = null;

  /** Whether the modal is open */
  readonly isOpen = input<boolean>(false);

  /** Modal title */
  readonly title = input<string>('');

  /** Whether to show close button */
  readonly showCloseButton = input<boolean>(true);

  /** Whether clicking overlay closes the modal */
  readonly closeOnOverlay = input<boolean>(true);

  /** Whether ESC key closes the modal */
  readonly closeOnEsc = input<boolean>(true);

  /** Size variant */
  readonly size = input<'sm' | 'md' | 'lg'>('md');

  /** Emits when modal close is requested */
  readonly closeRequest = output<void>();

  /** Dimensiones de la ventana para posicionamiento responsive */
  private readonly windowDimensions = signal({ width: 0, height: 0 });

  /**
   * Referencia al elemento dialog nativo.
   *
   * En Angular 21, `viewChild()` es una signal query que devuelve
   * un Signal<ElementRef<T> | undefined>. A diferencia del decorator
   * @ViewChild tradicional:
   *
   * - Se actualiza automaticamente cuando el elemento esta disponible
   * - Se integra con el sistema de signals para reactividad
   * - Puede usarse con effect() para reaccionar a cambios
   *
   * @example
   * ```typescript
   * // Acceso al elemento nativo
   * const dialog = this.dialogRef()?.nativeElement;
   * if (dialog) {
   *   dialog.showModal();
   * }
   * ```
   *
   * @see https://angular.dev/guide/signals/queries
   */
  private readonly dialogRef = viewChild<ElementRef<HTMLDialogElement>>('dialogRef');

  constructor() {
    effect(() => {
      const dialog = this.dialogRef()?.nativeElement;
      if (!dialog) return;

      if (this.isOpen()) {
        // Save currently focused element
        this.previouslyFocusedElement = this.document.activeElement as HTMLElement;

        dialog.showModal();
        this.document.body.style.overflow = 'hidden';

        // Focus the first focusable element or the dialog itself
        requestAnimationFrame(() => {
          const firstFocusable = dialog.querySelector<HTMLElement>(FOCUSABLE_SELECTOR);
          if (firstFocusable) {
            firstFocusable.focus();
          }
        });
      } else {
        dialog.close();
        this.document.body.style.overflow = '';

        // Restore focus to previously focused element
        if (this.previouslyFocusedElement && this.previouslyFocusedElement.focus) {
          this.previouslyFocusedElement.focus();
          this.previouslyFocusedElement = null;
        }
      }
    });

    // Inicializar dimensiones de ventana
    this.windowDimensions.set({
      width: window.innerWidth,
      height: window.innerHeight
    });
  }

  /**
   * Lifecycle hook ejecutado despues de inicializar las vistas.
   *
   * En Angular 21, aunque effect() y afterNextRender() son las APIs
   * modernas para reaccionar a cambios, ngAfterViewInit sigue siendo
   * valido para inicializacion post-render.
   *
   * Aqui verificamos que el dialogRef esta disponible y aplicamos
   * cualquier configuracion inicial necesaria usando Renderer2.
   */
  ngAfterViewInit(): void {
    const dialog = this.dialogRef();
    if (dialog) {
      // Renderer2: setAttribute para marcar el dialogo como inicializado
      this.renderer.setAttribute(dialog.nativeElement, 'data-modal-initialized', 'true');
      this.renderer.addClass(dialog.nativeElement, 'modal--initialized');
    }
  }

  /**
   * Listener global para eventos de redimension de ventana.
   *
   * Este metodo es llamado via la propiedad `host` del componente:
   * `'(window:resize)': 'onWindowResize()'`
   *
   * En Angular 21, se prefiere usar `host` property sobre @HostListener.
   * Esto es util para:
   * - Ajustar el posicionamiento del modal en pantallas pequeñas
   * - Recalcular dimensiones para animaciones
   * - Detectar cambios de orientacion en moviles
   *
   * @see https://angular.dev/guide/components/host-elements
   */
  protected onWindowResize(): void {
    this.windowDimensions.set({
      width: window.innerWidth,
      height: window.innerHeight
    });

    // Si el modal esta abierto, ajustar posicionamiento si es necesario
    if (this.isOpen()) {
      const dialog = this.dialogRef();
      if (dialog) {
        // Aplicar ajustes responsive usando Renderer2
        const isMobile = window.innerWidth < 768;
        if (isMobile) {
          this.renderer.addClass(dialog.nativeElement, 'modal--mobile');
        } else {
          this.renderer.removeClass(dialog.nativeElement, 'modal--mobile');
        }
      }
    }
  }

  protected onOverlayClick(event: MouseEvent): void {
    if (this.closeOnOverlay() && event.target === event.currentTarget) {
      this.closeRequest.emit();
    }
  }

  protected onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Escape' && this.closeOnEsc()) {
      event.preventDefault();
      this.closeRequest.emit();
      return;
    }

    // Focus trap: handle Tab and Shift+Tab
    if (event.key === 'Tab') {
      const dialog = this.dialogRef()?.nativeElement;
      if (!dialog) return;

      const focusableElements = dialog.querySelectorAll<HTMLElement>(FOCUSABLE_SELECTOR);
      if (focusableElements.length === 0) return;

      const firstElement = focusableElements[0];
      const lastElement = focusableElements[focusableElements.length - 1];

      if (event.shiftKey) {
        // Shift+Tab: if on first element, go to last
        if (this.document.activeElement === firstElement) {
          event.preventDefault();
          lastElement.focus();
        }
      } else {
        // Tab: if on last element, go to first
        if (this.document.activeElement === lastElement) {
          event.preventDefault();
          firstElement.focus();
        }
      }
    }
  }

  protected onCloseClick(): void {
    this.closeRequest.emit();
  }
}
