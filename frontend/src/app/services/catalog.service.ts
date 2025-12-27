import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import type {
  CategoryWithCount,
  ServiceType,
  Service
} from '../models';

/**
 * Icon names for social media platforms
 * Using ng-icons library (iconoir + simple-icons)
 */
const PLATFORM_ICONS: Record<string, string> = {
  instagram: 'iconoirInstagram',
  tiktok: 'iconoirTiktok',
  twitter: 'iconoirTwitter',
  youtube: 'iconoirYoutube',
  snapchat: 'simpleSnapchat',
  facebook: 'iconoirFacebook',
  discord: 'iconoirDiscord',
  linkedin: 'iconoirLinkedin'
};

/**
 * Mock categories (social media platforms)
 */
const MOCK_CATEGORIES: CategoryWithCount[] = [
  {
    id: 1,
    name: 'Instagram',
    slug: 'instagram',
    sortOrder: 1,
    isActive: true,
    serviceCount: 3
  },
  {
    id: 2,
    name: 'TikTok',
    slug: 'tiktok',
    sortOrder: 2,
    isActive: true,
    serviceCount: 3
  },
  {
    id: 3,
    name: 'Twitter/X',
    slug: 'twitter',
    sortOrder: 3,
    isActive: true,
    serviceCount: 3
  },
  {
    id: 4,
    name: 'YouTube',
    slug: 'youtube',
    sortOrder: 4,
    isActive: true,
    serviceCount: 3
  },
  {
    id: 5,
    name: 'Snapchat',
    slug: 'snapchat',
    sortOrder: 5,
    isActive: true,
    serviceCount: 3
  },
  {
    id: 6,
    name: 'Facebook',
    slug: 'facebook',
    sortOrder: 6,
    isActive: true,
    serviceCount: 3
  },
  {
    id: 7,
    name: 'Discord',
    slug: 'discord',
    sortOrder: 7,
    isActive: true,
    serviceCount: 3
  },
  {
    id: 8,
    name: 'LinkedIn',
    slug: 'linkedin',
    sortOrder: 8,
    isActive: true,
    serviceCount: 3
  }
];

/**
 * Mock service types (Followers, Likes, Comments)
 */
const MOCK_SERVICE_TYPES: ServiceType[] = [
  { id: 1, categoryId: 0, name: 'Followers', slug: 'followers', sortOrder: 1, isActive: true },
  { id: 2, categoryId: 0, name: 'Likes', slug: 'likes', sortOrder: 2, isActive: true },
  { id: 3, categoryId: 0, name: 'Comments', slug: 'comments', sortOrder: 3, isActive: true }
];

/**
 * Generate mock services for all platforms
 */
function generateMockServices(): Service[] {
  const services: Service[] = [];
  let serviceId = 1;

  for (const category of MOCK_CATEGORIES) {
    // Followers
    services.push({
      id: serviceId++,
      categoryId: category.id,
      serviceTypeId: 1,
      name: `${category.name} Followers`,
      description: `High-quality ${category.name} followers with fast delivery`,
      quality: 'HIGH',
      speed: 'FAST',
      minQuantity: 100,
      maxQuantity: 100000,
      pricePerK: 1.10,
      refillDays: 30,
      averageTime: '1-24 hours',
      isActive: true,
      sortOrder: 1
    });

    // Likes
    services.push({
      id: serviceId++,
      categoryId: category.id,
      serviceTypeId: 2,
      name: `${category.name} Likes`,
      description: `Real ${category.name} likes from active accounts`,
      quality: 'HIGH',
      speed: 'INSTANT',
      minQuantity: 50,
      maxQuantity: 50000,
      pricePerK: 0.80,
      refillDays: 0,
      averageTime: '0-1 hours',
      isActive: true,
      sortOrder: 2
    });

    // Comments
    services.push({
      id: serviceId++,
      categoryId: category.id,
      serviceTypeId: 3,
      name: `${category.name} Comments`,
      description: `Custom ${category.name} comments from real users`,
      quality: 'PREMIUM',
      speed: 'MEDIUM',
      minQuantity: 10,
      maxQuantity: 5000,
      pricePerK: 15.00,
      refillDays: 0,
      averageTime: '1-6 hours',
      isActive: true,
      sortOrder: 3
    });
  }

  return services;
}

const MOCK_SERVICES = generateMockServices();

/**
 * CatalogService provides access to the service catalog.
 * Currently uses mock data, prepared for future API integration.
 *
 * @example
 * ```typescript
 * const catalogService = inject(CatalogService);
 *
 * // Get all platforms
 * catalogService.getCategories().subscribe(categories => {
 *   console.log(categories);
 * });
 *
 * // Get services for a platform
 * catalogService.getServicesByCategory(1).subscribe(services => {
 *   console.log(services);
 * });
 * ```
 */
