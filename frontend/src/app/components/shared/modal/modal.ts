import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  inject,
  input,
  output,
  signal,
  effect,
  viewChild
} from '@angular/core';
import { DOCUMENT } from '@angular/common';

@Component({
  selector: 'app-modal',
  templateUrl: './modal.html',
  styleUrl: './modal.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class Modal {
  private readonly document = inject(DOCUMENT);

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

  private readonly dialogRef = viewChild<ElementRef<HTMLDialogElement>>('dialogRef');

  constructor() {
    effect(() => {
      const dialog = this.dialogRef()?.nativeElement;
      if (!dialog) return;

      if (this.isOpen()) {
        dialog.showModal();
        this.document.body.style.overflow = 'hidden';
      } else {
        dialog.close();
        this.document.body.style.overflow = '';
      }
    });
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
    }
  }

  protected onCloseClick(): void {
    this.closeRequest.emit();
  }
}
