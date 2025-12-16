import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NgIcon } from '@ng-icons/core';

export interface ServiceCardData {
  id: string;
  name: string;
  /** Nombre del icono ngicons (ej: 'iconoirInstagram') */
  icon: string;
  serviceCount: number;
  slug: string;
}

@Component({
  selector: 'app-service-card',
  templateUrl: './service-card.html',
  styleUrl: './service-card.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, NgIcon]
})
export class ServiceCard {
  /** Service data */
  readonly service = input.required<ServiceCardData>();

  /** Whether the card is interactive */
  readonly interactive = input<boolean>(true);

  /** Click event emitter */
  readonly cardClick = output<ServiceCardData>();

  protected onClick(): void {
    if (this.interactive()) {
      this.cardClick.emit(this.service());
    }
  }
}
