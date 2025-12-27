import type { CategorySummary } from './category.model';

/**
 * ServiceType represents a type of service within a category
 * (e.g., Followers, Likes, Comments)
 * Maps to backend ServiceType entity
 */
export interface ServiceType {
  id: number;
  categoryId: number;
  name: string;
  slug: string;
  sortOrder: number;
  isActive: boolean;
}

/**
 * ServiceType with full category details
 */
export interface ServiceTypeWithCategory extends Omit<ServiceType, 'categoryId'> {
  category: CategorySummary;
}

/**
 * Lightweight service type reference
 */
export interface ServiceTypeSummary {
  id: number;
  name: string;
  slug: string;
}
