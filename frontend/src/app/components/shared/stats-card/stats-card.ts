import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { NgIcon } from '@ng-icons/core';

export type StatsCardVariant = 'default' | 'success' | 'info' | 'warning';

export interface StatsCardData {
  /** Nombre del icono ngicons (ej: 'matActivity') */
  icon: string;
  /** Título del card (ej: 'TOTAL', 'PENDING', 'STATUS') */
  title: string;
  /** Valor numérico o texto a mostrar */
  value: string | number;
  /** Descripción debajo del valor */
  label: string;
}

@Component({
  selector: 'app-stats-card',
  templateUrl: './stats-card.html',
  styleUrl: './stats-card.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [NgIcon]
})
export class StatsCard {
  /** Stats data */
  readonly stats = input.required<StatsCardData>();

  /** Card visual variant - afecta el color del valor */
  readonly variant = input<StatsCardVariant>('default');
}
