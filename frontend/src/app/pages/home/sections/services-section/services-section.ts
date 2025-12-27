import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
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

  constructor() {
    // Load categories on init
    this.catalogService.getCategories().subscribe(categories => {
      this.categories.set(categories);
    });
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
    this.catalogService.getServicesByCategory(categoryId).subscribe(services => {
      this.categoryServices.set(services);
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