@Injectable({
  providedIn: 'root'
})
export class CatalogService {
  /**
   * Get all active categories (platforms) with service counts
   * @returns Observable of categories sorted by sortOrder
   */
  getCategories(): Observable<CategoryWithCount[]> {
    // TODO: Replace with API call: GET /api/v1/public/categories/with-counts
    const categories = MOCK_CATEGORIES
      .filter(c => c.isActive)
      .sort((a, b) => a.sortOrder - b.sortOrder);

    return of(categories);
  }

  /**
   * Get category by slug
   * @param slug Category slug (e.g., 'instagram')
   * @returns Observable of category or undefined
   */
  getCategoryBySlug(slug: string): Observable<CategoryWithCount | undefined> {
    // TODO: Replace with API call: GET /api/v1/public/categories/{slug}
    const category = MOCK_CATEGORIES.find(
      c => c.slug === slug && c.isActive
    );
    return of(category);
  }

  /**
   * Get category by ID
   * @param id Category ID
   * @returns Observable of category or undefined
   */
  getCategoryById(id: number): Observable<CategoryWithCount | undefined> {
    // TODO: Replace with API call: GET /api/v1/public/categories/{id}
    const category = MOCK_CATEGORIES.find(
      c => c.id === id && c.isActive
    );
    return of(category);
  }

  /**
   * Get icon name for a platform
   * @param slug Platform slug
   * @returns ng-icons icon name
   */
  getPlatformIcon(slug: string): string {
    return PLATFORM_ICONS[slug] ?? 'iconoirQuestionMark';
  }

  /**
   * Get all service types
   * @returns Observable of service types
   */
  getServiceTypes(): Observable<ServiceType[]> {
    return of(MOCK_SERVICE_TYPES.filter(t => t.isActive));
  }

  /**
   * Get services by category ID
   * @param categoryId Category ID
   * @returns Observable of services sorted by sortOrder
   */
  getServicesByCategory(categoryId: number): Observable<Service[]> {
    // TODO: Replace with API call: GET /api/v1/public/categories/{categoryId}/services
    const services = MOCK_SERVICES
      .filter(s => s.categoryId === categoryId && s.isActive)
      .sort((a, b) => a.sortOrder - b.sortOrder);

    return of(services);
  }

  /**
   * Get services by category slug
   * @param slug Category slug (e.g., 'instagram')
   * @returns Observable of services
   */
  getServicesByCategorySlug(slug: string): Observable<Service[]> {
    const category = MOCK_CATEGORIES.find(c => c.slug === slug);
    if (!category) {
      return of([]);
    }
    return this.getServicesByCategory(category.id);
  }

  /**
   * Get service by ID
   * @param id Service ID
   * @returns Observable of service or undefined
   */
  getServiceById(id: number): Observable<Service | undefined> {
    // TODO: Replace with API call: GET /api/v1/public/services/{id}
    const service = MOCK_SERVICES.find(s => s.id === id && s.isActive);
    return of(service);
  }

  /**
   * Find service matching search criteria
   * Used by the order input parser
   * @param categorySlug Platform slug
   * @param serviceTypeSlug Service type slug (followers, likes, comments)
   * @returns Observable of matching service or undefined
   */
  findService(categorySlug: string, serviceTypeSlug: string): Observable<Service | undefined> {
    const category = MOCK_CATEGORIES.find(c => c.slug === categorySlug);
    const serviceType = MOCK_SERVICE_TYPES.find(t => t.slug === serviceTypeSlug);

    if (!category || !serviceType) {
      return of(undefined);
    }

    const service = MOCK_SERVICES.find(
      s => s.categoryId === category.id &&
           s.serviceTypeId === serviceType.id &&
           s.isActive
    );

    return of(service);
  }

  /**
   * Search services with filters
   * @param params Search parameters
   * @returns Observable of matching services
   */
  searchServices(params: {
    categoryId?: number;
    serviceTypeId?: number;
    quality?: string;
    speed?: string;
    search?: string;
  }): Observable<Service[]> {
    // TODO: Replace with API call: GET /api/v1/public/services/search
    let services = MOCK_SERVICES.filter(s => s.isActive);

    if (params.categoryId) {
      services = services.filter(s => s.categoryId === params.categoryId);
    }

    if (params.serviceTypeId) {
      services = services.filter(s => s.serviceTypeId === params.serviceTypeId);
    }

    if (params.quality) {
      services = services.filter(s => s.quality === params.quality);
    }

    if (params.speed) {
      services = services.filter(s => s.speed === params.speed);
    }

    if (params.search) {
      const searchLower = params.search.toLowerCase();
      services = services.filter(
        s => s.name.toLowerCase().includes(searchLower) ||
             s.description.toLowerCase().includes(searchLower)
      );
    }

    return of(services.sort((a, b) => a.sortOrder - b.sortOrder));
  }
}
