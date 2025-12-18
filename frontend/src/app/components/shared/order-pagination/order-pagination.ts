import {
  ChangeDetectionStrategy,
  Component,
  computed,
  input,
  output,
  signal
} from '@angular/core';

/**
 * OrderPagination Component
 *
 * Displays pagination controls for orders list.
 * Shows page size selector and page navigation.
 *
 * @example
 * <app-order-pagination
 *   [currentPage]="1"
 *   [totalPages]="33"
 *   [pageSize]="10"
 *   (pageChange)="onPageChange($event)"
 *   (pageSizeChange)="onPageSizeChange($event)"
 * />
 */
@Component({
  selector: 'app-order-pagination',
  templateUrl: './order-pagination.html',
  styleUrl: './order-pagination.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class OrderPagination {
  /** Current page number (1-indexed) */
  readonly currentPage = input.required<number>();

  /** Total number of pages */
  readonly totalPages = input.required<number>();

  /** Number of items per page */
  readonly pageSize = input<number>(10);

  /** Available page size options */
  readonly pageSizeOptions = input<number[]>([10, 25, 50, 100]);

  /** Emits when page changes */
  readonly pageChange = output<number>();

  /** Emits when page size changes */
  readonly pageSizeChange = output<number>();

  /** Whether dropdown is open */
  protected readonly isDropdownOpen = signal(false);

  /** Whether previous button is disabled */
  protected readonly isPrevDisabled = computed(() => this.currentPage() <= 1);

  /** Whether next button is disabled */
  protected readonly isNextDisabled = computed(() => this.currentPage() >= this.totalPages());

  /** Go to previous page */
  protected goToPrev(): void {
    if (!this.isPrevDisabled()) {
      this.pageChange.emit(this.currentPage() - 1);
    }
  }

  /** Go to next page */
  protected goToNext(): void {
    if (!this.isNextDisabled()) {
      this.pageChange.emit(this.currentPage() + 1);
    }
  }

  /** Toggle dropdown visibility */
  protected toggleDropdown(): void {
    this.isDropdownOpen.update(v => !v);
  }

  /** Close dropdown */
  protected closeDropdown(): void {
    this.isDropdownOpen.set(false);
  }

  /** Select a page size option */
  protected selectPageSize(size: number): void {
    this.pageSizeChange.emit(size);
    this.closeDropdown();
  }

  /** Handle keyboard navigation on dropdown */
  protected onDropdownKeydown(event: KeyboardEvent): void {
    if (event.key === 'Escape') {
      this.closeDropdown();
    }
  }
}
