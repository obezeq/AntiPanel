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
import type { Tabs } from './tabs';

/**
 * TabPanel Component - Angular 21
 *
 * Panel de contenido asociado a un Tab.
 *
 * @example
 * ```html
 * <app-tab-panel value="tab1">
 *   <p>Content for tab 1</p>
 * </app-tab-panel>
 * ```
 *
 * @remarks
 * - Se muestra/oculta basado en el tab activo
 * - Usa Renderer2 para gestionar visibilidad y atributos ARIA
 */
@Component({
  selector: 'app-tab-panel',
  templateUrl: './tab-panel.html',
  styleUrl: './tab-panel.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    'class': 'tab-panel',
    '[class.tab-panel--visible]': 'isVisible()',
    '[attr.hidden]': '!isVisible() || null'
  }
})
export class TabPanel {
  private readonly renderer = inject(Renderer2);

  /**
   * Referencia al contenedor del panel.
   */
  private readonly panelContainer = viewChild<ElementRef<HTMLDivElement>>('panelContainer');

  /** Valor que asocia este panel con su tab */
  readonly value = input.required<string>();

  /** Estado de visibilidad */
  readonly isVisible = signal<boolean>(false);

  /** Referencia al componente padre */
  private parentTabs: Tabs | null = null;

  /**
   * Registra este panel con el componente padre.
   */
  registerParent(parent: Tabs): void {
    this.parentTabs = parent;
    this.updateAriaAttributes();
  }

  /**
   * Actualiza la visibilidad del panel.
   */
  setVisible(visible: boolean): void {
    this.isVisible.set(visible);
    this.updateAriaAttributes();
  }

  /**
   * Actualiza atributos ARIA usando Renderer2.
   */
  private updateAriaAttributes(): void {
    const panel = this.panelContainer();
    if (panel && this.parentTabs) {
      // Renderer2: setAttribute para manipulacion segura
      this.renderer.setAttribute(
        panel.nativeElement,
        'id',
        this.parentTabs.getPanelId(this.value())
      );
      this.renderer.setAttribute(
        panel.nativeElement,
        'aria-labelledby',
        this.parentTabs.getTabId(this.value())
      );
    }
  }
}
