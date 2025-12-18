import {
  ChangeDetectionStrategy,
  Component,
  computed,
  input,
  output
} from '@angular/core';
import { NgIcon } from '@ng-icons/core';

export interface OrderReadyData {
  matchPercentage: number;
  service: {
    icon: string;
    platform: string;
    type: string;
    quality: string;
    speed: string;
  };
  quantity: number;
  price: string;
  target?: string;
}

@Component({
  selector: 'app-order-ready',
  templateUrl: './order-ready.html',
  styleUrl: './order-ready.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [NgIcon]
})
export class OrderReady {
  /** Order data to display */
  readonly data = input.required<OrderReadyData>();

  /** Emits when user clicks "EXPLORE MORE" */
  readonly exploreMore = output<void>();

  /** Emits when user clicks "MORE [PLATFORM]" */
  readonly morePlatform = output<string>();

  /** Emits when user clicks "PLACE ORDER" */
  readonly placeOrder = output<OrderReadyData>();

  /** Whether target is provided (With Target mode) */
  protected readonly hasTarget = computed(() => !!this.data().target);

  /** Platform name for "MORE X" button */
  protected readonly platformName = computed(() => this.data().service.platform);
}
