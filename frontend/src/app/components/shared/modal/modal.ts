import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  inject,
  input,
  output,
  effect,
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

@Component({
  selector: 'app-modal',
  templateUrl: './modal.html',
  styleUrl: './modal.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class Modal {
  private readonly document = inject(DOCUMENT);

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
