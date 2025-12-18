import {
  ChangeDetectionStrategy,
  Component,
  input,
  output
} from '@angular/core';

export interface ServiceItemData {
  id: string;
  name: string;
  price: number;
  priceUnit: string;
  quality: string;
  speed: string;
}

@Component({
  selector: 'app-service-item-card',
  templateUrl: './service-item-card.html',
  styleUrl: './service-item-card.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ServiceItemCard {
  /** Service item data */
  readonly service = input.required<ServiceItemData>();

  /** Emits when user clicks "QUICK ORDER" button */
  readonly quickOrder = output<ServiceItemData>();

  /** Emits when card is clicked */
  readonly cardClick = output<ServiceItemData>();

  /** Format price with $ symbol */
  protected formatPrice(): string {
    return `$${this.service().price}`;
  }

  protected onQuickOrder(event: Event): void {
    event.stopPropagation();
    this.quickOrder.emit(this.service());
  }

  protected onCardClick(): void {
    this.cardClick.emit(this.service());
  }
}
