import type { CategorySummary } from './category.model';
import type { ServiceTypeSummary } from './service-type.model';

/**
 * Service quality levels
 * Maps to backend ServiceQuality enum
 */
export type ServiceQuality = 'LOW' | 'MEDIUM' | 'HIGH' | 'PREMIUM';

/**
 * Service delivery speed levels
 * Maps to backend ServiceSpeed enum
 */
export type ServiceSpeed = 'SLOW' | 'MEDIUM' | 'FAST' | 'INSTANT';

/**
 * Service represents a specific service offering
 * Maps to backend Service entity
 */
export interface Service {
  id: number;
  categoryId: number;
  serviceTypeId: number;
  name: string;
  description: string;
  quality: ServiceQuality;
  speed: ServiceSpeed;
  minQuantity: number;
  maxQuantity: number;
  pricePerK: number;
  refillDays: number;
  averageTime: string;
  isActive: boolean;
  sortOrder: number;
  createdAt?: string;
  updatedAt?: string;
}

/**
 * Service with full category and type details
 */
export interface ServiceDetail extends Omit<Service, 'categoryId' | 'serviceTypeId'> {
  category: CategorySummary;
  serviceType: ServiceTypeSummary;
}

/**
 * Lightweight service summary for listings
 */
export interface ServiceSummary {
  id: number;
  name: string;
  quality: ServiceQuality;
  speed: ServiceSpeed;
  minQuantity: number;
  maxQuantity: number;
  pricePerK: number;
  refillDays: number;
  averageTime: string;
}

/**
 * Helper to calculate price for a given quantity
 */
export function calculateServicePrice(pricePerK: number, quantity: number): number {
  return (pricePerK * quantity) / 1000;
}

/**
 * Helper to check if service has refill guarantee
 */
export function hasRefillGuarantee(service: Service | ServiceSummary): boolean {
  return service.refillDays > 0;
}

/**
 * Helper to format quality for display
 */
export function formatQuality(quality: ServiceQuality): string {
  return quality.charAt(0) + quality.slice(1).toLowerCase();
}

/**
 * Helper to format speed for display
 */
export function formatSpeed(speed: ServiceSpeed): string {
  return speed.charAt(0) + speed.slice(1).toLowerCase();
}
