/**
 * Category represents a social media platform (e.g., Instagram, TikTok)
 * Maps to backend Category entity
 */
export interface Category {
  id: number;
  name: string;
  slug: string;
  iconUrl?: string;
  sortOrder: number;
  isActive: boolean;
  createdAt?: string;
}

/**
 * Category with service count for catalog display
 */
export interface CategoryWithCount extends Category {
  serviceCount: number;
}

/**
 * Lightweight category reference for nested objects
 */
export interface CategorySummary {
  id: number;
  name: string;
  slug: string;
  iconUrl?: string;
}
