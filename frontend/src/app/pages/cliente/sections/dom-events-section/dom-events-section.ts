import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  inject,
  signal,
  viewChild
} from '@angular/core';
import { NgIcon } from '@ng-icons/core';
import { Tab, TabContent, TabList, TabPanel, Tabs } from '@angular/aria/tabs';
import { TooltipDirective } from '../../../../directives/tooltip.directive';
import { NotificationService } from '../../../../services/notification.service';

/**
 * DOM Events Section - Fase 1
 *
 * Demuestra:
 * - Tabs con @angular/aria
 * - Tooltips accesibles
 * - ViewChild y ElementRef
 * - Event binding (click, keydown, focus, blur)
 * - preventDefault y stopPropagation
 * - Accordion nativo con details/summary
 */
@Component({
  selector: 'app-dom-events-section',
  templateUrl: './dom-events-section.html',
  styleUrl: './dom-events-section.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    NgIcon,
    TooltipDirective,
    Tabs,
    TabList,
    Tab,
    TabPanel,
    TabContent
  ]
})
export class DomEventsSection {
  private readonly notificationService = inject(NotificationService);

  // =========================================================================
  // TABS STATE
  // =========================================================================

  protected readonly selectedTab = signal('eventos');

  // =========================================================================
  // VIEWCHILD DEMO
  // =========================================================================

  /** Referencia al input para focus programatico */
  protected readonly demoInput = viewChild<ElementRef<HTMLInputElement>>('demoInput');

  /** Referencia al contenedor para manipular estilos */
  protected readonly demoBox = viewChild<ElementRef<HTMLDivElement>>('demoBox');

  /** Texto del input */
  protected readonly inputText = signal('');

  /** Color actual del box */
  protected readonly boxColor = signal('var(--color-stats-blue)');

  protected focusInput(): void {
    const input = this.demoInput();
    if (input) {
      input.nativeElement.focus();
      this.notificationService.info('Input enfocado via ViewChild');
    }
  }

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
    this.boxColor.set(colors[nextIndex]);
  }

  protected getBoxText(): void {
    const box = this.demoBox();
    if (box) {
      const text = box.nativeElement.innerText;
      this.notificationService.info(`Texto del box: "${text}"`);
    }
  }

  // =========================================================================
  // EVENT BINDING DEMO
  // =========================================================================

  /** Contador de clicks */
  protected readonly clickCount = signal(0);

  /** Ultima tecla presionada */
  protected readonly lastKey = signal('');

  /** Estado de focus */
  protected readonly hasFocus = signal(false);

  /** Posicion del mouse */
  protected readonly mousePosition = signal({ x: 0, y: 0 });

  protected onButtonClick(): void {
    this.clickCount.update(c => c + 1);
  }

  protected onKeyDown(event: KeyboardEvent): void {
    this.lastKey.set(event.key);
  }

  protected onFocus(): void {
    this.hasFocus.set(true);
  }

  protected onBlur(): void {
    this.hasFocus.set(false);
  }

  protected onMouseMove(event: MouseEvent): void {
    this.mousePosition.set({ x: event.offsetX, y: event.offsetY });
  }

  // =========================================================================
  // PREVENT DEFAULT / STOP PROPAGATION DEMO
  // =========================================================================

  /** Log de eventos */
  protected readonly eventLog = signal<string[]>([]);

  protected onLinkClick(event: MouseEvent): void {
    event.preventDefault();
    this.addToLog('Link click - preventDefault() aplicado');
  }

  protected onFormSubmit(event: SubmitEvent): void {
    event.preventDefault();
    this.addToLog('Form submit - preventDefault() aplicado');
    this.notificationService.success('Formulario procesado sin recargar');
  }

  protected onOuterClick(): void {
    this.addToLog('Click en contenedor exterior');
  }

  protected onInnerClick(event: MouseEvent): void {
    event.stopPropagation();
    this.addToLog('Click en boton interior - stopPropagation() aplicado');
  }

  protected onInnerClickNoPropagation(event: MouseEvent): void {
    this.addToLog('Click en boton interior - sin stopPropagation()');
  }

  protected clearEventLog(): void {
    this.eventLog.set([]);
  }

  private addToLog(message: string): void {
    const timestamp = new Date().toLocaleTimeString();
    this.eventLog.update(log => [`[${timestamp}] ${message}`, ...log.slice(0, 9)]);
  }

  // =========================================================================
  // INPUT HANDLERS
  // =========================================================================

  protected onInputChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.inputText.set(input.value);
  }
}
