import { Component, ChangeDetectionStrategy, input } from '@angular/core';

@Component({
  selector: 'app-analysis-card',
  imports: [],
  templateUrl: './analysis-card.html',
  styleUrl: './analysis-card.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AnalysisCard {

  /** Icono ASCII para el analysis card */
  readonly icon = input.required<string>();

  /** Titulo del analysis card */
  readonly title = input.required<string>();

  /** Cantidad a mostrar en el analysis card */
  readonly amount = input.required<string>();

}
