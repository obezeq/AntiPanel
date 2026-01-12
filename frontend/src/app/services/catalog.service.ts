import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, tap, catchError, map, switchMap } from 'rxjs';
import { environment } from '../../environments/environment';
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
 * CatalogService provides access to the service catalog via API.
 * Uses signals for caching and reactive state management.
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
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/public`;

  /** Cached categories with service counts */
  private readonly categoriesCache = signal<CategoryWithCount[] | null>(null);

  /** Cached services by category ID */
  private readonly servicesCacheMap = signal<Map<number, Service[]>>(new Map());

  /** Cached service types by category ID */
  private readonly serviceTypesCache = signal<Map<number, ServiceType[]>>(new Map());

  /**
   * Get all active categories (platforms) with service counts
   * @returns Observable of categories sorted by sortOrder
   */
  getCategories(): Observable<CategoryWithCount[]> {
    const cached = this.categoriesCache();
    if (cached) {
      return of(cached);
    }

    return this.http.get<CategoryWithCount[]>(`${this.baseUrl}/categories/with-counts`).pipe(
      map(categories => categories.filter(c => c.serviceCount > 0)),
      tap(categories => this.categoriesCache.set(categories)),
      catchError(error => {
        console.error('Failed to fetch categories:', error);
        return of([]);
      })
    );
  }

  /**
   * Get category by slug (case-insensitive)
   * @param slug Category slug (e.g., 'instagram')
   * @returns Observable of category or undefined
   */
  getCategoryBySlug(slug: string): Observable<CategoryWithCount | undefined> {
    const normalized = slug.toLowerCase().replace(/-/g, '');
    return this.getCategories().pipe(
      map(categories => categories.find(c =>
        c.slug.toLowerCase() === slug.toLowerCase() ||
        c.slug.toLowerCase().replace(/-/g, '') === normalized
      ))
    );
  }

  /**
   * Get category by ID
   * @param id Category ID
   * @returns Observable of category or undefined
   */
  getCategoryById(id: number): Observable<CategoryWithCount | undefined> {
    return this.getCategories().pipe(
      map(categories => categories.find(c => c.id === id))
    );
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
   * Get all service types (legacy - returns common types)
   * @returns Observable of service types
   * @deprecated Use getServiceTypesByCategory instead for accurate IDs
   */
  getServiceTypes(): Observable<ServiceType[]> {
    return of([
      { id: 1, categoryId: 0, name: 'Followers', slug: 'followers', sortOrder: 1, isActive: true },
      { id: 2, categoryId: 0, name: 'Likes', slug: 'likes', sortOrder: 2, isActive: true },
      { id: 3, categoryId: 0, name: 'Comments', slug: 'comments', sortOrder: 3, isActive: true },
      { id: 4, categoryId: 0, name: 'Views', slug: 'views', sortOrder: 4, isActive: true },
      { id: 5, categoryId: 0, name: 'Shares', slug: 'shares', sortOrder: 5, isActive: true }
    ]);
  }

  /**
   * Get service types for a specific category (from API)
   * @param categoryId Category ID
   * @returns Observable of service types for this category
   */
  getServiceTypesByCategory(categoryId: number): Observable<ServiceType[]> {
    const cached = this.serviceTypesCache().get(categoryId);
    if (cached) {
      return of(cached);
    }

    return this.http.get<ServiceType[]>(`${this.baseUrl}/categories/${categoryId}/service-types`).pipe(
      tap(types => {
        const newCache = new Map(this.serviceTypesCache());
        newCache.set(categoryId, types);
        this.serviceTypesCache.set(newCache);
      }),
      catchError(error => {
        console.error(`Failed to fetch service types for category ${categoryId}:`, error);
        return of([]);
      })
    );
  }

  /**
   * Get services by category ID
   * @param categoryId Category ID
   * @returns Observable of services sorted by sortOrder
   */
  getServicesByCategory(categoryId: number): Observable<Service[]> {
    const cached = this.servicesCacheMap().get(categoryId);
    if (cached) {
      return of(cached);
    }

    return this.http.get<Service[]>(`${this.baseUrl}/categories/${categoryId}/services`).pipe(
      tap(services => {
        const currentCache = this.servicesCacheMap();
        const newCache = new Map(currentCache);
        newCache.set(categoryId, services);
        this.servicesCacheMap.set(newCache);
      }),
      catchError(error => {
        console.error(`Failed to fetch services for category ${categoryId}:`, error);
        return of([]);
      })
    );
  }

  /**
   * Get services by category slug
   * @param slug Category slug (e.g., 'instagram')
   * @returns Observable of services
   */
  getServicesByCategorySlug(slug: string): Observable<Service[]> {
    return this.getCategoryBySlug(slug).pipe(
      switchMap(category => {
        if (!category) {
          return of([]);
        }
        return this.getServicesByCategory(category.id);
      })
    );
  }

  /**
   * Get service by ID
   * @param id Service ID
   * @returns Observable of service or undefined
   */
  getServiceById(id: number): Observable<Service | undefined> {
    return this.http.get<Service>(`${this.baseUrl}/services/${id}`).pipe(
      catchError(error => {
        console.error(`Failed to fetch service ${id}:`, error);
        return of(undefined);
      })
    );
  }

  /**
   * Find service matching search criteria
   * Uses dynamic service type lookup per category (not hardcoded IDs)
   * @param categorySlug Platform slug
   * @param serviceTypeSlug Service type slug (followers, likes, comments)
   * @returns Observable of matching service or undefined
   */
  findService(categorySlug: string, serviceTypeSlug: string): Observable<Service | undefined> {
    return this.getCategoryBySlug(categorySlug).pipe(
      switchMap(category => {
        if (!category) {
          return of(undefined);
        }

        // Look up service type dynamically for this category
        return this.getServiceTypesByCategory(category.id).pipe(
          switchMap(serviceTypes => {
            const serviceType = serviceTypes.find(st =>
              st.slug.toLowerCase() === serviceTypeSlug.toLowerCase()
            );

            if (!serviceType) {
              return of(undefined);
            }

            return this.http.get<Service[]>(
              `${this.baseUrl}/categories/${category.id}/types/${serviceType.id}/services`
            ).pipe(
              map(services => services.length > 0 ? services[0] : undefined),
              catchError(() => of(undefined))
            );
          })
        );
      })
    );
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
    const queryParams = new URLSearchParams();

    if (params.categoryId) queryParams.set('categoryId', params.categoryId.toString());
    if (params.serviceTypeId) queryParams.set('serviceTypeId', params.serviceTypeId.toString());
    if (params.quality) queryParams.set('quality', params.quality);
    if (params.speed) queryParams.set('speed', params.speed);
    if (params.search) queryParams.set('search', params.search);

    const queryString = queryParams.toString();
    const url = queryString
      ? `${this.baseUrl}/services/search?${queryString}`
      : `${this.baseUrl}/services/search`;

    return this.http.get<{ content: Service[] }>(url).pipe(
      map(response => response.content),
      catchError(error => {
        console.error('Failed to search services:', error);
        return of([]);
      })
    );
  }

  /**
   * Clear all cached data
   * Call this when data might be stale (e.g., after admin updates)
   */
  clearCache(): void {
    this.categoriesCache.set(null);
    this.servicesCacheMap.set(new Map());
    this.serviceTypesCache.set(new Map());
  }
}
