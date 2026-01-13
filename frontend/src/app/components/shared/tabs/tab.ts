import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  inject,
  input,
  Renderer2,
  signal,
  viewChild
} from '@angular/core';
import { NgIcon } from '@ng-icons/core';
import type { Tabs } from './tabs';

/**
 * Tab Component - Angular 21
 *
 * Boton individual de tab que activa su panel correspondiente.
 *
 * @example
 * ```html
 * <app-tab value="tab1" label="First Tab" icon="matHome" />
 * ```
 *
 * @remarks
 * - Usa Renderer2 para actualizar atributos ARIA
 * - Se registra con el padre Tabs para coordinacion
 */
@Component({
  selector: 'app-tab',
  templateUrl: './tab.html',
  styleUrl: './tab.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [NgIcon],
  host: {
    'class': 'tab',
    '[class.tab--active]': 'isActive()',
    'role': 'presentation'
  }
})
export class Tab {
  private readonly renderer = inject(Renderer2);

  /**
   * Referencia al boton del tab.
   */
  private readonly tabButton = viewChild<ElementRef<HTMLButtonElement>>('tabButton');

  /** Valor unico que identifica este tab */
  readonly value = input.required<string>();

  /** Texto mostrado en el tab */
  readonly label = input.required<string>();

  /** Icono opcional (nombre de ng-icons) */
  readonly icon = input<string>('');

  /** Estado activo interno */
  readonly isActive = signal<boolean>(false);

  /** Referencia al componente padre */
  private parentTabs: Tabs | null = null;

  /** Indice en la lista de tabs */
  private tabIndex = 0;

  /**
   * Registra este tab con el componente padre.
   */
  registerParent(parent: Tabs, index: number): void {
    this.parentTabs = parent;
    this.tabIndex = index;
    this.updateAriaAttributes();
  }

  /**
   * Actualiza el estado activo del tab.
   */
  setActive(active: boolean): void {
    this.isActive.set(active);
    this.updateAriaAttributes();
  }

  /**
   * Enfoca el boton del tab.
   */
  focus(): void {
    const button = this.tabButton();
    if (button) {
      button.nativeElement.focus();
    }
  }

  /**
   * Handler para click en el tab.
   */
  protected onClick(): void {
    if (this.parentTabs) {
      this.parentTabs.selectTab(this.value());
    }
  }

  /**
   * Actualiza atributos ARIA usando Renderer2.
   */
  private updateAriaAttributes(): void {
    const button = this.tabButton();
    if (button && this.parentTabs) {
      // Renderer2: setAttribute para manipulacion segura
      this.renderer.setAttribute(
        button.nativeElement,
        'aria-selected',
        this.isActive().toString()
      );
      this.renderer.setAttribute(
        button.nativeElement,
        'tabindex',
        this.isActive() ? '0' : '-1'
      );
      this.renderer.setAttribute(
        button.nativeElement,
        'id',
        this.parentTabs.getTabId(this.value())
      );
      this.renderer.setAttribute(
        button.nativeElement,
        'aria-controls',
        this.parentTabs.getPanelId(this.value())
      );
    }
  }
}
