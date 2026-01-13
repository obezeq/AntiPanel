/**
 * Custom Pipes
 *
 * Standalone pipes for data transformation in templates.
 *
 * @example Usage in component
 * ```typescript
 * import { RelativeTimePipe } from '@app/pipes';
 *
 * @Component({
 *   imports: [RelativeTimePipe],
 *   template: '{{ date | relativeTime }}'
 * })
 * export class MyComponent {}
 * ```
 */

export { RelativeTimePipe } from './relative-time.pipe';
