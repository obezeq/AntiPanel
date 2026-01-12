import { ChangeDetectionStrategy, Component, signal } from '@angular/core';
import { Modal } from '../../../../components/shared/modal/modal';

/**
 * Modal Section - Fase 4
 *
 * Demuestra:
 * - Componente Modal con diferentes tamanos (sm, md, lg)
 * - Apertura/cierre con signals
 * - Focus trap para accesibilidad
 * - Cierre con tecla Escape
 * - Cierre al hacer clic en overlay
 * - Contenido personalizado con ng-content
 */
@Component({
  selector: 'app-modal-section',
  templateUrl: './modal-section.html',
  styleUrl: './modal-section.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [Modal]
})
export class ModalSection {
  /** Estado de apertura del modal principal */
  protected readonly isModalOpen = signal(false);

  /** Tamano actual del modal */
  protected readonly modalSize = signal<'sm' | 'md' | 'lg'>('md');

  /** Titulo del modal actual */
  protected readonly modalTitle = signal('');

  /** Estado del modal de confirmacion */
  protected readonly isConfirmModalOpen = signal(false);

  /** Resultado de la confirmacion */
  protected readonly confirmResult = signal<string | null>(null);

  /** Estado del modal sin overlay close */
  protected readonly isNoOverlayModalOpen = signal(false);

  /**
   * Abre el modal con el tamano especificado
   */
  protected openModal(size: 'sm' | 'md' | 'lg'): void {
    const titles: Record<'sm' | 'md' | 'lg', string> = {
      sm: 'Modal Pequeno',
      md: 'Modal Mediano',
      lg: 'Modal Grande'
    };
    this.modalSize.set(size);
    this.modalTitle.set(titles[size]);
    this.isModalOpen.set(true);
  }

  /**
   * Cierra el modal principal
   */
  protected closeModal(): void {
    this.isModalOpen.set(false);
  }

  /**
   * Abre el modal de confirmacion
   */
  protected openConfirmModal(): void {
    this.confirmResult.set(null);
    this.isConfirmModalOpen.set(true);
  }

  /**
   * Cierra el modal de confirmacion
   */
  protected closeConfirmModal(): void {
    this.isConfirmModalOpen.set(false);
  }

  /**
   * Maneja la confirmacion
   */
  protected onConfirm(): void {
    this.confirmResult.set('Confirmado');
    this.closeConfirmModal();
  }

  /**
   * Maneja la cancelacion
   */
  protected onCancel(): void {
    this.confirmResult.set('Cancelado');
    this.closeConfirmModal();
  }

  /**
   * Abre el modal que no cierra con overlay
   */
  protected openNoOverlayModal(): void {
    this.isNoOverlayModalOpen.set(true);
  }

  /**
   * Cierra el modal que no cierra con overlay
   */
  protected closeNoOverlayModal(): void {
    this.isNoOverlayModalOpen.set(false);
  }
}
