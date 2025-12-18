import {
  ChangeDetectionStrategy,
  Component,
  computed,
  input,
  output,
  signal
} from '@angular/core';

/** Available sort orders */
export type SortOrder = 'latest' | 'oldest';

/** Available filter categories */
export type FilterCategory = 'ALL' | 'PENDING' | 'PROCESSING' | 'COMPLETED';

/**
 * OrderFilters Component
 *
 * Displays filter controls for orders page.
 * Includes category dropdown, sort toggle, and search input.
 *
 * @example
 * <app-order-filters
 *   [selectedCategory]="'ALL'"
 *   [sortOrder]="'latest'"
 *   [searchQuery]="''"
 *   (categoryChange)="onCategoryChange($event)"
 *   (sortChange)="onSortChange($event)"
 *   (searchChange)="onSearchChange($event)"
 * />
 */
@Component({
  selector: 'app-order-filters',
  templateUrl: './order-filters.html',
  styleUrl: './order-filters.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class OrderFilters {
  /** Available filter categories */
  readonly categories = input<FilterCategory[]>(['ALL', 'PENDING', 'PROCESSING', 'COMPLETED']);

  /** Currently selected category */
  readonly selectedCategory = input<FilterCategory>('ALL');

  /** Current sort order */
  readonly sortOrder = input<SortOrder>('latest');

  /** Current search query */
  readonly searchQuery = input<string>('');

  /** Emits when category changes */
  readonly categoryChange = output<FilterCategory>();

  /** Emits when sort order changes */
  readonly sortChange = output<SortOrder>();

  /** Emits when search query changes */
  readonly searchChange = output<string>();

  /** Whether category dropdown is open */
  protected readonly isCategoryOpen = signal(false);

  /** Display text for sort button */
  protected readonly sortLabel = computed(() =>
    this.sortOrder() === 'latest' ? 'LATEST' : 'OLDEST'
  );

  /** Toggle category dropdown */
  protected toggleCategoryDropdown(): void {
    this.isCategoryOpen.update(v => !v);
  }

  /** Close category dropdown */
  protected closeCategoryDropdown(): void {
    this.isCategoryOpen.set(false);
  }

  /** Select a category */
  protected selectCategory(category: FilterCategory): void {
    this.categoryChange.emit(category);
    this.closeCategoryDropdown();
  }

  /** Toggle sort order */
  protected toggleSort(): void {
    const newOrder: SortOrder = this.sortOrder() === 'latest' ? 'oldest' : 'latest';
    this.sortChange.emit(newOrder);
  }

  /** Handle search input */
  protected onSearchInput(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.searchChange.emit(target.value);
  }

  /** Handle keyboard navigation */
  protected onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Escape') {
      this.closeCategoryDropdown();
    }
  }
}
