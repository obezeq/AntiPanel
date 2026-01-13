import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  contentChildren,
  effect,
  ElementRef,
  inject,
  input,
  OnDestroy,
  output,
  Renderer2,
  signal,
  viewChild
} from '@angular/core';
import { Tab } from './tab';
import { TabPanel } from './tab-panel';

/**
 * Tabs Component - Angular 21
 *
 * Componente contenedor para sistema de tabs con navegacion
 * por teclado completa (WAI-ARIA Tabs pattern).
 *
 * @example
 * ```html
 * <app-tabs [defaultTab]="'tab1'" (tabChange)="onTabChange($event)">
 *   <app-tab value="tab1" label="First Tab" />
 *   <app-tab value="tab2" label="Second Tab" />
 *
 *   <app-tab-panel value="tab1">Content 1</app-tab-panel>
 *   <app-tab-panel value="tab2">Content 2</app-tab-panel>
 * </app-tabs>
 * ```
 *
 * @remarks
 * - Usa contentChildren() para acceder a Tabs y TabPanels proyectados
 * - Implementa AfterViewInit para inicializacion post-render
 * - Usa Renderer2 para manipulacion segura del DOM
 * - Navegacion completa: ArrowLeft, ArrowRight, Home, End
 */
@Component({
  selector: 'app-tabs',
  templateUrl: './tabs.html',
  styleUrl: './tabs.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    'class': 'tabs',
    '(keydown)': 'onKeydown($event)'
  }
})
export class Tabs implements AfterViewInit, OnDestroy {
  private readonly renderer = inject(Renderer2);

  /**
   * Referencia al contenedor de la tablist.
   */
  private readonly tablistContainer = viewChild<ElementRef>('tablistContainer');

  /**
   * Tabs proyectados via ng-content.
   */
  readonly tabs = contentChildren(Tab);

  /**
   * Paneles proyectados via ng-content.
   */
  readonly panels = contentChildren(TabPanel);

  /**
   * Tab activo por defecto.
   */
  readonly defaultTab = input<string>('');

  /**
   * Tab actualmente seleccionado (signal para reactividad).
   */
  readonly activeTab = signal<string>('');

  /**
   * Emite cuando cambia el tab activo.
   */
  readonly tabChange = output<string>();

  /** ID unico para el componente */
  private readonly instanceId = `tabs-${Math.random().toString(36).slice(2, 9)}`;

  constructor() {
    /**
     * Effect para sincronizar el estado activo con tabs y panels.
     */
    effect(() => {
      const active = this.activeTab();
      const tabsList = this.tabs();
      const panelsList = this.panels();

      // Actualizar estado de cada tab
      tabsList.forEach(tab => {
        tab.setActive(tab.value() === active);
      });

      // Actualizar visibilidad de cada panel
      panelsList.forEach(panel => {
        panel.setVisible(panel.value() === active);
      });
    });
  }

  /**
   * Inicializacion post-render.
   *
   * Establece el tab inicial y configura IDs ARIA.
   */
  ngAfterViewInit(): void {
    const tablist = this.tablistContainer();
    if (tablist) {
      // Renderer2: setAttribute para manipulacion segura
      this.renderer.setAttribute(tablist.nativeElement, 'data-instance', this.instanceId);
    }

    // Establecer tab activo inicial
    const tabsList = this.tabs();
    if (tabsList.length > 0) {
      const initialTab = this.defaultTab() || tabsList[0].value();
      this.activeTab.set(initialTab);

      // Registrar cada tab con este contenedor
      tabsList.forEach((tab, index) => {
        tab.registerParent(this, index);
      });
    }

    // Registrar panels
    const panelsList = this.panels();
    panelsList.forEach(panel => {
      panel.registerParent(this);
    });
  }

  /**
   * Limpieza al destruir.
   */
  ngOnDestroy(): void {
    // Cleanup si es necesario
  }

  /**
   * Selecciona un tab por valor.
   */
  selectTab(value: string): void {
    if (this.activeTab() !== value) {
      this.activeTab.set(value);
      this.tabChange.emit(value);
    }
  }

  /**
   * Navegacion por teclado WAI-ARIA para tabs.
   *
   * - ArrowRight: Siguiente tab
   * - ArrowLeft: Tab anterior
   * - Home: Primer tab
   * - End: Ultimo tab
   */
  protected onKeydown(event: KeyboardEvent): void {
    const tabsList = this.tabs();
    if (!tabsList.length) return;

    const currentIndex = tabsList.findIndex(tab => tab.value() === this.activeTab());

    switch (event.key) {
      case 'ArrowRight':
        event.preventDefault();
        this.focusAndSelectTab((currentIndex + 1) % tabsList.length);
        break;

      case 'ArrowLeft':
        event.preventDefault();
        this.focusAndSelectTab(currentIndex <= 0 ? tabsList.length - 1 : currentIndex - 1);
        break;

      case 'Home':
        event.preventDefault();
        this.focusAndSelectTab(0);
        break;

      case 'End':
        event.preventDefault();
        this.focusAndSelectTab(tabsList.length - 1);
        break;
    }
  }

  /**
   * Enfoca y selecciona un tab por indice.
   */
  private focusAndSelectTab(index: number): void {
    const tabsList = this.tabs();
    if (index >= 0 && index < tabsList.length) {
      const tab = tabsList[index];
      tab.focus();
      this.selectTab(tab.value());
    }
  }

  /**
   * Genera ID para un tab basado en su valor.
   */
  getTabId(value: string): string {
    return `${this.instanceId}-tab-${value}`;
  }

  /**
   * Genera ID para un panel basado en su valor.
   */
  getPanelId(value: string): string {
    return `${this.instanceId}-panel-${value}`;
  }
}
