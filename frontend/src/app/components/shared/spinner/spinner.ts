import { ChangeDetectionStrategy, Component, input } from '@angular/core';

/**
 * Tamanos disponibles para el spinner
 */
export type SpinnerSize = 'sm' | 'md' | 'lg';

/**
 * Spinner - Componente de Carga
 *
 * Indicador visual de carga con soporte para:
 * - Diferentes tamanos
 * - Overlay fullscreen opcional
 * - Accesibilidad (aria-busy, aria-label)
 *
 * @example
 * ```html
 * <!-- Spinner simple -->
 * <app-spinner />
 *
 * <!-- Spinner con tamano especifico -->
 * <app-spinner size="lg" />
 *
 * <!-- Spinner con overlay fullscreen -->
 * <app-spinner [overlay]="true" />
 *
 * <!-- Spinner con label personalizado -->
 * <app-spinner label="Cargando datos..." />
 * ```
 */
@Component({
  selector: 'app-spinner',
  templateUrl: './spinner.html',
  styleUrl: './spinner.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    '[class.spinner--sm]': 'size() === "sm"',
    '[class.spinner--md]': 'size() === "md"',
    '[class.spinner--lg]': 'size() === "lg"',
    '[class.spinner--overlay]': 'overlay()',
    '[attr.role]': '"status"',
    '[attr.aria-busy]': '"true"',
    '[attr.aria-label]': 'label()'
  }
})
export class Spinner {
  /** Tamano del spinner */
  readonly size = input<SpinnerSize>('md');

  /** Mostrar con overlay fullscreen */
  readonly overlay = input<boolean>(false);

  /** Texto accesible para lectores de pantalla */
  readonly label = input<string>('Cargando...');

  /** Mostrar texto de carga visible */
  readonly showLabel = input<boolean>(false);
}
