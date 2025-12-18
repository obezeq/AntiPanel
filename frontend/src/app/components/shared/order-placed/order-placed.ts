import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  effect,
  inject,
  input,
  output,
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
  selector: 'app-order-placed',
  templateUrl: './order-placed.html',
  styleUrl: './order-placed.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class OrderPlaced {
  private readonly document = inject(DOCUMENT);

  /** Element that had focus before modal opened */
  private previouslyFocusedElement: HTMLElement | null = null;

  /** Whether the modal is visible */
  readonly isOpen = input<boolean>(false);

  /** Emits when user clicks "CONTINUE" or closes modal */
  readonly continueClick = output<void>();

  /** Reference to dialog element */
  private readonly dialogRef = viewChild<ElementRef<HTMLDialogElement>>('dialogRef');

  constructor() {
    // Effect to sync isOpen input with dialog state
    effect(() => {
      const dialog = this.dialogRef()?.nativeElement;
      if (!dialog) return;

      if (this.isOpen()) {
        // Save currently focused element
        this.previouslyFocusedElement = this.document.activeElement as HTMLElement;

        dialog.showModal();
        this.document.body.style.overflow = 'hidden';

        // Focus the continue button
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

  /** Handle overlay click to close */
  protected onOverlayClick(event: MouseEvent): void {
    if (event.target === event.currentTarget) {
      this.continueClick.emit();
    }
  }

  /** Handle ESC key and focus trap */
  protected onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Escape') {
      event.preventDefault();
      this.continueClick.emit();
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
}
