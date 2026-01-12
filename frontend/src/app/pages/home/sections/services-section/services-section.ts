import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  input,
  output,
  signal
} from '@angular/core';
import { NgIcon } from '@ng-icons/core';
import { DashboardSectionHeader } from '../../../../components/shared/dashboard-section-header/dashboard-section-header';
import { ServiceCard, type ServiceCardData } from '../../../../components/shared/service-card/service-card';
import { ServiceItemCard, type ServiceItemData } from '../../../../components/shared/service-item-card/service-item-card';
import { CatalogService } from '../../../../services/catalog.service';
import type { CategoryWithCount, Service } from '../../../../models';

/**
 * ServicesSection component for the home page.
 * Displays platform cards that expand to show available services.
 *
 * @example
 * <app-services-section
 *   (quickOrder)="handleQuickOrder($event)"
 * />
 */
@Component({
  selector: 'app-services-section',
  templateUrl: './services-section.html',
  styleUrl: './services-section.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [NgIcon, DashboardSectionHeader, ServiceCard, ServiceItemCard]
})
export class ServicesSection {
  private readonly catalogService = inject(CatalogService);

  /** Platform slug to auto-select (from "More Platform" button) */
  readonly platformToSelect = input<string | null>(null);

  /** Whether to reset platform selection (from "Explore More" button) */
  readonly shouldReset = input<boolean>(false);

  /** Emits when user clicks "QUICK ORDER" on a service */
  readonly quickOrder = output<ServiceItemData>();

  /** All available categories (platforms) */
  protected readonly categories = signal<CategoryWithCount[]>([]);

  /** Currently selected category (null = show all platforms) */
  protected readonly selectedCategory = signal<CategoryWithCount | null>(null);

  /** Services for the selected category */
  protected readonly categoryServices = signal<Service[]>([]);

  /** Whether expanded view is active */
  protected readonly isExpanded = computed(() => this.selectedCategory() !== null);

  /** Whether categories are loading */
  protected readonly isLoadingCategories = signal<boolean>(true);

  /** Whether services are loading */
  protected readonly isLoadingServices = signal<boolean>(false);

  /** Error message for failed API calls */
  protected readonly errorMessage = signal<string | null>(null);

  constructor() {
    // Load categories on init
    this.isLoadingCategories.set(true);
    this.errorMessage.set(null);
    this.catalogService.getCategories().subscribe({
      next: (categories) => {
        this.categories.set(categories);
        this.isLoadingCategories.set(false);
      },
      error: () => {
        this.isLoadingCategories.set(false);
        this.errorMessage.set('Failed to load services. Please try again.');
      }
    });

    // Watch for platform selection from parent
    effect(() => {
      const slug = this.platformToSelect();
      if (slug) {
        this.selectPlatformBySlug(slug);
      }
    });

    // Watch for reset signal from parent
    effect(() => {
      if (this.shouldReset()) {
        this.onBackClick();
      }
    });
  }

  /**
   * Select a platform by its slug
   */
  selectPlatformBySlug(slug: string): void {
    const category = this.categories().find(c => c.slug === slug);
    if (category) {
      this.selectedCategory.set(category);
      this.loadCategoryServices(category.id);
    }
  }

  /**
   * Map category to ServiceCardData for the ServiceCard component
   */
  protected mapCategoryToServiceCardData(category: CategoryWithCount): ServiceCardData {
    return {
      id: category.id.toString(),
      name: category.name.toUpperCase(),
      icon: this.catalogService.getPlatformIcon(category.slug),
      serviceCount: category.serviceCount,
      slug: category.slug
    };
  }

  /**
   * Map service to ServiceItemData for the ServiceItemCard component
   */
  protected mapServiceToItemData(service: Service): ServiceItemData {
    return {
      id: service.id.toString(),
      name: service.name,
      price: service.pricePerK,
      priceUnit: 'PER 1K',
      quality: service.quality,
      speed: service.speed
    };
  }

  /**
   * Handle platform card click - expand to show services
   */
  protected onPlatformClick(data: ServiceCardData): void {
    const category = this.categories().find(c => c.id.toString() === data.id);
    if (category) {
      this.selectedCategory.set(category);
      this.loadCategoryServices(category.id);
    }
  }

  /**
   * Handle back button click - return to platform grid
   */
  protected onBackClick(): void {
    this.selectedCategory.set(null);
    this.categoryServices.set([]);
  }

  /**
   * Handle quick order click
   */
  protected onQuickOrder(data: ServiceItemData): void {
    this.quickOrder.emit(data);
  }

  /**
   * Load services for a category
   */
  private loadCategoryServices(categoryId: number): void {
    this.isLoadingServices.set(true);
    this.errorMessage.set(null);
    this.catalogService.getServicesByCategory(categoryId).subscribe({
      next: (services) => {
        this.categoryServices.set(services);
        this.isLoadingServices.set(false);
      },
      error: () => {
        this.isLoadingServices.set(false);
        this.errorMessage.set('Failed to load services. Please try again.');
      }
    });
  }

  /**
   * Handle keyboard navigation for back button
   */
  protected onBackKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault();
      this.onBackClick();
    }
  }
}
