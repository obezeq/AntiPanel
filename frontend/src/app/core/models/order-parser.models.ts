/**
 * Parsed order data from user input.
 * Result of the OrderParserService.parse() method.
 */
export interface ParsedOrder {
  quantity: number | null;
  platform: string | null;
  serviceType: string | null;
  target: string | null;
  matchPercentage: number;
}

/**
 * Keyword mapping structure.
 * Maps user input keywords to canonical service identifiers.
 */
export interface KeywordMapping {
  readonly platforms: Readonly<Record<string, string>>;
  readonly serviceTypes: Readonly<Record<string, string>>;
}

/**
 * Display name mappings for UI.
 * Maps slugs to human-readable names.
 */
export interface DisplayNameMapping {
  readonly platforms: Readonly<Record<string, string>>;
  readonly serviceTypes: Readonly<Record<string, string>>;
}
